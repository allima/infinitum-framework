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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.clarionmedia.infinitum.context.impl.ContextFactory;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.internal.Preconditions;
import com.clarionmedia.infinitum.rest.Deserializer;
import com.clarionmedia.infinitum.rest.RestfulClient;
import com.clarionmedia.infinitum.rest.XmlDeserializer;

/**
 * <p>
 * A basic implementation of {@link RestfulClient} for standard CRUD operations.
 * This implementation is used for web services which send responses back as
 * XML.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 05/21/12
 */
public class RestfulXmlClient extends RestfulClient {

	protected Map<Class<?>, XmlDeserializer<?>> mXmlDeserializers;

	/**
	 * Constructs a new {@code RestfulXmlClient}. You must call
	 * {@link ContextFactory#configure(android.content.Context, int)} before
	 * invoking this constructor.
	 */
	public RestfulXmlClient() {
		mXmlDeserializers = new HashMap<Class<?>, XmlDeserializer<?>>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T load(Class<T> type, Serializable id) throws InfinitumRuntimeException, IllegalArgumentException {
		Preconditions.checkPersistenceForLoading(type);
		mLogger.debug("Sending GET request to retrieve entity");
		HttpClient httpClient = new DefaultHttpClient(getHttpParams());
		String uri = mHost + mPolicy.getRestfulResource(type) + "/" + id;
		if (mIsAuthenticated)
			uri += '?' + mAuthStrategy.getAuthenticationString();
		HttpGet httpGet = new HttpGet(uri);
		httpGet.addHeader("Accept", "application/xml");
		try {
			HttpResponse response = httpClient.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();
				if (entity == null)
					return null;
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				entity.writeTo(out);
				out.close();
				String xmlResponse = out.toString();
				if (mXmlDeserializers.containsKey(type))
					return (T) mXmlDeserializers.get(type).deserializeObject(xmlResponse);
				// TODO try to deserialize it ourselves
			}
		} catch (ClientProtocolException e) {
			mLogger.error("Unable to send GET request", e);
			return null;
		} catch (IOException e) {
			mLogger.error("Unable to read web service response", e);
			return null;
		}
		return null;
	}

	@Override
	public <T> void registerDeserializer(Class<T> type, Deserializer<T> deserializer) {
		try {
			XmlDeserializer<T> d = (XmlDeserializer<T>) deserializer;
			mXmlDeserializers.put(type, d);
		} catch (ClassCastException e) {
			// If deserializer is not XmlDeserializer, ignore it
		}
	}

}
