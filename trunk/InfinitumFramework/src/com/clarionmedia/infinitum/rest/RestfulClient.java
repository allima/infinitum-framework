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

package com.clarionmedia.infinitum.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.clarionmedia.infinitum.context.ContextProvider;
import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.internal.Preconditions;
import com.clarionmedia.infinitum.logging.Logger;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.persistence.TypeAdapter;
import com.clarionmedia.infinitum.rest.impl.RestfulJsonClient;
import com.clarionmedia.infinitum.rest.impl.RestfulMapper;
import com.clarionmedia.infinitum.rest.impl.RestfulModelMap;
import com.clarionmedia.infinitum.rest.impl.RestfulXmlClient;

/**
 * <p>
 * This abstract class provides an API for communicating with a RESTful web
 * service using objects. Infinitum provides two implementations called
 * {@link RestfulJsonClient}, which is used for web services that respond with
 * JSON, and {@link RestfulXmlClient}, which is used for web services that
 * respond with XML. These can be extended or re-implemented for specific
 * business needs.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/27/12
 */
public abstract class RestfulClient {

	protected static final String ENCODING = "UTF-8";

	protected InfinitumContext mContext;
	protected String mHost;
	protected boolean mIsAuthenticated;
	protected AuthenticationStrategy mAuthStrategy;
	protected RestfulMapper mMapper;
	protected PersistencePolicy mPolicy;
	protected Logger mLogger;

	/**
	 * Constructs a new {@code RestfulClient}. You must call
	 * {@link ContextProvider#configure(android.content.Context, int)} before
	 * invoking this constructor.
	 */
	public RestfulClient() {
		mLogger = Logger.getInstance(getClass().getSimpleName());
		mMapper = new RestfulMapper();
		mPolicy = ContextProvider.getInstance().getPersistencePolicy();
	}

	/**
	 * Returns an instance of the given persistent model {@link Class} as
	 * identified by the specified primary key or {@code null} if no such entity
	 * exists.
	 * 
	 * @param type
	 *            the {@code Class} of the persistent instance to load
	 * @param id
	 *            the primary key value of the persistent instance to load
	 * @return the persistent instance
	 * @throws InfinitumRuntimeException
	 *             if the specified {@code Class} is marked transient
	 * @throws IllegalArgumentException
	 *             if an invalid primary key is provided
	 */
	public abstract <T> T load(Class<T> type, Serializable id) throws InfinitumRuntimeException, IllegalArgumentException;

	/**
	 * Registers the given {@link Deserializer} for the given {@link Class}
	 * type. Registering a {@code Deserializer} for a {@code Class} which
	 * already has a {@code Deserializer} registered for it will result in the
	 * previous {@code Deserializer} being overridden.
	 * 
	 * @param type
	 *            the {@code Class} to associate this deserializer with
	 * @param deserializer
	 *            the {@code Deserializer} to use when deserializing
	 *            {@code Objects} of the given type
	 */
	public abstract <T> void registerDeserializer(Class<T> type, Deserializer<T> deserializer);

	/**
	 * Prepares this {@code RestfulClient} for use. This must be called before
	 * using it.
	 */
	public final void prepare() {
		mContext = ContextProvider.getInstance().getContext();
		mHost = mContext.getRestfulConfiguration().getRestHost();
		mIsAuthenticated = mContext.getRestfulConfiguration().isRestAuthenticated();
		mAuthStrategy = mContext.getRestfulConfiguration().getAuthStrategy();
	}

