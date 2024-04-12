/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package trafficsim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.text.DecimalFormat;
import java.util.ArrayList;

import util.Log;

/**
 *  
 */
public class Node extends OsmType implements DrawingObject {
	final Color defaultColor = Color.GRAY;
	final Color selectedColor = Color.MAGENTA;
	Color color = defaultColor;
	Color initColor = color; // needed to store random way colour assigned to node
	final int width = 4; // px
	final int height = 4; // px
	private Double lat;
	private Double lon;
	private Intersection intersection;
	private ArrayList<Long> wayRefs = new ArrayList<>();
	private boolean selected = false;
	private DecimalFormat df;

	public Node(Long id, Double lat, Double lon) {
		this.setId(id);
		this.lat = lat;
		this.lon = lon;
		df = new DecimalFormat("0.000");
	}
	
	public Double getLat() {
		return lat;
	}
	public Double getLon() {
		return lon;
	}
	public Intersection getIntersection() {
		return intersection;
	}
	public ArrayList<Long> getWayRefs() {
		return wayRefs;
	}
	public void setLat(Double lat) {
		this.lat = lat;
	}
	public void setLon(Double lon) {
		this.lon = lon;
	}
	public void setIntersection(Intersection intersection) {
		this.intersection = intersection;
	}
	public void addWayRef(long wayId) {
		wayRefs.add(wayId);
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		this.initColor = color;
		this.color = color;
	}
	
	@Override
	public Dimension getSize(double scale) {
		return new Dimension((int)(width*scale), (int)(height*scale));
	}
	
	@Override
	public Point getPanelPos(Graphics g, double scale, PixelCoords pixelCoords) {
		Point p = null;
		try {
			int[] coords = pixelCoords.get(lon, lat);
			p = new Point((int)(coords[0]*scale), (int)(coords[1]*scale));
		} catch (Exception ex) {
			Log.error("Could not get drawing panel position of node: " + super.getId()
					+ " at [" + lon + "," + lat + "]");
		}
		return p;
	}

	@Override
	public void draw(Graphics g, double scale, PixelCoords pixelCoords) {
		try {
			int[] coords = pixelCoords.get(lon, lat);
			g.setColor(color);
			int xPos = (int)(coords[0]*scale - width*0.5);
			int yPos = (int)(coords[1]*scale - height*0.5);
			g.fillOval(xPos, yPos, (int)(width*scale), (int)(height*scale));
		} catch (Exception ex) {
			Log.error("Could not draw node: " + this.getId() + " at [" + lon + "," + lat + "]");
		}
	}

	@Override
	public void drawLabels(Graphics g, double scale, PixelCoords pixelCoords) {
		if (selected) {
			try {
				int[] coords = pixelCoords.get(lon, lat);
				g.setColor(color);
				int xPos = (int)(coords[0]*scale - width*0.5);
				int yPos = (int)(coords[1]*scale - height*0.5);
				// print id of node; position is left-most position of text
				int margin = 2;
				g.drawString(this.getId().toString(), xPos - margin, yPos - margin);
			} catch (Exception ex) {
				Log.error("Could not draw label for node: " + this.getId() + " at [" + lon + "," + lat + "]");
			}
		}
	}

	@Override
	public void drawInfoBox(Graphics g, double scale, PixelCoords pixelCoords) {
		// if selected also show additional pop-up box with extra info
		if (selected) {
			try {
				int[] coords = pixelCoords.get(lon, lat);
				g.setColor(color);
				int xPos = (int)(coords[0]*scale - width*0.5) - 50;
				int yPos = (int) (coords[1] * scale - height * 0.5) - 100; // put out of way so can see traffic passing through
				int textboxWidth = 150;
				int textboxHeight = 80;
				int margin = 2;
				int boxMargin = 5;
				g.setColor(Color.LIGHT_GRAY);
				g.fillRect(xPos + width, yPos - height - textboxHeight - boxMargin, textboxWidth, textboxHeight);
				g.setColor(Color.BLACK);
				g.drawRect(xPos + width, yPos - height - textboxHeight - boxMargin, textboxWidth, textboxHeight);
				String intersectionTimes = "";
				String intersectionStates = "";
				if (intersection == null) {
					intersectionTimes = "no intersection";
					intersectionStates = "(no state)";
				} else if (intersection.getType() == Intersection.ALL_WAY_STOP) {
					intersectionTimes = "4-way stop";
					intersectionStates = "(no state)";
				} else {
					for (Node node : intersection.getApproachNodes()) {
						int state = intersection.getState(node);
						String stateStr = "";
						if (state == Intersection.GREEN) {
							stateStr = "G";
						} else if (state == Intersection.YELLOW) {
							stateStr = "Y";
						} else if (state == Intersection.RED) {
							stateStr = "R";
						} else {
							// programmer error: forgot to put in left/right turn green or something
							Log.error("Light state is not one of G/Y/R!");
							System.exit(1);
						}
						intersectionStates += node.getId() + " : " + stateStr + ", ";
					}
					for (NodePair pair : intersection.getPeriods().keySet()) {
						Double period = intersection.getPeriods().get(pair);
						intersectionTimes += pair.toString() + " : " + df.format(period) + ", ";
					}
				}
				String[] infoArray = new String[] {
					"id = " + this.getId(),
					"S: " + intersectionStates,
					"T: " + intersectionTimes
				};
				int i = 1;
				int spacing = g.getFontMetrics().getHeight() + margin;
				for (String info : infoArray) {
					g.drawString(info, xPos + width + boxMargin, yPos - height - textboxHeight + i*spacing);
					i++;
				}
			} catch (Exception ex) {
				Log.error("Could not draw node info box for node: " + this.getId() + " at [" + lon + "," + lat + "]");
			}
		}
	}

	@Override
    public boolean equals(Object o) {
	    if (this == o) {
			return true;
		}
        if (!(o instanceof Node)) {
			return false;
		}
        final Node node = (Node) o;
        if (this.getId() != null ? !this.getId().equals(node.getId()) : node.getId() != null) {
			return false;
		}
        return true;
    }
	
    @Override
    public int hashCode() {
        return (this.getId() != null ? this.getId().hashCode() : 0);
    }

	@Override
	public void setSelected(boolean selected) {
		this.selected = selected;
		if (!selected) {
			if (initColor != null) {
				color = initColor;
			} else {
				color = defaultColor;
			}
			return;
		}
		color = selectedColor;
	}
}
