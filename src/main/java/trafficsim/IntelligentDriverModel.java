/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package trafficsim;

/**
 *  
 */
public abstract class IntelligentDriverModel implements TrafficFlowModel {
	// parameters:
	double v0; // desired speed
	double delta; // free acceleration exponent
	double T; // desired time gap
	double s0; // jam distance
	double a; // maximum acceleration
	double b; // desired deceleration
	double length; // vehicle length for calculating s (bumper to bumper space)

	/**
	 * @param s  bumper to bumper space between vehicle and leading vehicle
	 * @param v  vehicle velocity
	 * @param dv relative velocity of vehicle to leading, dv = v - v_lead
	 * @return new vehicle acceleration
	 */
	@Override
	public double a(Double s, double v, Double dv) {
		double s_star;
		double dVbrake;
		if (s == null && dv == null) { // if not following any car
			dVbrake = 0;
		} else {
			s_star = s0 + v * T + 0.5 * v * dv / Math.sqrt(a * b); // desired (safe) gap
			dVbrake = -a * (s_star / s);
		}
		double dVfree = a * (1 - Math.pow(v / v0, delta));
		return (dVfree + dVbrake);
	}
}
