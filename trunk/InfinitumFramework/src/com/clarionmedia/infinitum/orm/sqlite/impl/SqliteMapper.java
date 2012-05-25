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

package com.clarionmedia.infinitum.orm.sqlite.impl;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;

import com.clarionmedia.infinitum.context.impl.ContextFactory;
import com.clarionmedia.infinitum.internal.Primitives;
import com.clarionmedia.infinitum.internal.bind.SqliteTypeAdapters;
import com.clarionmedia.infinitum.orm.ObjectMapper;
import com.clarionmedia.infinitum.orm.exception.InvalidMappingException;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.persistence.TypeAdapter;
import com.clarionmedia.infinitum.orm.persistence.TypeResolutionPolicy.SqliteDataType;
import com.clarionmedia.infinitum.orm.sqlite.SqliteTypeAdapter;

/**
 * <p>
 * This implementation of {@link ObjectMapper} provides methods to map domain
 * models to SQLite table columns and vice versa.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/13/12
 */
public class SqliteMapper extends ObjectMapper {

	private Map<Class<?>, SqliteTypeAdapter<?>> mTypeAdapters;
	private PersistencePolicy mPolicy;

	/**
	 * Constructs a new {@code SqliteMapper}.
	 */
	public SqliteMapper() {
		mPolicy = ContextFactory.getInstance().getPersistencePolicy();
		mTypeAdapters = new HashMap<Class<?>, SqliteTypeAdapter<?>>();
		mTypeAdapters.put(boolean.class, SqliteTypeAdapters.BOOLEAN);
		mTypeAdapters.put(byte.class, SqliteTypeAdapters.BYTE);
		mTypeAdapters.put(byte[].class, SqliteTypeAdapters.BYTE_ARRAY);
		mTypeAdapters.put(char.class, SqliteTypeAdapters.CHARACTER);
		mTypeAdapters.put(Date.class, SqliteTypeAdapters.DATE);
		mTypeAdapters.put(double.class, SqliteTypeAdapters.DOUBLE);
		mTypeAdapters.put(float.class, SqliteTypeAdapters.FLOAT);
		mTypeAdapters.put(int.class, SqliteTypeAdapters.INTEGER);
		mTypeAdapters.put(long.class, SqliteTypeAdapters.LONG);
		mTypeAdapters.put(short.class, SqliteTypeAdapters.SHORT);
		mTypeAdapters.put(String.class, SqliteTypeAdapters.STRING);
	}

	@Override
	public SqliteModelMap mapModel(Object model) throws InvalidMappingException, ModelConfigurationException {
		// We do not map transient classes!
		if (!mPolicy.isPersistent(model.getClass()))
			return null;
		SqliteModelMap ret = new SqliteModelMap(model);
		ContentValues values = new ContentValues();
		for (Field f : mPolicy.getPersistentFields(model.getClass())) {
			// Don't map primary keys if they are autoincrementing
			if (mPolicy.isFieldPrimaryKey(f) && mPolicy.isPrimaryKeyAutoIncrement(f))
				continue;
			try {
				f.setAccessible(true);
				// Map relationships
				if (mPolicy.isRelationship(f)) {
					mapRelationship(ret, model, f);
					continue;
				}
				// Map Field values
				mapField(values, model, f);
			} catch (IllegalArgumentException e) {
				mLogger.error("Unable to map field " + f.getName() + " for object of type '"
						+ model.getClass().getName() + "'", e);
			} catch (IllegalAccessException e) {
				mLogger.error("Unable to map field " + f.getName() + " for object of type '"
						+ model.getClass().getName() + "'", e);
			}
		}
		ret.setContentValues(values);
		return ret;
	}

	/**
	 * Retrieves a {@link SqliteTypeAdapter} for the given {@link Class}.
	 * 
	 * @param type
	 *            the {@code Class} to retrieve a {@code SqliteTypeResolver} for
	 * @return {@code SqliteTypeResolver} for the given type
	 * @throws InvalidMappingException
	 *             if no {@code SqliteTypeResolver} exists for the given
	 *             {@code Class}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> SqliteTypeAdapter<T> resolveType(Class<T> type) throws InvalidMappingException {
		type = Primitives.unwrap(type);
		if (mTypeAdapters.containsKey(type))
			return (SqliteTypeAdapter<T>) mTypeAdapters.get(type);
		throw new InvalidMappingException(String.format(mPropLoader.getErrorMessage("CANNOT_MAP_TYPE"),
				type.getSimpleName()));
	}

	@Override
	public <T> void registerTypeAdapter(Class<T> type, TypeAdapter<T> adapter) {
		try {
			SqliteTypeAdapter<T> sqliteAdapter = (SqliteTypeAdapter<T>) adapter;
			mTypeAdapters.put(type, sqliteAdapter);
		} catch (ClassCastException e) {
			// Ignore TypeAdapters that are not SqliteTypeAdapters
		}
	}

	@Override
	public Map<Class<?>, SqliteTypeAdapter<?>> getRegisteredTypeAdapters() {
		return mTypeAdapters;
	}

	@Override
	public boolean isTextColumn(Field f) {
		return getSqliteDataType(f) == SqliteDataType.TEXT;
	}

	/**
	 * Retrieves the SQLite data type associated with the given {@link Field}.
	 * 
	 * @param field
	 *            the{@code Field} to retrieve the SQLite data type for
	 * @return {@code SqliteDataType} that matches the given {@code Field}
	 */
	public SqliteDataType getSqliteDataType(Field field) {
		SqliteDataType ret = null;
		Class<?> c = Primitives.unwrap(field.getType());
		if (mTypeAdapters.containsKey(c))
			ret = mTypeAdapters.get(c).getSqliteType();
		else if (mTypePolicy.isDomainModel(c))
			ret = getSqliteDataType(mPolicy.getPrimaryKeyField(c));
		return ret;
	}

	/**
	 * Retrieves the SQLite data type associated with the given {@link Object}.
	 * 
	 * @param object
	 *            the {@code Object} to retrieve the SQLite data type for
	 * @return {@code SqliteDataType} that matches the given {@code Object}
	 */
	public SqliteDataType getSqliteDataType(Object object) {
		SqliteDataType ret = null;
		Class<?> c = Primitives.unwrap(object.getClass());
		if (mTypeAdapters.containsKey(c))
			ret = mTypeAdapters.get(c).getSqliteType();
		else if (mTypePolicy.isDomainModel(c))
			ret = getSqliteDataType(mPolicy.getPrimaryKeyField(c));
		return ret;
	}

	// Map Field value to ContentValues
	private void mapField(ContentValues values, Object model, Field field) throws InvalidMappingException,
			IllegalArgumentException, IllegalAccessException {
		Object val = null;
		// We need to use the Field's getter if model is a proxy
		if (mTypePolicy.isDomainProxy(model.getClass()))
			val = mClassReflector.invokeGetter(field, model);
		// Otherwise just use normal reflection...
		else
			val = field.get(model);
		String colName = mPolicy.getFieldColumnName(field);
		resolveType(field.getType()).mapObjectToColumn(val, colName, values);
	}

}
