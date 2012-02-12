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

package com.clarionmedia.infinitum.context;

/**
 * <p>
 * Indicates there is an error in the way Infinitum is configured.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/11/12
 */
public class InfinitumConfigurationException extends Exception {

	private static final long serialVersionUID = -6745750594618266996L;

	private static String mError;

	/**
	 * Constructs a new <code>InfinitumConfigurationException</code> with the
	 * given error message.
	 * 
	 * @param error
	 *            the error message for the <code>Exception</code>
	 */
	public InfinitumConfigurationException(String error) {
		super(error);
		mError = error;
	}

	/**
	 * Returns the error message for the
	 * <code>InfinitumConfigurationException</code>.
	 * 
	 * @return the error message
	 */
	public String getError() {
		return mError;
	}

}
