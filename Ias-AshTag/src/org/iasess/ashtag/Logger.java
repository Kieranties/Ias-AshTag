package org.iasess.ashtag;

import android.util.Log;

/**
 * Application wide logging class
 */
public final class Logger {

	/**
	 * The application wide tag name to use when appending messages to LogCat
	 */
	private static String _tagName = AshTagApp.getContext().getResources().getString(R.string.app_name);

	/**
	 * Logs the given message as a DEBUG message
	 * 
	 * @param message
	 *            The text to log
	 */
	public static void debug(String message) {
		Log.d(_tagName, message);
	}

	/**
	 * Logs the given message as a WARNING message
	 * 
	 * @param message
	 *            The test to log
	 */
	public static void warn(String message) {
		Log.w(_tagName, message);
	}

	/**
	 * Private constructor to promote Singleton use
	 */
	private Logger() {}
}