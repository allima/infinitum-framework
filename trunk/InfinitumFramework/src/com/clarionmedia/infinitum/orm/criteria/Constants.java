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

package com.clarionmedia.infinitum.orm.criteria;

/**
 * <p>
 * Contains constants for the {@link Criteria} API.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/17/12
 */
public class Constants {
	
	// SQL
	public static final String LOWER = "LOWER";

	// Errors
	public static final String NON_UNIQUE_RESULT = "Criteria query for '%s' specified unique result but there were %d results.";
	public static final String TRANSIENT_CRITERIA = "Cannot create Criteria for transient class '%s'.";
	public static final String INVALID_CRITERIA = "Invalid Criteria for type '%s'.";

}
