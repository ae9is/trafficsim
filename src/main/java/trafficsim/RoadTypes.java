/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package trafficsim;

/**
 *  
 */
public class RoadTypes {
	// road types could be enum as well
	// note: speeds given in km/hr
	public static final double SPD_HIGHWAY = 80;
	public static final int MOTORWAY = 0;
	public static final int MOTORWAY_LINK = 1;
	public static final int TRUNK = 2;
	public static final int TRUNK_LINK = 3;
	public static final double SPD_PRIMARY = 60;
	public static final int PRIMARY = 4;
	public static final int PRIMARY_LINK = 5;
	public static final double SPD_NORMAL = 50;
	public static final int SECONDARY = 6;
	public static final int SECONDARY_LINK = 7;
	public static final int TERTIARY = 8;
	public static final int TERTIARY_LINK = 9;
	public static final int ROAD = 16;
	public static final double SPD_RESIDENTIAL = 30;
	public static final int RESIDENTIAL = 10;
	public static final double SPD_SLOW = 10;
	public static final int LIVING_STREET = 11;
	public static final int PEDESTRIAN = 12;
	public static final int UNCLASSIFIED = 13;
	public static final int SERVICE = 14;
	public static final int TRACK = 15;
	public static final int CONSTRUCTION = 16;
}
