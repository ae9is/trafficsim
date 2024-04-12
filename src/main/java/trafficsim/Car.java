/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package trafficsim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;

import util.Log;

/**
 * 
 */
@SuppressWarnings("ComparableType")
public class Car extends IntelligentDriverModel implements DrawingObject, Comparable<Object>, Serializable {
	final Color defaultColor = Color.RED;
	final Color finishColor = Color.BLACK;
	final Color selectedColor = Color.MAGENTA;
	final int width = 10; // For a more realistic size, use 3 - 5 px
	final int height = 10;
	private Color prevColor;
	private Color color;
	private Double lon;
	private Double lat;
	private Long id;
	private ArrayList<Node> routeNodes;
	private int nextNodeCounter = 0;
	private Way currentWay; // That the car is travelling on
	private Integer wayDirection; // Direction car is travelling along way
	private double velocity = 0; // Along way
	private boolean selected = false;
	private DecimalFormat df;

	// Parameters describing how quickly the driver accelerates, see getAcceleration().
	private static final double AVG_ACC_ALPHA = 1.74; // m/s^2
	private static final double AVG_ACC_BETA = 0.0995;
	private static final double AVG_DEC_ALPHA = 3.23; // m/s^2
	private static final double AVG_DEC_BETA = 0.165;

	public static final double ROLLING_STOP_SPEED = 1.0; // m/s
	public static final double STOP_DIST = 2.0; // m From intersection / stop sign
	public static final double SLOWEST_STOP_ACC = -0.5; // m/s^2 Slowest car allowed to brake to intersection
	                                                 // Limit slow brakes so car doesn't slowly coast to halt.

	public Car(Double lon, Double lat) {
		// Set position
		id = DMV.getNewId();
		this.lon = lon;
		this.lat = lat;
		// Set traffic model parameters
		v0 = 120 / 3.6; // m/s, Desired speed
		delta = 4.0; // Free acceleration exponent
		T = 1.5; // s, Desired time gap
		s0 = 2.0; // m, Jam distance
		a = 1.4; // m/s^2, Maximum acceleration
		b = 2.0; // m/s^2, Desired deceleration
		length = 5.0; // Full size car length ~ 5 m
		color = defaultColor;
		prevColor = color;
		df = new DecimalFormat("0.000");
	}

	public Double getLon() {
		return lon;
	}

	public Double getLat() {
		return lat;
	}

	public Long getId() {
		return id;
	}

	public Integer getWayDirection() {
		return wayDirection;
	}

	public Node getDestNode() {
		return (routeNodes == null || routeNodes.isEmpty()) ? null : routeNodes.get(routeNodes.size() - 1);
	}

	public Node getNextNextNode() {
		return (routeNodes == null || routeNodes.isEmpty() ||
				nextNodeCounter >= routeNodes.size() - 2) ? null : routeNodes.get(nextNodeCounter + 1);
	}

	public Node getNextNode() {
		return (routeNodes == null || routeNodes.isEmpty() ||
				nextNodeCounter >= routeNodes.size() - 1) ? null : routeNodes.get(nextNodeCounter);
	}

	public Node getPrevNode() {
		return (routeNodes == null || routeNodes.isEmpty() ||
				nextNodeCounter == 0) ? null : routeNodes.get(nextNodeCounter - 1);
	}

	public ArrayList<Node> getRouteNodes() {
		return routeNodes;
	}

	public Way getCurrentWay() {
		return currentWay;
	}

	public double getVelocity() {
		return velocity;
	}

	public double getVelocityKm() {
		return velocity * 3.6;
	}

	public Car getLeader() {
		Car lead = null;
		if (currentWay != null && wayDirection != null) {
			// Get list of cars in lane in order by position
			LinkedList<Car> cars = currentWay.getLane(wayDirection).getCars();
			if (cars.size() > 1) {
				int i = cars.indexOf(this); // -1 if not in list
				if (i > 0) { // The first car in the way is at index 0 and has no leader
					lead = cars.get(i - 1);
				}
			}
		}
		return lead;
	}

	// Gets new car acceleration (using current lane's immediately leading car, if any exists)
	public double getAccIdm() {
		Car lead = getLeader();
		Double s = null;
		Double dv = null;
		if (lead != null) {
			double dBetween = ProjectionConverter.getDistBetweenPoints(
					lon, lat, lead.getLon(), lead.getLat());
			s = dBetween - 0.5 * length - 0.5 * lead.getLength();
			if (s < 0) {
				s = 0.0; // In case cars end up too close, perhaps because of coordinate convertion to distance
			}
			dv = velocity - lead.getVelocity();
		}
		return a(s, velocity, dv);
	}

