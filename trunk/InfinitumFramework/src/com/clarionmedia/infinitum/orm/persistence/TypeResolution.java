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

package com.clarionmedia.infinitum.orm.persistence;

import java.lang.reflect.Field;
import java.util.Date;

/**
 * <p>
 * This class provides runtime resolution of data types for the purpose of
 * persistence in the ORM.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/14/12
 */
public class TypeResolution {

	public static enum SqliteDataType {
		NULL, INTEGER, REAL, TEXT, BLOB
	};

	/**
	 * Retrieves the SQLite data type associated with the given
	 * <code>Field</code>.
	 * 
	 * @param field
	 *            the <code>Field</code> to retrieve the SQLite data type for
	 * @return <code>SqliteDataType</code> that matches the given
	 *         <code>Field</code>
	 */
	public static SqliteDataType getSqliteDataType(Field field) {
		Class<?> c = field.getType();
		if (c == String.class)
			return SqliteDataType.TEXT;
		else if (c == Integer.class)
			return SqliteDataType.INTEGER;
		else if (c == Long.class)
			return SqliteDataType.INTEGER;
		else if (c == Float.class)
			return SqliteDataType.REAL;
		else if (c == Double.class)
			return SqliteDataType.REAL;
		else if (c == Short.class)
			return SqliteDataType.INTEGER;
		else if (c == Boolean.class)
			return SqliteDataType.INTEGER;
		else if (c == Byte.class)
			return SqliteDataType.INTEGER;
		else if (c == byte[].class)
			return SqliteDataType.BLOB;
		else if (c == Character.class)
			return SqliteDataType.TEXT;
		else if (c == Date.class)
			return SqliteDataType.TEXT;
		// TODO: support additional types
		return null;
	}

}
