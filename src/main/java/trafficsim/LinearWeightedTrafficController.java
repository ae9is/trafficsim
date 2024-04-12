/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package trafficsim;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

/**
 * A traffic controller whose light timings (periods) are weighted linearly according to how much traffic is coming from
 * each approach.
 * 
 * To be precise, queues are kept for each approach in Intersection: cars are added to the queue when
 * they must stop for the light and are removed when they exit the Intersection.
 */
public class LinearWeightedTrafficController extends NormalTrafficController {
	Intersection intersection;

	// Same as parent, but uses intersection queue info as well
	public LinearWeightedTrafficController(Intersection intersection, HashMap<Long, Integer> states,
			HashMap<NodePair, Double> periods, HashMap<Long, Double> yellowTimes) {
		super(states, periods, yellowTimes);
		this.intersection = intersection;
	}

	@Override
	public void updateStates() {
		// (1) Set periods according to amount of traffic queued in each approach
		// Get amount of queued traffic in intersection
		HashMap<Long, LinkedList<Car>> map = intersection.getApproachQueueMap();
		HashMap<NodePair, Integer> traffic = new HashMap<>();
		// Get all approach nodes, in opposing pairs if possible.
		// No need to call intersection.getOpposingNodes() since we can get them from "periods"
		// TreeSet<NodePair> opp = intersection.getOpposingNodes();
		Set<NodePair> opp = nextPeriods.keySet();
		for (NodePair pair : opp) {
			// For each pair, find out sum of traffic in intersection approach queues
			Integer sizeA = map.get(pair.a.getId()).size();
			Integer sizeB = null;
			if (pair.b != null) {
				sizeB = map.get(pair.b.getId()).size();
			}
			int size = (sizeB == null) ? sizeA : (sizeA + sizeB);
			traffic.put(pair, size);
		}
		int totalSize = 0;
		for (int v : traffic.values()) {
			totalSize += v;
		}
		// Weight periods linearly to total amount traffic queued
		double normalPeriodPerPair = Intersection.MEDIUM_LIGHT;
		double totalPeriod = opp.size() * normalPeriodPerPair;
		for (NodePair pair : nextPeriods.keySet()) {
			double pairPeriod = (totalSize > 0) ? totalPeriod * traffic.get(pair) / totalSize : normalPeriodPerPair;
			nextPeriods.put(pair, pairPeriod);
		}
		// (2) Call normal traffic controller update method
		super.updateStates();
	}
}
