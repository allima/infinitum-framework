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

import com.clarionmedia.infinitum.context.ContextFactory;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.internal.Preconditions;
import com.clarionmedia.infinitum.orm.Session;
import com.clarionmedia.infinitum.rest.Deserializer;
import com.clarionmedia.infinitum.rest.XmlDeserializer;

/**
 * <p>
 * Concrete implementation of {@link RestfulSession} for web services which send
 * responses back as XML.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 05/21/12
 * @since 1.0
 */
public class RestfulXmlSession extends RestfulSession {

	protected Map<Class<?>, XmlDeserializer<?>> mXmlDeserializers;

	/**
	 * Constructs a new {@code RestfulXmlSession}. You must call
	 * {@link ContextFactory#configure(android.content.Context, int)} before
	 * invoking this constructor.
	 */
	public RestfulXmlSession() {
		mXmlDeserializers = new HashMap<Class<?>, XmlDeserializer<?>>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T loadEntity(Class<T> type, Serializable id) throws InfinitumRuntimeException, IllegalArgumentException {
		Preconditions.checkPersistenceForLoading(type, mPersistencePolicy);
		mLogger.debug("Sending GET request to retrieve entity");
		HttpClient httpClient = new DefaultHttpClient(getHttpParams());
		String uri = mHost + mPersistencePolicy.getRestEndpoint(type) + "/" + id;
		if (mIsAuthenticated && !mAuthStrategy.isHeader())
			uri += '?' + mAuthStrategy.getAuthenticationString();
		HttpGet httpGet = new HttpGet(uri);
		if (mIsAuthenticated && mAuthStrategy.isHeader())
			httpGet.addHeader(mAuthStrategy.getAuthenticationKey(), mAuthStrategy.getAuthenticationValue());
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
				T ret = null;
				if (mXmlDeserializers.containsKey(type))
					ret = (T) mXmlDeserializers.get(type).deserializeObject(xmlResponse);
				// TODO try to deserialize it ourselves
				if (ret != null) {
				    int objHash = mPersistencePolicy.computeModelHash(ret);
				    cache(objHash, ret);
				}
				return ret;
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
	public <T> Session registerDeserializer(Class<T> type, Deserializer<T> deserializer) {
		if (XmlDeserializer.class.isAssignableFrom(deserializer.getClass()))
			mXmlDeserializers.put(type, (XmlDeserializer<T>) deserializer);
		return this;
	}

}
