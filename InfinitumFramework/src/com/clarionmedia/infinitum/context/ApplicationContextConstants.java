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
 * Contains constants used in {@link ApplicationContext},
 * {@link ApplicationContextFactory}, and other supporting classes.
 * 
 * @author Tyler Treat
 * @version 1.0 02/12/12
 * 
 */
public class ApplicationContextConstants {
	
	// XML node names
	public static final String CONFIG_NODE = "infinitum-configuration";
	public static final String SQLITE_NODE = "sqlite";
	public static final String PROPERTY_NODE = "property";
	
	// XML attribute names
	public static final String NAME_ATTRIBUTE = "name";
	public static final String DB_NAME_ATTRIBUTE = "dbname";
	public static final String DB_VERSION_ATTRIBUTE = "dbversion";

	// Error messages
	public static final String CONFIG_NOT_CALLED = "You must call configure before accessing the ApplicationContext!";
	public static final String CONFIG_PARSE_ERROR = "Could not parse configuration file.";
	public static final String CONFIG_PARSE_ERROR_LINE = "Could not parse configuration file at line {?}.";
	public static final String SQLITE_DB_NAME_MISSING = "SQLite database name is missing!";

}
