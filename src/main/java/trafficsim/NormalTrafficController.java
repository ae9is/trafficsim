/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package trafficsim;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Traffic controller that alternates between N approaching ways with fixed timings.
 */
public class NormalTrafficController extends TrafficController {
	protected HashMap<NodePair, Double> nextPeriods; // controller only changes after current light is "finished" (switches red)
	private HashMap<NodePair, Double> periods;
	private LinkedList<NodePair> nextGreens; // stores approach node id's in order of which should be green next
	private NodePair currentGreens;
	private HashMap<Long, Double> yellowTimes; // speeds can be change after intersection so keep one time per approach node

	public NormalTrafficController(HashMap<Long, Integer> states, HashMap<NodePair, Double> periods,
			HashMap<Long, Double> yellowTimes) {
		super(states);
		this.periods = periods;
		this.nextPeriods = periods;
		this.yellowTimes = yellowTimes;
		nextGreens = new LinkedList<>(periods.keySet());
		currentGreens = nextGreens.getLast();
	}

	@Override
	public void updateStates() {
		// time variable used as the time since last light change
		double period = periods.get(currentGreens);
		double yellowA = yellowTimes.get(currentGreens.a.getId());
		double yellowB = Double.MIN_VALUE;
		if (currentGreens.b != null) {
			yellowB = yellowTimes.get(currentGreens.b.getId());
		}
		if (time > period) {
			// set next light(s) in queue to green:
			currentGreens = nextGreens.poll();
			nextGreens.add(currentGreens);
			time = 0;
			// all lights reset to red
			for (Long approachNode : states.keySet()) {
				states.put(approachNode, Intersection.RED);
			}
			// set next appropriate opposing (not perpendicular) lights to green
			states.put(currentGreens.a.getId(), Intersection.GREEN);
			if (currentGreens.b != null) {
				states.put(currentGreens.b.getId(), Intersection.GREEN);
			}
			// set value of next light cycles
			periods = nextPeriods;
		} else {
			// set yellow lights as appropriate
			if (time > period - yellowA) {
				states.put(currentGreens.a.getId(), Intersection.YELLOW);
			}
			if (currentGreens.b != null && time > period - yellowB) {
				states.put(currentGreens.b.getId(), Intersection.YELLOW);
			}
		}
	}
}
