/*
 * Copyright (c) 2012 Tyler Treat
 * 
 * This file is part of Infinitum Framework.
 *
 * Infinitum Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Infinitum Framework is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Infinitum Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.clarionmedia.infinitum.logging;

/**
 * <p>
 * Prints log messages to Logcat but adheres to environment configuration.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 04/10/12
 */
public interface Logger {

	/**
	 * Prints a log message at the DEBUG level.
	 * 
	 * @param msg
	 *            the message to log
	 */
	void debug(String msg);

	/**
	 * Prints a log message at the DEBUG level.
	 * 
	 * @param msg
	 *            the message to log
	 * @param tr
	 *            the {@link Throwable} to log
	 */
	void debug(String msg, Throwable tr);

	/**
	 * Prints a log message at the ERROR level.
	 * 
	 * @param msg
	 *            the message to log
	 */
	void error(String msg);

	/**
	 * Prints a log message at the ERROR level.
	 * 
	 * @param msg
	 *            the message to log
	 * @param tr
	 *            the {@link Throwable} to log
	 */
	void error(String msg, Throwable tr);

	/**
	 * Prints a log message at the INFO level.
	 * 
	 * @param msg
	 *            the message to log
	 */
	void info(String msg);

	/**
	 * Prints a log message at the INFO level.
	 * 
	 * @param msg
	 *            the message to log
	 * @param tr
	 *            the {@link Throwable} to log
	 */
	void info(String msg, Throwable tr);

	/**
	 * Prints a log message at the VERBOSE level.
	 * 
	 * @param msg
	 *            the message to log
	 */
	void verbose(String msg);

	/**
	 * Prints a log message at the VERBOSE level.
	 * 
	 * @param msg
	 *            the message to log
	 * @param tr
	 *            the {@link Throwable} to log
	 */
	void verbose(String msg, Throwable tr);

	/**
	 * Prints a log message at the WARN level.
	 * 
	 * @param msg
	 *            the message to log
	 */
	void warn(String msg);

	/**
	 * Prints a log message at the WARN level.
	 * 
	 * @param msg
	 *            the message to log
	 * @param tr
	 *            the {@link Throwable} to log
	 */
	void warn(String msg, Throwable tr);

	/**
	 * What a Terrible Failure. Used to report a condition that should never
	 * occur.
	 * 
	 * @param msg
	 *            the message to log
	 */
	void wtf(String msg);

	/**
	 * What a Terrible Failure. Used to report a condition that should never
	 * occur.
	 * 
	 * @param msg
	 *            the message to log
	 * @param tr
	 *            the {@link Throwable} to log
	 */
	void wtf(String msg, Throwable tr);

}
