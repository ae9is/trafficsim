/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package trafficsim;

import java.util.HashMap;

/**
 * State machine for intersection traffic lights.
 * Given new simulation time should update and return light signals in
 * all intersection directions.
 */
public abstract class TrafficController {
	protected double time;
	protected HashMap<Long, Integer> states; // copy constructed from Intersection.states

	public TrafficController(HashMap<Long, Integer> states) {
		this.states = new HashMap<>(states);
		time = 0;
	}

	public void step(double timestep) {
		time += timestep;
		updateStates();
	}

	// returns approach node id to light state hash map
	public HashMap<Long, Integer> getStates() {
		return states;
	}

	public abstract void updateStates();
}
