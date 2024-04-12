/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package trafficsim;

/**
 * For microscopic traffic flow simulation. 
 * Will only be using car-following models (time-continuous) and not cellular automaton models.
 */
public interface TrafficFlowModel {

	/**
	 * Get vehicle acceleration. If s and dv null then assume vehicle is not following anything.
	 * @param s bumper to bumper space between vehicle and leading vehicle
	 * @param v vehicle velocity
	 * @param dv relative velocity of vehicle to leading, dv = v - v_lead
	 * @return new vehicle acceleration
	 */
	public abstract double a(Double s, double v, Double dv);
}
