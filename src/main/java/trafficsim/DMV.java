/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package trafficsim;

/**
 *  
 */
public class DMV {
	private static long idCount;

	static {
		idCount = 0;
	}

	public static long getNewId() {
		return idCount++;
	}

	public static void resetIdCount() {
		idCount = 0;
	}
}
