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

import com.clarionmedia.infinitum.internal.Primitives;
import com.clarionmedia.infinitum.internal.bind.RestfulTypeAdapters;
import com.clarionmedia.infinitum.orm.ObjectMapper;
import com.clarionmedia.infinitum.orm.OrmConstants;
import com.clarionmedia.infinitum.orm.exception.InvalidMappingException;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.PersistenceResolution;
import com.clarionmedia.infinitum.orm.persistence.TypeAdapter;
import com.clarionmedia.infinitum.orm.persistence.TypeResolution;
import com.clarionmedia.infinitum.reflection.ClassReflector;
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
	
	public RestfulMapper() {
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
		if (!PersistenceResolution.isPersistent(model.getClass()))
		    return null;
		RestfulModelMap  ret = new RestfulModelMap(model);
		for (Field f : PersistenceResolution.getPersistentFields(model.getClass())) {
			// Don't map primary keys if they are autoincrementing
			if (PersistenceResolution.isFieldPrimaryKey(f) && PersistenceResolution.isPrimaryKeyAutoIncrement(f))
			    continue;
			try {
				f.setAccessible(true);
				// Map relationships
				if (PersistenceResolution.isRelationship(f)) {
					mapRelationship(ret, model, f);
					continue;
				}
				// Map Field values
				mapField(ret, model, f);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		// TODO Auto-generated method stub
		return false;
	}
	
	// Map Field value to NameValuePair
	private void mapField(RestfulModelMap map, Object model, Field field) throws InvalidMappingException, IllegalArgumentException, IllegalAccessException {
		Object val = null;
		// We need to use the Field's getter if model is a proxy
		if (TypeResolution.isDomainProxy(model.getClass()))
			val = ClassReflector.invokeGetter(field, model);
		// Otherwise just use normal reflection...
		else
			val = field.get(model);
		String fieldName = PersistenceResolution.getResourceFieldName(field);
		resolveType(field.getType()).mapObjectToField(val, fieldName, map.getNameValuePairs());
	}

}
