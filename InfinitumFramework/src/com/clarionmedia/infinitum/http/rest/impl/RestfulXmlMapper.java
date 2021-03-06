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

package com.clarionmedia.infinitum.http.rest.impl;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.clarionmedia.infinitum.http.rest.RestfulMapper;
import com.clarionmedia.infinitum.http.rest.RestfulPairsTypeAdapter;
import com.clarionmedia.infinitum.http.rest.RestfulXmlTypeAdapter;
import com.clarionmedia.infinitum.orm.ObjectMapper;
import com.clarionmedia.infinitum.orm.exception.InvalidMappingException;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.TypeAdapter;

/**
 * <p>
 * This implementation of {@link ObjectMapper} provides methods to map domain
 * models to RESTful web service resources for XML message types.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 08/05/12
 * @since 1.0
 */
public class RestfulXmlMapper extends RestfulMapper {

	private Map<Class<?>, RestfulXmlTypeAdapter<?>> mTypeAdapters;
	private Serializer mSerializer;

	/**
	 * Constructs a new {@code RestfulJsonMapper}.
	 */
	public RestfulXmlMapper() {
		mTypeAdapters = new HashMap<Class<?>, RestfulXmlTypeAdapter<?>>();
		mSerializer = new Persister();
	}

	@Override
	public RestfulStringModelMap mapModel(Object model) throws InvalidMappingException, ModelConfigurationException {
		// We do not map transient classes!
		if (!mPersistencePolicy.isPersistent(model.getClass()))
			return null;
		RestfulStringModelMap modelMap = new RestfulStringModelMap(model);
		String xml;
		if (mTypeAdapters.containsKey(model.getClass()))
			xml = mTypeAdapters.get(model.getClass()).serializeObjectToXml(model);
		else {
			StringWriter writer = new StringWriter();
			try {
				mSerializer.write(model, writer);
				xml = writer.toString();
			} catch (Exception e) {
				return null;
			}
		}
		modelMap.setMessage(xml);
		return modelMap;
	}

	@Override
	public <T> void registerTypeAdapter(Class<T> type, TypeAdapter<T> adapter) {
		if (RestfulXmlTypeAdapter.class.isAssignableFrom(adapter.getClass()))
		    mTypeAdapters.put(type, (RestfulXmlTypeAdapter<?>) adapter);
	}

	@Override
	public Map<Class<?>, ? extends TypeAdapter<?>> getRegisteredTypeAdapters() {
		return mTypeAdapters;
	}

	@Override
	public <T> RestfulPairsTypeAdapter<T> resolveType(Class<T> type) {
		throw new UnsupportedOperationException();
	}

}
