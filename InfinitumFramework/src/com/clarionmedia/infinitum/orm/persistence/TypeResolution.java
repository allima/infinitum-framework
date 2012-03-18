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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.clarionmedia.infinitum.context.InfinitumContextFactory;
import com.clarionmedia.infinitum.internal.Primitives;
import com.clarionmedia.infinitum.internal.bind.SqliteTypeResolvers;
import com.clarionmedia.infinitum.orm.sqlite.SqliteTypeAdapter;

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

	// Represent the data types used in SQLite
	public static enum SqliteDataType {
		NULL, INTEGER, REAL, TEXT, BLOB
	};
	
	// This Map contains SqliteTypeResolvers for basic types
	public static Map<Class<?>, SqliteTypeAdapter<?>> sSqliteTypeResolvers;

	// Load basic TypeAdapters
	static {
		sSqliteTypeResolvers = new HashMap<Class<?>, SqliteTypeAdapter<?>>();
		sSqliteTypeResolvers.put(boolean.class, SqliteTypeResolvers.BOOLEAN);
		sSqliteTypeResolvers.put(byte.class, SqliteTypeResolvers.BYTE);
		sSqliteTypeResolvers.put(byte[].class, SqliteTypeResolvers.BYTE_ARRAY);
		sSqliteTypeResolvers.put(char.class, SqliteTypeResolvers.CHARACTER);
		sSqliteTypeResolvers.put(Date.class, SqliteTypeResolvers.DATE);
		sSqliteTypeResolvers.put(double.class, SqliteTypeResolvers.DOUBLE);
		sSqliteTypeResolvers.put(float.class, SqliteTypeResolvers.FLOAT);
		sSqliteTypeResolvers.put(int.class, SqliteTypeResolvers.INTEGER);
		sSqliteTypeResolvers.put(long.class, SqliteTypeResolvers.LONG);
		sSqliteTypeResolvers.put(short.class, SqliteTypeResolvers.SHORT);
		sSqliteTypeResolvers.put(String.class, SqliteTypeResolvers.STRING);
	}

	/**
	 * Indicates if the given ID is a valid value for the given primary key
	 * {@link Field}. Precondition assumes pkField is in fact a primary key.
	 * 
	 * @param pkField
	 *            the primary key {@code Field}
	 * @param id
	 *            the primary key value to check
	 * @return {@code true} if it is a valid primary key value, {@code false} if
	 *         not
	 */
	public static boolean isValidPrimaryKey(Field pkField, Serializable id) {
		if (id == null)
			return false;
		Class<?> pkUnwrapped = Primitives.unwrap(pkField.getType());
		Class<?> idUnwrapped = Primitives.unwrap(id.getClass());
		return pkUnwrapped == idUnwrapped;
	}

	/**
	 * Indicates if the given {@link Class} is a registered domain model for
	 * this application.
	 * 
	 * @param c
	 *            the {@code Class} to check
	 * @return {@code true} if it is a domain model, {@code false} if not
	 */
	public static boolean isDomainModel(Class<?> c) {
		for (String s : InfinitumContextFactory.getInfinitumContext().getDomainModels()) {
			if (c.getName().equalsIgnoreCase(s))
				return true;
		}
		return isDomainProxy(c);
	}

	/**
	 * Indicates if the given {@link Class} is a proxy for a domain model.
	 * 
	 * @param c
	 *            the {@code Class} to check
	 * @return {@code true} if it is a domain proxy, {@code false} if not
	 */
	public static boolean isDomainProxy(Class<?> c) {
		for (String s : InfinitumContextFactory.getInfinitumContext().getDomainModels()) {
			String name = s;
			if (name.contains("."))
				name = name.substring(name.lastIndexOf('.') + 1);
			if (c.getName().equalsIgnoreCase(name + "_Proxy"))
				return true;
		}
		return false;
	}

}