	/**
	 * Makes an HTTP request to the web service to save the given model.
	 * 
	 * @param model
	 *            the model to save
	 * @return {@code true} if the save succeeded, {@code false} if not
	 */
	public boolean save(Object model) {
		Preconditions.checkPersistenceForModify(model);
		mLogger.debug("Sending POST request to save entity");
		HttpClient httpClient = new DefaultHttpClient();
		String uri = mHost + mPolicy.getRestfulResource(model.getClass());
		if (mIsAuthenticated)
			uri += '?' + mAuthStrategy.getAuthenticationString();
		HttpPost httpPost = new HttpPost(uri);
		RestfulModelMap map = mMapper.mapModel(model);
		List<NameValuePair> pairs = map.getNameValuePairs();
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(pairs));
			HttpResponse response = httpClient.execute(httpPost);
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
					ENCODING));
			StringBuilder s = new StringBuilder();
			String ln;
			while ((ln = reader.readLine()) != null)
				s.append(ln);
			// TODO Check response?
			return true;
		} catch (UnsupportedEncodingException e) {
			mLogger.error("Unable to encode entity", e);
			return false;
		} catch (ClientProtocolException e) {
			mLogger.error("Unable to send POST request", e);
			return false;
		} catch (IOException e) {
			mLogger.error("Unable to read web service response", e);
			return false;
		}
	}

	/**
	 * Makes an HTTP request to the web service to delete the given model.
	 * 
	 * @param model
	 *            the model to delete
	 * @return {@code true} if the delete succeeded, {@code false} if not
	 */
	public boolean delete(Object model) {
		Preconditions.checkPersistenceForModify(model);
		mLogger.debug("Sending DELETE request to delete entity");
		HttpClient httpClient = new DefaultHttpClient();
		Serializable pk = mPolicy.getPrimaryKey(model);
		String uri = mHost + mPolicy.getRestfulResource(model.getClass()) + "/" + pk.toString();
		if (mIsAuthenticated)
			uri += '?' + mAuthStrategy.getAuthenticationString();
		HttpDelete httpDelete = new HttpDelete(uri);
		HttpResponse response;
		try {
			response = httpClient.execute(httpDelete);
			StatusLine statusLine = response.getStatusLine();
			return statusLine.getStatusCode() == HttpStatus.SC_OK;
		} catch (ClientProtocolException e) {
			mLogger.error("Unable to send DELETE request", e);
			return false;
		} catch (IOException e) {
			mLogger.error("Unable to send DELETE request", e);
			return false;
		}
	}

	/**
	 * Makes an HTTP request to the web service to update the given model or
	 * save it if it does not exist in the database.
	 * 
	 * @param model
	 *            the model to save or update
	 * @return 0 if the model was updated, 1 if the model was saved, or -1 if
	 *         the operation failed
	 */
	public int saveOrUpdate(Object model) {
		Preconditions.checkPersistenceForModify(model);
		mLogger.debug("Sending PUT request to save or update entity");
		HttpClient httpClient = new DefaultHttpClient();
		String uri = mHost + mPolicy.getRestfulResource(model.getClass());
		if (mIsAuthenticated)
			uri += '?' + mAuthStrategy.getAuthenticationString();
		HttpPut httpPut = new HttpPut(uri);
		RestfulModelMap map = mMapper.mapModel(model);
		List<NameValuePair> pairs = map.getNameValuePairs();
		try {
			httpPut.setEntity(new UrlEncodedFormEntity(pairs));
			HttpResponse response = httpClient.execute(httpPut);
			StatusLine statusLine = response.getStatusLine();
			switch (statusLine.getStatusCode()) {
			case HttpStatus.SC_CREATED:
				return 1;
			case HttpStatus.SC_OK:
				return 0;
			default:
				return -1;
			}
		} catch (UnsupportedEncodingException e) {
			mLogger.error("Unable to encode entity", e);
			return -1;
		} catch (ClientProtocolException e) {
			mLogger.error("Unable to send PUT request", e);
			return -1;
		} catch (IOException e) {
			mLogger.error("Unable to send PUT request", e);
			return -1;
		}
	}

	/**
	 * Registers the given {@link RestfulTypeAdapter} for the specified
	 * {@link Class} with this {@code RestfulClient} instance. The
	 * {@code RestfulTypeAdapter} allows a {@link Field} of this type to be
	 * mapped to a resource field in a web service. Registering a
	 * {@code RestfulTypeAdapter} for a {@code Class} which already has a
	 * {@code RestfulTypeAdapter} registered for it will result in the previous
	 * {@code RestfulTypeAdapter} being overridden.
	 * 
	 * @param type
	 *            the {@code Class} this {@code RestfulTypeAdapter} is for
	 * @param adapter
	 *            the {@code RestfulTypeAdapter} to register
	 */
	public <T> void registerTypeAdapter(Class<T> type, RestfulTypeAdapter<T> adapter) {
		mMapper.registerTypeAdapter(type, adapter);
	}

	/**
	 * Returns a {@link Map} containing all {@link TypeAdapter} instances
	 * registered with this {@code RestfulClient} and the {@link Class}
	 * instances in which they are registered for.
	 * 
	 * @return {@code Map<Class<?>, TypeAdapter<?>>
	 * 
	 */
	public Map<Class<?>, ? extends TypeAdapter<?>> getRegisteredTypeAdapters() {
		return mMapper.getRegisteredTypeAdapters();
	}

	/**
	 * Returns a {@link HttpParams} configured using the
	 * {@link InfinitumContext}.
	 * 
	 * @return {@code HttpParams}
	 */
	protected HttpParams getHttpParams() {
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams
				.setConnectionTimeout(httpParams, mContext.getRestfulConfiguration().getConnectionTimeout());
		HttpConnectionParams.setSoTimeout(httpParams, mContext.getRestfulConfiguration().getResponseTimeout());
		HttpConnectionParams.setTcpNoDelay(httpParams, true);
		return httpParams;
	}

}
