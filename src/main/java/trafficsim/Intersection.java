/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package trafficsim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import util.Log;

/**
 * Logic for traffic intersections.
 */
public class Intersection {

	private final int type;
	public static final int ALL_WAY_STOP = 0;
	public static final int TWO_WAY_STOP = 1; // Major street with lights, minor without
	public static final int ONE_WAY_STOP = 2; // T intersection no lights
	public static final int FULLY_LIT = 3;

	private TrafficController trafficController;
	private HashMap<NodePair, Double> periods; // Opposing approach nodes to period of each light green
	private HashMap<Long, Double> yellowTimes; // Approach node id to yellow light time
	public static final double LONG_LIGHT = 60; // seconds
	public static final double MEDIUM_LIGHT = 40; // seconds
	public static final double SHORT_LIGHT = 15; // seconds
	private HashMap<Long, Integer> states; // Approach node id to light state
	public static final int RED = 0;
	public static final int YELLOW = 1;
	public static final int GREEN = 2;
	public static final int LEFT_GREEN = 3;
	public static final int RIGHT_GREEN = 4;

	private ArrayList<Car> polledCars = new ArrayList<>();
	private Node intersectionNode;
	private HashMap<Long, Node> approachNodeMap; // Node id to approach node
	private HashMap<Long, LinkedList<Car>> approachQueueMap; // Node id to cars queued at light
	private LinkedList<Long> nextToPoll; // For 4-way stop the next line to remove a car from

	// Want to store approach node id's and light state.
	// Initially set nodes on same way to same light state.
	public Intersection(int type, Node intersectionNode, ArrayList<Node> approachingNodes,
			ArrayList<Way> approachingWays) {
		if (approachingNodes.size() != approachingWays.size()) {
			Log.error("Approach nodes size must equal approach ways size!");
			System.exit(-1);
		}
		this.type = type;
		this.intersectionNode = intersectionNode;
		approachNodeMap = new HashMap<>();
		states = new HashMap<>();
		periods = new HashMap<>();
		yellowTimes = new HashMap<>();
		approachQueueMap = new HashMap<>();
		nextToPoll = new LinkedList<>();
		for (Node node : approachingNodes) {
			approachNodeMap.put(node.getId(), node);
			states.put(node.getId(), RED);
			approachQueueMap.put(node.getId(), new LinkedList<Car>());
			nextToPoll.add(node.getId());
		}
		for (int i = 0; i < approachingWays.size(); i++) {
			Way approachWay = approachingWays.get(i);
			Node approachNode = approachingNodes.get(i);
			yellowTimes.put(approachNode.getId(), getYellowTime(approachWay));
		}
		TreeSet<NodePair> opposingNodes = getOpposingNodes(); // intersectionNode, approachingNodes);
		for (NodePair pair : opposingNodes) {
			periods.put(pair, MEDIUM_LIGHT);
		}
		// TODO optimise so no traffic controller / states created and updated for 4-way stops
		// trafficController = new BasicTrafficController(states, periods, yellowTimes);
		// trafficController = new NormalTrafficController(states, periods, yellowTimes);
		trafficController = new LinearWeightedTrafficController(this, states, periods, yellowTimes);
	}

	public Long getId() {
		return (intersectionNode == null) ? null : intersectionNode.getId();
	}

	public int getType() {
		return type;
	}

	public final Collection<Node> getApproachNodes() {
		return approachNodeMap.values();
	}

	// Input the node the vehicle is approaching the intersection from, get state of light for that approach node
	public int getState(Node approach) {
		Integer val = states.get(approach.getId());
		if (val != null) {
			return val;
		}
		Log.error("Car approaching intersection " + intersectionNode.getId() + " from invalid node : " + approach.getId());
		return RED;
	}

	// Input current simulation time and controller state updated
	public void updateStates(double timestep) {
		trafficController.step(timestep);
		states = trafficController.getStates();
	}

	// Input approach node, get yellow time. Depends on way speed.
	// For determining whether approaching car should stop for yellow.
	public double getYellowTime(Car car) {
		return getYellowTime(car.getCurrentWay());
	}

