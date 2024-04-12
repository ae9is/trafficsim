/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package util;

/**
 * Simple logger for projects.
 */
public class Log {

	public static final int ERROR = 3;
	public static final int WARNING = 2;
	public static final int INFO = 1;
	public static final int DEBUG = 0;
	private static int logLevel;
	
	static {
		logLevel = DEBUG;
	}
	
	public static void error(String msg) {
		System.out.println("Error: " + msg);
	}
	
	public static void warning(String msg) {
		if (logLevel <= WARNING) {
			System.out.println("Warning: " + msg);
		}
	}

	public static void info(String msg) {
		if (logLevel <= INFO) {
			System.out.println("Info: " + msg);
		}
	}

	public static void debug(String msg) {
		if (logLevel <= DEBUG) {
			System.out.println("Debug: " + msg);
		}
	}

	public int getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(int logLevel) {
		Log.logLevel = logLevel;
	}
	
}
