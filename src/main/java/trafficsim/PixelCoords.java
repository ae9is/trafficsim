/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package trafficsim;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import util.Log;

/**
 * Convert longitude/latitude coords into pixel coords.
 */
public class PixelCoords {
	private int panX = 0;
	private int panY = 0;
	private int yFudge = 0;
	private int xFudge = 0;
	private double scaleFudge = 1.0;
	private File dataFile;
	private int width = 1024;
	private int height = 1024;
	private OsmParser parser;

	public void setup(String dataFilePath) throws IOException {
		dataFile = new File(dataFilePath);
		if (!(dataFile.exists() && dataFile.canRead())) {
			throw new IOException("Missing or cannot read OSM xml data file at: " + dataFile.getAbsolutePath());
		}
		// parse data file
		parser = new OsmParser(dataFile);
		long start = System.currentTimeMillis();
		parser.parse();
		long stop = System.currentTimeMillis();
		double time = (stop - start) / 1000.0;
		Log.debug("Finished parsing " + dataFile.getAbsolutePath() + "; operation took " +
				time + " s.");
	}

	public OsmParser getParser() {
		return parser;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setSize(Dimension d) {
		this.width = d.width;
		this.height = d.height;
	}

	// Notes:
	// - Could be off by quite a bit, since it doesn't respect the actual projection being used. It just uses a linear estimation.
	// (Although, for mercator I believe this might actually be valid since it is nearly conformal.)
	// - Will miserably fail at the +/-180 degrees longitude line where max lon is negative and min lon is positive.
	// - graphics (0,0) == top left of window. therefore flip y pixel coords, replacing pxY with (height - pxY).
	public int[] get(double lon, double lat) throws Exception {
		if (parser == null) {
			throw new Exception("Cannot get pixel coords from lon/lat before running parser on a data file!");
		}
		Bounds b = parser.getBounds();
		if (b == null) {
			throw new Exception("Cannot get pixel coords from lon/lat since data file had no bounds!");
		}
		// Convert WGS84 lon/lat bounds to internet maps style mercator bounds.
		// NOTE: this works because the conversions between WGS84/mercator for lon/x and lat/y are not coupled.
		double[] minbounds = ProjectionConverter.lonlat2merc(b.minlon, b.minlat); // lon = 0, lat = 1
		double[] maxbounds = ProjectionConverter.lonlat2merc(b.maxlon, b.maxlat);
		double[] coords = ProjectionConverter.lonlat2merc(lon, lat);
		// Find mercator difference between edges of map
		double deltaX = Math.abs(maxbounds[0] - minbounds[0]);
		double deltaY = Math.abs(maxbounds[1] - minbounds[1]);
		// Calculate pixel x, y positions corresponding to mercator style x, y coords
		int xPx = panX + (int) Math.round((coords[0] - minbounds[0]) / deltaX * width) - xFudge;
		int yPx = panY + (int) (height * scaleFudge)
				- (int) Math.round((coords[1] - minbounds[1]) / deltaY * height * scaleFudge) - yFudge;
		return new int[] { xPx, yPx };
	}

	public void setPan(int panX, int panY) {
		this.panX = panX;
		this.panY = panY;
	}

	public void setPanX(int panX) {
		this.panX = panX;
	}

	public void setPanY(int panY) {
		this.panY = panY;
	}

	public void setXFudge(int xFudge) {
		this.xFudge = xFudge;
	}

	public void setYFudge(int yFudge) {
		this.yFudge = yFudge;
	}

	public void setScaleFudge(double scaleFudge) {
		this.scaleFudge = scaleFudge;
	}
}
