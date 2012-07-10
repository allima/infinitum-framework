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
import com.clarionmedia.infinitum.rest.Deserializer;
import com.clarionmedia.infinitum.rest.JsonDeserializer;
import com.clarionmedia.infinitum.rest.RestfulModelClient;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * <p>
 * A basic implementation of {@link RestfulModelClient} for standard CRUD operations.
 * This implementation is used for web services which send responses back as
 * JSON.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0
 * @since 03/21/12
 */
public class RestfulJsonClient extends RestfulModelClient {

	protected Map<Class<?>, JsonDeserializer<?>> mJsonDeserializers;

	/**
	 * Constructs a new {@code RestfulJsonClient}. You must call
	 * {@link ContextFactory#configure(android.content.Context, int)} before
	 * invoking this constructor.
	 */
	public RestfulJsonClient() {
		mJsonDeserializers = new HashMap<Class<?>, JsonDeserializer<?>>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T load(Class<T> type, Serializable id) throws InfinitumRuntimeException, IllegalArgumentException {
		Preconditions.checkPersistenceForLoading(type);
		mLogger.debug("Sending GET request to retrieve entity");
		HttpClient httpClient = new DefaultHttpClient(getHttpParams());
		String uri = mHost + mPolicy.getRestEndpoint(type) + "/" + id;
		if (mIsAuthenticated && !mAuthStrategy.isHeader())
			uri += '?' + mAuthStrategy.getAuthenticationString();
		HttpGet httpGet = new HttpGet(uri);
		if (mIsAuthenticated && mAuthStrategy.isHeader())
			httpGet.addHeader(mAuthStrategy.getAuthenticationKey(), mAuthStrategy.getAuthenticationValue());
		httpGet.addHeader("Accept", "application/json");
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
				String jsonResponse = out.toString();
				// Attempt to use a registered deserializer
				if (mJsonDeserializers.containsKey(type))
					return (T) mJsonDeserializers.get(type).deserializeObject(jsonResponse);
				// Otherwise fallback to Gson
				return new Gson().fromJson(jsonResponse, type);
			}
		} catch (ClientProtocolException e) {
			mLogger.error("Unable to send GET request", e);
			return null;
		} catch (IOException e) {
			mLogger.error("Unable to read web service response", e);
			return null;
		} catch (JsonSyntaxException e) {
			mLogger.error("Unable to deserialize web service response", e);
			return null;
		}
		return null;
	}

	@Override
	public <T> void registerDeserializer(Class<T> type, Deserializer<T> deserializer) {
		if (deserializer instanceof JsonDeserializer)
			mJsonDeserializers.put(type, (JsonDeserializer<T>) deserializer);
	}

}