	// Always positive
	@Deprecated
	public double getAcceleration() {
		// From http://www.dot.state.fl.us/rail/Publications/Studies/Safety/AccelerationResearch.pdf
		// Table 2, Motorist observations
		// Average driver: alpha = 1.74, beta = 0.0995
		// Model is: a = alpha - beta*v, for v < alpha/beta else 0.
		// ** Note: scale current speed by (alpha/beta) / maxspeed == model vmax / way vmax **
		double maxspeed = this.getCurrentWay().getMaxspeedM();
		double scale = (AVG_ACC_ALPHA / AVG_ACC_BETA) / maxspeed;
		double a = AVG_ACC_ALPHA - AVG_ACC_BETA * scale * velocity;
		return (a < 0) ? 0 : a;
	}

	// Always negative
	@Deprecated
	public double getDeceleration() {
		// Similar to getAcceleration. also uses adjusted model.
		// Table 2, Decelerations from Traffic Observations, Beakey
		double maxspeed = this.getCurrentWay().getMaxspeedM();
		double scale = (AVG_DEC_ALPHA / AVG_DEC_BETA) / maxspeed;
		double a = AVG_DEC_ALPHA - AVG_DEC_BETA * scale * velocity;
		return (a < 0) ? 0 : -a;
	}

	// Always negative
	// Say 0.9 to 1g max for passenger vehicles, good weather/pavement.
	// TODO Modify based on other factors (car type/weight, weather, road type...)
	public double getMaxDeceleration() {
		return -0.9 * 9.8; // m/s^2
	}

	public double getLength() {
		return length;
	}

	public void setLon(Double lon) {
		this.lon = lon;
	}

