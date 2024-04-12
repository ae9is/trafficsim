/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package trafficsim;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;

import util.Log;


/**
 * Scroll pane that allows panning and zooming.
 */
public class ZoomPanScrollPane extends JScrollPane {

	private Integer mouseStartX, mouseStartY;
	private Point viewStart;
	private double scale = 1.0;  //absolute scale
	private JLayeredPane layeredPane;

	ZoomPanScrollPane(JLayeredPane layeredPane) {
		super(layeredPane);
		this.layeredPane = layeredPane;
		// note: add all listeners to scrollpane to get zoom + pan at same time
		this.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent mwe) {
				updatePreferredSize(mwe.getWheelRotation(), mwe.getPoint());
			}
		});
		this.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent me) {
				scrollPaneMouseDragged(me);
			}

			@Override
			public void mouseMoved(MouseEvent me) {
			}
		});
		this.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent me) {
				scrollPaneMouseClicked(me);
			}

			@Override
			public void mousePressed(MouseEvent me) {
				scrollPaneMousePressed(me);
			}

			@Override
			public void mouseReleased(MouseEvent me) {
				scrollPaneMouseReleased(me);
			}

			@Override
			public void mouseEntered(MouseEvent me) {
			}

			@Override
			public void mouseExited(MouseEvent me) {
			}
		});
	}

	// mouse wheel listener function
	private void updatePreferredSize(int n, Point p) {
		double d = (double) (n * 1.08); // scale factor for this transformation
		d = (n > 0) ? 1 / d : -d;
		double scale = this.getScale() * d;
		this.setScale(scale);
		Log.debug("Layered pane scale = " + this.getScale());
		// Reference window size is that of the scrollpane.
		int w = (int) (scale * getWidth());
		int h = (int) (scale * getHeight());
		Dimension size = new Dimension(w, h);
		layeredPane.setPreferredSize(size);
		layeredPane.setMinimumSize(size);
		layeredPane.setMaximumSize(size);
		layeredPane.setSize(size);
		int offX = (int) (p.x * d) - p.x;
		int offY = (int) (p.y * d) - p.y;
		layeredPane.setLocation(layeredPane.getLocation().x - offX, layeredPane.getLocation().y - offY);
	}

	// mouse listener functions
	public void scrollPaneMouseClicked(MouseEvent e) {
	}

	public void scrollPaneMousePressed(MouseEvent e) {
		setStartPosition(e);
		Log.debug("Mouse pressed: x/y = " + e.getX() + "/" + e.getY());
	}

	public void scrollPaneMouseDragged(MouseEvent e) {
		updatePosition(e);
		Log.debug("Mouse dragging: x/y = " + e.getX() + "/" + e.getY());
	}

	public void scrollPaneMouseReleased(MouseEvent e) {
		reset();
		Log.debug("Mouse released: x/y = " + e.getX() + "/" + e.getY());
	}

	// pan the viewport
	private void updatePosition(MouseEvent e) {
		if (mouseStartX != null && mouseStartY != null) {
			int x = e.getX();
			int y = e.getY();
			int viewStartX = (int) viewStart.getX();
			int viewStartY = (int) viewStart.getY();
			this.getViewport().setViewPosition(new Point(
					viewStartX + (mouseStartX - x),
					viewStartY + (mouseStartY - y)));
			this.doLayout();
		}
	}

	private void reset() {
		mouseStartX = null;
		mouseStartY = null;
	}

	private void setStartPosition(MouseEvent e) {
		mouseStartX = e.getX();
		mouseStartY = e.getY();
		viewStart = this.getViewport().getViewPosition();
	}

	public double getScale() {
		return scale;
	}

	public void setScale(double scale) {
		this.scale = scale;
	}
}
