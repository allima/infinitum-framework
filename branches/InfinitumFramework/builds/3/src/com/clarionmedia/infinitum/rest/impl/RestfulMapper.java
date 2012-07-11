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

package com.clarionmedia.infinitum.rest.impl;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.clarionmedia.infinitum.context.impl.ContextFactory;
import com.clarionmedia.infinitum.internal.Primitives;
import com.clarionmedia.infinitum.internal.bind.RestfulTypeAdapters;
import com.clarionmedia.infinitum.orm.ObjectMapper;
import com.clarionmedia.infinitum.orm.OrmConstants;
import com.clarionmedia.infinitum.orm.exception.InvalidMappingException;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.persistence.TypeAdapter;
import com.clarionmedia.infinitum.rest.RestfulTypeAdapter;

/**
 * <p>
 * This implementation of {@link ObjectMapper} provides methods to map domain
 * models to RESTful web service resources and vice versa.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/21/12
 */
public class RestfulMapper extends ObjectMapper {

	private Map<Class<?>, RestfulTypeAdapter<?>> mTypeAdapters;
	private PersistencePolicy mPolicy;

	public RestfulMapper() {
		mPolicy = ContextFactory.getInstance().getPersistencePolicy();
		mTypeAdapters = new HashMap<Class<?>, RestfulTypeAdapter<?>>();
		mTypeAdapters.put(boolean.class, RestfulTypeAdapters.BOOLEAN);
		mTypeAdapters.put(byte.class, RestfulTypeAdapters.BYTE);
		mTypeAdapters.put(byte[].class, RestfulTypeAdapters.BYTE_ARRAY);
		mTypeAdapters.put(char.class, RestfulTypeAdapters.CHARACTER);
		mTypeAdapters.put(Date.class, RestfulTypeAdapters.DATE);
		mTypeAdapters.put(double.class, RestfulTypeAdapters.DOUBLE);
		mTypeAdapters.put(float.class, RestfulTypeAdapters.FLOAT);
		mTypeAdapters.put(int.class, RestfulTypeAdapters.INTEGER);
		mTypeAdapters.put(long.class, RestfulTypeAdapters.LONG);
		mTypeAdapters.put(short.class, RestfulTypeAdapters.SHORT);
		mTypeAdapters.put(String.class, RestfulTypeAdapters.STRING);
	}

	@Override
	public RestfulModelMap mapModel(Object model) throws InvalidMappingException, ModelConfigurationException {
		// We do not map transient classes!
		if (!mPolicy.isPersistent(model.getClass()))
			return null;
		RestfulModelMap ret = new RestfulModelMap(model);
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
				mapField(ret, model, f);
			} catch (IllegalArgumentException e) {
				mLogger.error("Unable to map field " + f.getName() + " for object of type '" + model.getClass().getName() + "'", e);
			} catch (IllegalAccessException e) {
				mLogger.error("Unable to map field " + f.getName() + " for object of type '" + model.getClass().getName() + "'", e);
			}
		}
		return ret;
	}

	@Override
	public <T> void registerTypeAdapter(Class<T> type, TypeAdapter<T> adapter) {
		try {
			RestfulTypeAdapter<T> restfulAdapter = (RestfulTypeAdapter<T>) adapter;
			mTypeAdapters.put(type, restfulAdapter);
		} catch (ClassCastException e) {
			// Ignore TypeAdapters that are not RestfulTypeAdapters
		}
	}

	@Override
	public Map<Class<?>, ? extends TypeAdapter<?>> getRegisteredTypeAdapters() {
		return mTypeAdapters;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> RestfulTypeAdapter<T> resolveType(Class<T> type) throws InvalidMappingException {
		type = Primitives.unwrap(type);
		if (mTypeAdapters.containsKey(type))
			return (RestfulTypeAdapter<T>) mTypeAdapters.get(type);
		throw new InvalidMappingException(String.format(OrmConstants.CANNOT_MAP_TYPE, type.getSimpleName()));
	}

	@Override
	public boolean isTextColumn(Field f) {
		throw new UnsupportedOperationException();
	}

	// Map Field value to NameValuePair
	private void mapField(RestfulModelMap map, Object model, Field field) throws InvalidMappingException,
			IllegalArgumentException, IllegalAccessException {
		Object val = null;
		// We need to use the Field's getter if model is a proxy
		if (mTypePolicy.isDomainProxy(model.getClass()))
			val = mClassReflector.invokeGetter(field, model);
		// Otherwise just use normal reflection...
		else
			val = field.get(model);
		String fieldName = mPolicy.getResourceFieldName(field);
		resolveType(field.getType()).mapObjectToField(val, fieldName, map.getNameValuePairs());
	}

}
