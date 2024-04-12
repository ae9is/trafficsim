/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package trafficsim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.Log;

/**
 * Converts projections in OSM data.osm file from GPS (WGS84, EPSG:4326) to Mercator (EPSG:3857).
 */
public class ProjectionConverter {

	File in;
	File out;
	boolean convertToWsg84;

	public ProjectionConverter(String inFile, String outFile, boolean convertToWsg84) {
		this.in = new File(inFile);
		this.out = new File(outFile);
		if (in == null || !in.exists() || !in.canRead()) {
			Log.error("Invalid OSM XML data file passed to OsmXmlProjectionConverter");
			return;
		}
		if (out == null || !out.canWrite()) {
			Log.error("Invalid output file name passed to OsmXmlProjectionConverter");
			return;
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(in));
			BufferedWriter bw = new BufferedWriter(new FileWriter(out));
			String boundsRegex = "^(.*?minlat=\")([-.0-9]+)(\" minlon=\")([-.0-9]+)(\" maxlat=\")([-.0-9]+)(\" maxlon=\")([-.0-9]+)(\".*)$";
			String regex = "^(.*?lat=\")([-.0-9]+)(\" lon=\")([-.0-9]+)(\".*)$";
			Pattern boundsP = Pattern.compile(boundsRegex);
			Pattern p = Pattern.compile(regex);
			while (br.ready()) {
				String line = br.readLine();
				Matcher m = p.matcher(line);
				if (m.matches()) {
					// group 0 = line, 1 = ... lat=", 2 = latitude, 3 = " lon=", 4 = longitude, 5 = " ...
					try {
						double lat = Double.parseDouble(m.group(2)); // y
						double lon = Double.parseDouble(m.group(4)); // x
						double[] coords;
						if (convertToWsg84) {
							coords = merc2lonlat(lon, lat); // returns x, y
						} else {
							coords = lonlat2merc(lon, lat); // returns lon, lat
						}
						String outline = m.group(1) + new BigDecimal(coords[1]).toPlainString() + m.group(3)
								+ new BigDecimal(coords[0]).toPlainString() + m.group(5);
						bw.write(outline);
						bw.newLine();
					} catch (NumberFormatException nfe) {
						Log.warning("Could not parse longitude/latitude for line: " + line);
						continue;
					}
				} else {
					m = boundsP.matcher(line);
					if (m.matches()) {
						try {
							String outline = m.group(1);
							// loop runs twice, once on each of lat/lon pairs
							for (int i = 0; i <= 4; i += 4) {
								double lat = Double.parseDouble(m.group(2 + i)); // y
								double lon = Double.parseDouble(m.group(4 + i)); // x
								double[] coords;
								if (convertToWsg84) {
									coords = merc2lonlat(lon, lat); // returns x, y
								} else {
									coords = lonlat2merc(lon, lat); // returns lon, lat
								}
								outline += new BigDecimal(coords[1]).toPlainString() + m.group(3 + i)
										+ new BigDecimal(coords[0]).toPlainString() + m.group(5 + i);
							}
							bw.write(outline);
							bw.newLine();
						} catch (NumberFormatException nfe) {
							Log.warning("Could not parse longitude/latitudes for bounds line: " + line);
							continue;
						}
					} else {
						// line without coords on it so print to file as is
						bw.write(line);
						bw.newLine();
					}
				}
			}
			bw.flush();
			bw.close();
			br.close();
		} catch (Exception e) {
			Log.error(e.toString());
		}
	}

	static final double EARTH_RADIUS = 6378137.0;
	static final double EARTH_DIAMETER = EARTH_RADIUS * 2.0;
	static final double EARTH_CIRCUMFERENCE = EARTH_DIAMETER * Math.PI;
	static final double MAXEXTENT = EARTH_CIRCUMFERENCE / 2.0;
	static final double M_PI_by2 = Math.PI / 2;
	static final double D2R = Math.PI / 180; // degrees to radians constant
	static final double R2D = 180 / Math.PI;
	static final double M_PIby360 = Math.PI / 360;
	static final double MAXEXTENTby180 = MAXEXTENT / 180;
	static final double MAX_LATITUDE = R2D * (2 * Math.atan(Math.exp(180 * D2R)) - M_PI_by2);

	public static double[] lonlat2merc(double lon, double lat) {
		// lon, lat in degrees
		double x = lon;
		double y = lat;
		if (x > 180) {
			x = 180;
		} else if (x < -180) {
			x = -180;
		}
		if (y > MAX_LATITUDE) {
			y = MAX_LATITUDE;
		} else if (y < -MAX_LATITUDE) {
			y = -MAX_LATITUDE;
		}
		x *= MAXEXTENTby180;
		y = Math.log(Math.tan((90 + y) * M_PIby360)) * R2D;
		y *= MAXEXTENTby180;
		return new double[] { x, y };
	}

	public static double[] merc2lonlat(double x, double y) {
		double lon = x;
		double lat = y;
		if (lon > MAXEXTENT) {
			lon = MAXEXTENT;
		} else if (lon < -MAXEXTENT) {
			lon = -MAXEXTENT;
		}
		if (lat > MAXEXTENT) {
			lat = MAXEXTENT;
		} else if (lat < -MAXEXTENT) {
			lat = -MAXEXTENT;
		}
		lat = (lat / MAXEXTENT) * 180;
		lon = (lon / MAXEXTENT) * 180;
		lon = R2D * (2 * Math.atan(Math.exp(lon * D2R)) - M_PI_by2);
		return new double[] { lon, lat };
	}

	// Takes lon/lat in degrees and returns arc length between two points (see great-circle distance)
	public static double getDistBetweenPoints(double lon1, double lat1, double lon2, double lat2) {
		return (EARTH_RADIUS * getCentralAngleRads(lon1, lat1, lon2, lat2));
	}

	// Note: for convenience takes degrees and returns radians
	private static double getCentralAngleRads(double lon1, double lat1, double lon2, double lat2) {
		double dLon = (lon2 - lon1) * D2R;
		double dLat = (lat2 - lat1) * D2R;
		return (2 * Math.asin(Math.sqrt(Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(lat1 * D2R) * Math.cos(lat2 * D2R) * Math.sin(dLon / 2) * Math.sin(dLon / 2))));
		// could also: atan2(sqrt(x), sqrt(1-x)); x is the added sinusoids above
	}

	// Find new point in between two coordinates given the amount of distance travelled along route from first point.
	// Fraction is fraction of distance between two points. central angle between the points from above.
	// Algorithm and credit to: http://williams.best.vwh.net/avform.htm#Intro
	// Note: all formulae from this page assume longitude's sign is reversed (west positive)! Shouldn't matter here.
	public static double[] getPointBetweenPoints(double lon1Deg, double lat1Deg, double lon2Deg, double lat2Deg,
			double frac) {
		double lon1 = -lon1Deg * D2R; // convert degrees to radians
		double lat1 = lat1Deg * D2R;
		double lon2 = -lon2Deg * D2R;
		double lat2 = lat2Deg * D2R;
		double centralAngle = getCentralAngleRads(lon1Deg, lat1Deg, lon2Deg, lat2Deg); // returns radians
		double A = Math.sin((1 - frac) * centralAngle) / Math.sin(centralAngle);
		double B = Math.sin(frac * centralAngle) / Math.sin(centralAngle);
		double x = A * Math.cos(lat1) * Math.cos(lon1) + B * Math.cos(lat2) * Math.cos(lon2);
		double y = A * Math.cos(lat1) * Math.sin(lon1) + B * Math.cos(lat2) * Math.sin(lon2);
		double z = A * Math.sin(lat1) + B * Math.sin(lat2);
		double lat = Math.atan2(z, Math.sqrt(x * x + y * y)) * R2D;
		double lon = -Math.atan2(y, x) * R2D;
		return new double[] { lon, lat };
	}

	public static void main(String[] args) {
		String inFile = "resources/sanfrancisco.osm";
		String outFile = "projected.osm";
		@SuppressWarnings("unused")
		ProjectionConverter osm = new ProjectionConverter(inFile, outFile, false);
		Log.info("Finished converting " + inFile + " to " + outFile);
	}
}
