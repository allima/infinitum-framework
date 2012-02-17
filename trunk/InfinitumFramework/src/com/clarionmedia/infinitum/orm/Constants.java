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

package com.clarionmedia.infinitum.orm;

/**
 * <p>
 * Contains constants related to the Infinitum ORM.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/12/12
 */
public class Constants {

	public static enum PersistenceMode {
		Transient, Persistent
	}

	// Errors
	public static final String NO_PERSISTENT_FIELDS = "No persistent fields declared in '%s'.";
	public static final String CREATE_TABLES_ERROR = "Error creating database tables.";
	public static final String CANNOT_REGISTER_TYPE_ADAPTER = "Cannot register a TypeAdapter for '%s'.";
	public static final String CANNOT_MAP_TYPE = "Cannot map '%s' to a database column.";
	public static final String CANNOT_SAVE_TRANSIENT = "Cannot save transient class '%s'.";
	public static final String CANNOT_UPDATE_TRANSIENT = "Cannot update transient class '%s'.";
	public static final String CANNOT_LOAD_TRANSIENT = "Cannot load transient class '%s'.";
	public static final String MULTIPLE_PK_ERROR = "Multiple primary keys declared in '%s'.";
	public static final String INVALID_PK = "Invalid primary key value of type '%s' for '%s'.";
	public static final String IMPLICIT_PK_TYPE_ERROR = "Implicit primary key '%s' is not of type int or long in '%s'.";
	public static final String EXPLICIT_PK_TYPE_ERROR = "Explicit primary key '%s' is not of type int or long in '%s'.";
	public static final String UNABLE_TO_GEN_QUERY = "Unable to generate SQL query for Object of type '%s'.";
	public static final String NO_EMPTY_CONSTRUCTOR = "No empty constructor defined in '%s'.";

}