	private double getYellowTime(Way way) {
		double s;
		if (way != null) {
			s = way.getMaxspeedKm();
		} else {
			// If car has no current way (temporarily off-road) assume a really short yellow time
			s = 15;
		}
		// Yellow light times minimum (kph): 40/3.0s, 48/3.5s, 56/4.0s, 64/4.5s, 72/5.0s, 80/5.5s, 88/6.0s
		double yellowTime;
		if (s <= 30) {
			yellowTime = 2.5; // made up
		} else if (s < 48) {
			yellowTime = 3.0;
		} else if (s < 56) {
			yellowTime = 3.5;
		} else if (s < 64) {
			yellowTime = 4.0;
		} else if (s < 72) {
			yellowTime = 4.5;
		} else if (s < 80) {
			yellowTime = 5.0;
		} else if (s < 88) {
			yellowTime = 5.5;
		} else {
			yellowTime = 6.0;
		}
		return yellowTime;
	}

	public void queue(Car car) {
		long prev = car.getPrevNode().getId();
		LinkedList<Car> queue = approachQueueMap.get(prev);
		if (!queue.contains(car)) {
			queue.add(car);
		}
	}

	public boolean isQueued(Car car) {
		long prev = car.getPrevNode().getId();
		LinkedList<Car> queue = approachQueueMap.get(prev);
		return queue.contains(car);
	}

	public HashMap<Long, LinkedList<Car>> getApproachQueueMap() {
		return approachQueueMap;
	}

	public ArrayList<Car> getPolledCars() {
		return polledCars;
	}

	public void forgetPolled(Car car) {
		for (Iterator<Car> iter = polledCars.iterator(); iter.hasNext();) {
			Car c = iter.next();
			if (c.getId().longValue() == car.getId().longValue()) {
				iter.remove();
			}
		}
		if (polledCars.contains(car)) {
			Log.error("Could not remove polled car from polledCars!");
		}
	}

	public void unqueueNext() {
		Car polled = pollNext();
		if (polled != null) {
			polledCars.add(polled);
		}
	}

	private Car pollNext() {
		Car car = null;
		// Iterate through all approaching nodes to find next car to poll (if there exists one)
		for (int i = 0; i < nextToPoll.size(); i++) {
			// Get id of next approach node queue to poll from; add back to back of queue
			long nextId = nextToPoll.poll();
			nextToPoll.add(nextId);
			LinkedList<Car> queue = approachQueueMap.get(nextId);
			car = queue.poll();
			if (car != null) {
				if (queue.contains(car)) {
					Log.error("Queue polled car but still contains car");
				}
				break;
			}
		}
		return car;
	}

	// Signals whether remove was successful
	public boolean remove(Car car) {
		Node prev = car.getPrevNode();
		if (prev == null) {
			return false;
		}
		LinkedList<Car> queue = approachQueueMap.get(prev.getId());
		if (queue == null) {
			return false;
		}
		return queue.remove(car);
	}

	/*
	 * Returns tuples of opposing nodes that approach the intersection node.
	 * 
	 * Algorithm:
	 * 
	 * a & b opposite if both:
	 * 
	 * 1. xa < xi < xb or xa > xi > xb 
	 * and
	 * 2. ya < yi < yb or ya > yi > yb
	 */
	public final TreeSet<NodePair> getOpposingNodes() {
		TreeSet<NodePair> opposing = new TreeSet<>();
		Node i = intersectionNode;
		for (Node a : approachNodeMap.values()) {
			NodePair opposites = new NodePair();
			opposites.a = a;
			for (Node b : approachNodeMap.values()) {
				if (!a.equals(b) && ((a.getLat() < i.getLat() && i.getLat() < b.getLat())
						|| (a.getLat() > i.getLat() && i.getLat() > b.getLat())) && // 1.
						((a.getLon() < i.getLon() && i.getLon() < b.getLon())
								|| (a.getLon() > i.getLon() && i.getLon() > b.getLon()))) // 2.
				{
					opposites.b = b;
				}
			}
			opposing.add(opposites);
		}
		return opposing;
	}

	public final HashMap<NodePair, Double> getPeriods() {
		return periods;
	}
}
