/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package trafficsim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;

/**
 *  
 */
public interface DrawingObject {
	public abstract Color getColor();
	public abstract void setColor(Color color);
	public abstract void draw(Graphics g, double scale, PixelCoords pixelCoords);  //implements how the drawing object is drawn
	public abstract void drawLabels(Graphics g, double scale, PixelCoords pixelCoords);  //implements labels for object
	public abstract void drawInfoBox(Graphics g, double scale, PixelCoords pixelCoords);  //extra info box on selection drawn over everything
	public abstract void setSelected(boolean selected);
	public abstract Point getPanelPos(Graphics g, double scale, PixelCoords pixelCoords);
	public abstract Dimension getSize(double scale);
}
