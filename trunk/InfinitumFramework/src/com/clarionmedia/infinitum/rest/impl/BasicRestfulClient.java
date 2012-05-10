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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.clarionmedia.infinitum.context.ContextFactory;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.internal.Preconditions;
import com.clarionmedia.infinitum.logging.Logger;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.persistence.TypeAdapter;
import com.clarionmedia.infinitum.rest.JsonDeserializer;
import com.clarionmedia.infinitum.rest.RestfulClient;
import com.clarionmedia.infinitum.rest.RestfulTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * <p>
 * A basic implementation of {@link RestfulClient} for standard CRUD operations.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/21/12
 */
public class BasicRestfulClient extends RestfulClient {
	
	private static final String TAG = "BasicRestfulClient";
	private static final String ENCODING = "UTF-8";
	
	protected RestfulMapper mMapper;
	protected Map<Class<?>, JsonDeserializer<?>> mJsonDeserializers;
	protected PersistencePolicy mPolicy;
	protected Logger mLogger;
	
	/**
	 * Constructs a new {@code BasicRestfulClient}. You must call
	 * {@link ContextFactory#configure(android.content.Context, int)}
	 * before invoking this constructor.
	 */
	public BasicRestfulClient() {
		mLogger = Logger.getInstance(TAG);
		mMapper = new RestfulMapper();
		mJsonDeserializers = new HashMap<Class<?>, JsonDeserializer<?>>();
		mPolicy = ContextFactory.getInstance().getContext().getPersistencePolicy();
	}

	@Override
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
		    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), ENCODING));
		    StringBuilder s = new StringBuilder();
		    String ln;
			while ((ln = reader.readLine()) != null)
				s.append(ln);
			// TODO Check response?
			return true;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean delete(Object model) {
		Preconditions.checkPersistenceForModify(model);
		mLogger.debug("Sending DELETE request to delete entity");
		HttpClient httpClient = new DefaultHttpClient();
		Object pk = mPolicy.getPrimaryKey(model);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	@Override
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
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
				if (mJsonDeserializers.containsKey(type))
					return (T) mJsonDeserializers.get(type).deserializeObject(jsonResponse);
				return new Gson().fromJson(jsonResponse, type);
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	@Override
	public <T> void registerJsonDeserializer(Class<T> type, JsonDeserializer<T> deserializer) {
		mJsonDeserializers.put(type, deserializer);
	}
	
	@Override
	public <T> void registerTypeAdapter(Class<T> type,
			RestfulTypeAdapter<T> adapter) {
		mMapper.registerTypeAdapter(type, adapter);
	}
	
	@Override
	public Map<Class<?>, ? extends TypeAdapter<?>> getRegisteredTypeAdapters() {
		return mMapper.getRegisteredTypeAdapters();
	}
	
	private HttpParams getHttpParams() {
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, mContext.getRestfulContext().getConnectionTimeout());
		HttpConnectionParams.setSoTimeout(httpParams, mContext.getRestfulContext().getResponseTimeout());
		HttpConnectionParams.setTcpNoDelay(httpParams, true);
		return httpParams;
	}

}
