/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package trafficsim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import util.Log;

/**
 *  
 */
public class Way extends OsmType implements DrawingObject {
	private boolean isRoad = false;
	final Color defaultColor;
	Color color;
	private Boolean visible;
	private ArrayList<Long> nodeRefs = new ArrayList<>(); // ordered list
	private LinkedList<Node> nodes = new LinkedList<>(); // ordered list
	private ArrayList<Car> cars = new ArrayList<>(); // transiting cars. not in any order.
	private double maxspeed = 10; // km/hr. set low on purpose for debugging.
																// this field guessed later based on road type.
																// for true max speed see if maxspeed tag exists.
	private HashMap<Integer, Lane> lanes = new HashMap<>(); // lane direction, only 1 lane until switching model in place

	public Way(Long id, Boolean visible) {
		this.setId(id);
		this.visible = visible;
		defaultColor = new Color((int) (255 * Math.random()), (int) (255 * Math.random()), (int) (255 * Math.random()));
		this.doSetColor(defaultColor);
		// TODO add # of lanes based on "lane" tag, and add in lane-switching model
		this.lanes.put(0, new Lane(0));
		this.lanes.put(1, new Lane(1));
	}

	// one-way ways will only have one direction: 0
	// TODO multiple lanes per direction
	public Lane getLane(int direction) {
		return lanes.get(direction);
	}

	public Boolean getVisible() {
		return visible;
	}

	public boolean isOneway() {
		String oneway = this.getTag("oneway");
		if (oneway != null && oneway.equals("yes")) {
			return true;
		}
		return false;
	}

	public ArrayList<Long> getNodeRefs() {
		return nodeRefs;
	}

	public LinkedList<Node> getNodes() {
		return nodes;
	}

	public Node getLastNode() {
		return (nodes == null) ? null : nodes.getLast();
	}

	public Node getFirstNode() {
		return (nodes == null) ? null : nodes.getFirst();
	}

	public boolean isPopulated() {
		return (cars == null || cars.isEmpty()) ? false : true;
	}

	public ArrayList<Car> getCars() {
		return cars;
	}

	public double getMaxspeedKm() {
		return maxspeed;
	}

	public double getMaxspeedM() {
		return maxspeed / 3.6;
	}

	public void setNodeRefs(ArrayList<Long> nodeRefs) {
		this.nodeRefs = nodeRefs;
	}

	public void addNodeRef(Long nodeRef) {
		nodeRefs.add(nodeRef);
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	public void addNode(Node node) {
		nodes.add(node);
	}

	public void setMaxspeedKm(double maxspeed) {
		this.maxspeed = maxspeed;
	}

	public void insert(Car car) {
		cars.add(car);
		Integer wayDirection = car.getWayDirection();
		if (wayDirection != null) {
			Lane lane = lanes.get(wayDirection);
			lane.insert(car);
			if (!lane.getCars().contains(car)) {
				Log.error("Could not insert car into lane");
			}
		} else {
			// Car going offroad or done after next node so just assign to no lane.
			Log.warning("Car has no way direction, not inserted into lane");
		}
	}

	public void remove(Car car) {
		// Remove car from way's lanes, if it's in one
		Integer wayDirection = car.getWayDirection();
		if (wayDirection != null) {
			Lane lane = lanes.get(wayDirection);
			lane.remove(car);
		}
		// Might not be able to rely on address of car object due to iteration over copies in step().
		// Therefore remove based on id.
		boolean hadCar = false;
		for (Iterator<Car> iter = cars.iterator(); iter.hasNext();) {
			Car c = iter.next();
			if (c.getId().longValue() == car.getId().longValue()) {
				hadCar = true;
				iter.remove();
			}
		}
		if (!hadCar) {
			Log.error("Called Way.remove(car) when car wasn't in way!");
		}
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		this.doSetColor(color);
	}

	private void doSetColor(Color color) {
		this.color = color;
		for (Node node : nodes) {
			node.setColor(color);
		}
	}

	@Override
	public Point getPanelPos(Graphics g, double scale, PixelCoords pixelCoords) {
		throw new UnsupportedOperationException("Way.getPanelPos() not implemented");
	}

	@Override
	public Dimension getSize(double scale) {
		return null;
	}

	@Override
	public void draw(Graphics g, double scale, PixelCoords pixelCoords) {
		try {
			int[] coords = pixelCoords.get(nodes.get(0).getLon(), nodes.get(0).getLat());
			for (int i = 0; i < nodes.size(); i++) {
				// Draw out node in way
				g.setColor(color);
				// Connect to next node with a line
				if (i < nodes.size() - 1) {
					Node next = nodes.get(i + 1);
					int[] nextCoords = pixelCoords.get(next.getLon(), next.getLat());
					g.drawLine((int) (coords[0] * scale), (int) (coords[1] * scale), (int) (nextCoords[0] * scale),
							(int) (nextCoords[1] * scale));
					coords = nextCoords; // Save for next iteration
				}
			}
		} catch (Exception ex) {
			Log.error("Couldn't get coords for at least one node in Way.draw()");
		}
	}

	@Override
	public void drawLabels(Graphics g, double scale, PixelCoords pixelCoords) {
	}

	@Override
	public void drawInfoBox(Graphics g, double scale, PixelCoords pixelCoords) {
	}

	@Override
	public void setSelected(boolean selected) {
		throw new UnsupportedOperationException("Way.setSelected() not implemented");
	}

	public void setRoad() {
		isRoad = true;
	}

	public boolean isRoad() {
		return isRoad;
	}
}
