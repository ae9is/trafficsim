/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package trafficsim;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.TreeSet;
import javax.swing.JPanel;

import util.Log;

/**
 * Overlays background ImagePanel. Contains symbols representing vehicles and traffic lights.
 */
public class DrawingPanel extends JPanel {

	private double scale = 1.0; // absolute scale
	private TreeSet<DrawingObject> objects = new TreeSet<>();
	private PixelCoords pixelCoords;

	public DrawingPanel(PixelCoords pixelCoords) {
		// Always transparent
		this.setOpaque(false);
		this.pixelCoords = pixelCoords;

		this.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent me) {
				selectDrawableObject(me);
			}

			@Override
			public void mousePressed(MouseEvent me) {
			}

			@Override
			public void mouseReleased(MouseEvent me) {
			}

			@Override
			public void mouseEntered(MouseEvent me) {
			}

			@Override
			public void mouseExited(MouseEvent me) {
			}
		});
	}

	// "select" and display info about the drawable drawing panel object
	//   below the mouse cursor, if any.
	private void selectDrawableObject(MouseEvent e) {
		for (DrawingObject object : objects) {
			// Mouse position must be within object bounds
			Point pos = object.getPanelPos(getGraphics(), scale, pixelCoords);
			Dimension dim = object.getSize(scale);
			// Check that drawing object has methods for selection implemented
			if (pos != null && dim != null) {
				int dx = Math.abs(pos.x - e.getX());
				int dy = Math.abs(pos.y - e.getY());
				// Check that mouse cursor position over object
				if ((dx <= dim.getWidth() * 0.5 * scale) && (dy <= dim.getHeight() * 0.5 * scale)) {
					Log.debug("Cursor: scrollpane: " + e.getX() + "," + e.getY());
					Log.debug("Object: drawingpane: " + pos.x + "," + pos.y);
					// What is drawn contained in object
					object.setSelected(true);
					continue;
				}
			}
			object.setSelected(false); // only get here if not selected
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		// Scale by multiplying by width, height or by scale factor from parent
		int w = this.getParent().getWidth();
		int h = this.getParent().getHeight();
		scale = ((ZoomPanScrollPane) this.getParent().getParent().getParent()).getScale();
		// Update width, height stored in pixelCoords
		Dimension dim = new Dimension((int) (scale * w), (int) (scale * h));
		setSize(dim);
		if (pixelCoords != null) {
			pixelCoords.setSize(dim);
		}

		// Paint all the objects...
		for (DrawingObject object : objects) {
			object.draw(g, scale, pixelCoords);
		}
		// Paint labels on top of objects
		for (DrawingObject object : objects) {
			object.drawLabels(g, scale, pixelCoords);
		}
		// Paint select info box on top of everything
		for (DrawingObject object : objects) {
			object.drawInfoBox(g, scale, pixelCoords);
		}
	}

	public double getScale() {
		return scale;
	}

	public void clear() {
		objects = new TreeSet<>();
	}

	public TreeSet<DrawingObject> getDrawingObjects() {
		return objects;
	}

	public void setDrawingObjects(Collection<DrawingObject> objects) {
		this.objects = (TreeSet<DrawingObject>) objects;
	}

	public void addDrawingObject(DrawingObject object) {
		objects.add(object);
	}

	public void addDrawingObjects(Collection<DrawingObject> objects) {
		this.objects.addAll(objects);
	}

	public void setPixelCoords(PixelCoords pixelCoords) {
		this.pixelCoords = pixelCoords;
	}

	public PixelCoords getPixelCoords() {
		return pixelCoords;
	}
}
