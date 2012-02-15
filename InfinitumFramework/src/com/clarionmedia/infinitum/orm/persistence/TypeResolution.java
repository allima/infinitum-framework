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
import java.util.Hashtable;
import java.util.Map;

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
	
	// This Map caches the SQLite data type for each Field
	private static Map<Field, SqliteDataType> sDataTypeCache;
	
	static {
		sDataTypeCache = new Hashtable<Field, SqliteDataType>();
	}

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
		if (sDataTypeCache.containsKey(field))
			return sDataTypeCache.get(field);
		SqliteDataType ret = null;
		Class<?> c = field.getType();
		if (c == String.class)
			ret = SqliteDataType.TEXT;
		else if (c == Integer.class || c == int.class)
			ret = SqliteDataType.INTEGER;
		else if (c == Long.class || c == long.class)
			ret = SqliteDataType.INTEGER;
		else if (c == Float.class || c == float.class)
			ret = SqliteDataType.REAL;
		else if (c == Double.class || c == double.class)
			ret = SqliteDataType.REAL;
		else if (c == Short.class || c == short.class)
			ret = SqliteDataType.INTEGER;
		else if (c == Boolean.class || c == boolean.class)
			ret = SqliteDataType.INTEGER;
		else if (c == Byte.class || c == byte.class)
			ret = SqliteDataType.INTEGER;
		else if (c == byte[].class)
			ret = SqliteDataType.BLOB;
		else if (c == Character.class || c == char.class)
			ret = SqliteDataType.TEXT;
		else if (c == Date.class)
			ret = SqliteDataType.TEXT;
		// TODO: support additional types
		if (ret != null)
			sDataTypeCache.put(field, ret);
		return ret;
	}

}
