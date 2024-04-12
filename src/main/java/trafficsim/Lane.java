/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package trafficsim;

import java.util.Iterator;
import java.util.LinkedList;

import util.Log;

/**
 *  
 */
public class Lane {
	private LinkedList<Car> cars = new LinkedList<>(); // Transiting cars, in order
	private int direction; // 0 or 1. No geographical meaning. Convenience variable only since stored in way in hashmap.

	public Lane(int direction) {
		this.direction = direction;
	}

	public int getDirection() {
		return direction;
	}

	public boolean isPopulated() {
		return (cars == null || cars.isEmpty()) ? false : true;
	}

	public LinkedList<Car> getCars() {
		return cars;
	}

	// Inserts the car into the lane's list of cars at the right point according to its position
	public void insert(Car car) {
		// If no cars in lane then just add the car to the list
		if (cars == null) {
			cars = new LinkedList<>();
		}
		if (cars.isEmpty()) {
			cars.add(car);
			return;
		}
		// Else get closest car to this in the list. approximate squares of change in lon/lat as distance here.
		int i;
		int closestIndex = 0;
		Car closest = cars.get(0);
		for (i = 1; i < cars.size(); i++) {
			Car laneCar = cars.get(i);
			double dLonClosest = closest.getLon() - car.getLon();
			double dLatClosest = closest.getLat() - car.getLat();
			double dLonLane = laneCar.getLon() - car.getLon();
			double dLatLane = laneCar.getLat() - car.getLat();
			double dClosest = dLonClosest * dLonClosest + dLatClosest * dLatClosest;
			double dLane = dLonLane * dLonLane + dLatLane * dLatLane;
			if (dLane < dClosest) {
				closest = laneCar;
				closestIndex = i;
			}
		}
		// Is car's next node also closest's next node?
		if (car.getNextNode().equals(closest.getNextNode())) {
			// Insert car before closest
			cars.add(closestIndex, car);
		} else {
			// Insert after
			cars.add(closestIndex + 1, car);
		}
		if (!cars.contains(car)) {
			Log.debug("Could not add car " + car.getId() + " to way " + car.getCurrentWay().getId() + " direction "
					+ this.getDirection());
		}
	}

	public void remove(Car car) {
		boolean hadCar = false;
		for (Iterator<Car> iter = cars.iterator(); iter.hasNext();) {
			Car c = iter.next();
			if (c.getId().longValue() == car.getId().longValue()) {
				hadCar = true;
				iter.remove();
			}
		}
		if (!hadCar) {
			Log.error("Called Lane.remove(car) when car wasn't in lane!");
		}
	}
}