	public void setLat(Double lat) {
		this.lat = lat;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setCurrentWay(Way currentWay) {
		this.currentWay = currentWay;
		// Update v0 = desired velocity to reflect current way
		v0 = this.currentWay.getMaxspeedM(); // +10/3.6
		// Update car direction along way.
		// Way's ordering of nodes in data file is direction 0, opposite ordering is direction 1.
		// Take car's next and next next nodes because previous cannot be on way!
		// If car doesn't have 2 nodes on way it's at end of route or going off-road
		Node next = getNextNode();
		Node nextnext = getNextNextNode();
		if (next != null && nextnext != null) {
			int n = this.currentWay.getNodes().indexOf(next); // Fail if next not in way: undefined behaviour!
			int nn = this.currentWay.getNodes().indexOf(nextnext); // next next could legitimately be off of way
			if (nn != -1) {
				if (nn > n) {
					wayDirection = 0; // Ordering in data file
				} else {
					wayDirection = 1;
				}
			} else {
				Log.warning("Car " + this.getId() + " does not have 2 nodes on way " + currentWay.getId() + "? ..."
						+ "Leaving wayDirection as 0");
			}
		} else {
			Log.warning("Car.setCurrentWay() should not be called if getNextNextNode() == null");
			wayDirection = null;
		}
	}

	public void setVelocity(double velocity) {
		this.velocity = velocity;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public boolean setRoute(RoutePlanner planner, Node dest) {
		Node nearest = planner.getNearestNode(lon, lat, 5); // More than 5 degrees from a node forget it
		return setRoute(planner, nearest, dest);
	}

	public boolean setRoute(RoutePlanner planner, Node start, Node dest) {
		routeNodes = planner.getNewRoute(start, dest);
		nextNodeCounter = 0;
		Node nextNode = getNextNode();
		if (nextNode != null) {
			// Assign to any way (must be road) containing the first + second nodes.
			//  Should be fixed by way changing anyway.
			ArrayList<Node> nextNodes = new ArrayList<>();
			nextNodes.add(this.getNextNode());
			Node nextnext = this.getNextNextNode();
			if (nextnext != null) {
				nextNodes.add(nextnext);
			}
			setCurrentWay(planner.getFirstRoadContaining(nextNodes));
			currentWay.insert(this);
			return true;
		}
		return false;
	}

	public void nextNode() {
		// Change the cars next "target" node to the next one in the list
		int oldnncount = nextNodeCounter;
		nextNodeCounter++;
		if (oldnncount == nextNodeCounter) {
			Log.error("int++ failed!");
		}
		if (nextNodeCounter >= routeNodes.size() - 1) {
			// Reached last node.
			// This should be last time this method is called (before new route acquired).
			nextNodeCounter = routeNodes.size() - 1;
			prevColor = finishColor; // Stay finishing colour always!
			color = finishColor;
			// Take car off road so it doesn't block others behind it
			if (currentWay == null) {
				Log.warning("Cannot remove car from null way, car is offroad");
			} else {
				currentWay.remove(this);
				currentWay = null;
			}
		}
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		prevColor = this.color;
		this.color = color;
	}

	@Override
	public Dimension getSize(double scale) {
		return new Dimension((int) (width * scale), (int) (height * scale));
	}

	@Override
	public void setSelected(boolean selected) {
		this.selected = selected;
		if (selected) {
			if (color != selectedColor) {
				prevColor = color;
				color = selectedColor;
			}
			return;
		}
		color = prevColor;
	}

	@Override
	public Point getPanelPos(Graphics g, double scale, PixelCoords pixelCoords) {
		Point p = null;
		try {
			int[] coords = pixelCoords.get(lon, lat);
			// p = new Point((int)(coords[0]*scale - width*0.5), (int)(coords[1]*scale - height*0.5));
			p = new Point((int) (coords[0] * scale), (int) (coords[1] * scale));
		} catch (Exception ex) {
			Log.error("Could not get drawing panel position of car: " + id + " at [" + lon + "," + lat + "]");
		}
		return p;
	}

	@Override
	public void draw(Graphics g, double scale, PixelCoords pixelCoords) {
		try {
			int[] coords = pixelCoords.get(lon, lat);
			g.setColor(color);
			int xPos = (int) (coords[0] * scale - width * 0.5);
			int yPos = (int) (coords[1] * scale - height * 0.5);
			g.fillOval(xPos, yPos, (int) (width * scale), (int) (height * scale));
		} catch (Exception ex) {
			Log.error("Could not draw car: " + id + " at [" + lon + "," + lat + "]");
		}
	}

	@Override
	public void drawLabels(Graphics g, double scale, PixelCoords pixelCoords) {
		try {
			int[] coords = pixelCoords.get(lon, lat);
			g.setColor(color);
			int xPos = (int) (coords[0] * scale - width * 0.5);
			int yPos = (int) (coords[1] * scale - height * 0.5);
			// Print id of car; position is left-most position of text
			int margin = 2;
			g.drawString(this.getId().toString(), xPos - margin, yPos - margin);
		} catch (Exception ex) {
			Log.error("Could not draw car label for car: " + id + " at [" + lon + "," + lat + "]");
		}
	}

	@Override
	public void drawInfoBox(Graphics g, double scale, PixelCoords pixelCoords) {
		// If selected also show additional pop-up box with extra info
		if (selected) {
			try {
				int[] coords = pixelCoords.get(lon, lat);
				g.setColor(color);
				int xPos = (int) (coords[0] * scale - width * 0.5);
				int yPos = (int) (coords[1] * scale - height * 0.5);
				int textboxWidth = 150;
				int textboxHeight = 80;
				int margin = 2;
				int boxMargin = 5;
				g.setColor(Color.LIGHT_GRAY);
				g.fillRect(xPos + width, yPos - height - textboxHeight - boxMargin, textboxWidth, textboxHeight);
				g.setColor(Color.BLACK);
				g.drawRect(xPos + width, yPos - height - textboxHeight - boxMargin, textboxWidth, textboxHeight);
				String nextNodeId = (this.getNextNode() == null) ? "N/A" : String.valueOf(this.getNextNode().getId());
				String[] infoArray = new String[] {
						"id = " + this.getId(),
						"v = " + df.format(this.getVelocityKm()) + " km/h",
						"[ " + df.format(this.getLon()) + "; " + df.format(this.getLat()) + " ]",
						"next = " + nextNodeId
				};
				int i = 1;
				int spacing = g.getFontMetrics().getHeight() + margin;
				for (String info : infoArray) {
					g.drawString(info, xPos + width + boxMargin, yPos - height - textboxHeight + i * spacing);
					i++;
				}
			} catch (Exception ex) {
				Log.error("Could not draw car info box for car: " + id + " at [" + lon + "," + lat + "]");
			}
		}
	}

	@Override
	public int compareTo(Object o) {
		if (!(o instanceof Car)) {
			return id.toString().compareTo(o.toString());
		}
		final Car car = (Car) o;
		return id.compareTo(car.getId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Car)) {
			return false;
		}
		final Car car = (Car) o;
		if (id != null ? !id.equals(car.id) : car.id != null) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return (id != null ? id.hashCode() : 0);
	}
}
