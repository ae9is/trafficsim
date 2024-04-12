/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package trafficsim;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Traffic controller that alternates between 4 approaches with fixed timings.
 */
public class BasicTrafficController extends TrafficController {
	private HashMap<Long, Double> periods;
	private HashMap<Long, Double> yellowTimes;
	private LinkedList<Long> nextGreen; // Stores approach node IDs in order of which should be green next
	private Long currentGreen;

	public BasicTrafficController(HashMap<Long, Integer> states, HashMap<Long, Double> periods,
			HashMap<Long, Double> yellowTimes) {
		super(states);
		this.periods = periods;
		this.yellowTimes = yellowTimes;
		nextGreen = new LinkedList<>(states.keySet());
		currentGreen = nextGreen.getLast();
	}

	@Override
	public void updateStates() {
		// Time variable used as the time since last light change
		double currentPeriod = periods.get(currentGreen);
		double currentYellow = yellowTimes.get(currentGreen);
		if (time > currentPeriod) {
			// Set next light in queue to green:
			currentGreen = nextGreen.poll();
			nextGreen.add(currentGreen);
			time = 0;
			// All lights reset to red
			for (Long approachNode : states.keySet()) {
				states.put(approachNode, Intersection.RED);
			}
			// Except next one to be green
			states.put(currentGreen, Intersection.GREEN);
		} else if (time > currentPeriod - currentYellow) {
			// Set current green to yellow
			states.put(currentGreen, Intersection.YELLOW);
		}
	}
}
