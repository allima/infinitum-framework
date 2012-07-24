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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
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

import android.database.SQLException;

import com.clarionmedia.infinitum.context.ContextFactory;
import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.internal.Preconditions;
import com.clarionmedia.infinitum.logging.Logger;
import com.clarionmedia.infinitum.orm.Session;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.exception.SQLGrammarException;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.persistence.TypeAdapter;
import com.clarionmedia.infinitum.rest.AuthenticationStrategy;

/**
 * <p>
 * {@link Session} implementation for communicating with a RESTful web service using domain
 * objects. Infinitum provides two concrete implementations called
 * {@link RestfulJsonClient}, which is used for web services that respond with
 * JSON, and {@link RestfulXmlClient}, which is used for web services that
 * respond with XML. These can be extended or re-implemented for specific
 * business needs.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/27/12
 * @since 1.0
 */
public abstract class RestfulSession implements Session {

	protected static final String ENCODING = "UTF-8";

	protected boolean mIsOpen;
	protected String mHost;
	protected boolean mIsAuthenticated;
	protected AuthenticationStrategy mAuthStrategy;
	protected Logger mLogger;
	protected RestfulMapper mMapper;
	protected PersistencePolicy mPolicy;
	protected Map<Integer, Object> mSessionCache;
	protected int mCacheSize;
	
	@Override
	public abstract <T> T load(Class<T> type, Serializable id) throws InfinitumRuntimeException,
			IllegalArgumentException;
	
	@Override
	public Session open() throws SQLException {
		mLogger = Logger.getInstance(getClass().getSimpleName());
		InfinitumContext context = ContextFactory.getInstance().getContext();
		mHost = context.getRestfulConfiguration().getRestHost();
		if (!mHost.endsWith("/"))
			mHost += '/';
		mIsAuthenticated = context.getRestfulConfiguration().isRestAuthenticated();
		mAuthStrategy = context.getRestfulConfiguration().getAuthStrategy();
		mMapper = new RestfulMapper();
		mPolicy = ContextFactory.getInstance().getPersistencePolicy();
		mSessionCache = new HashMap<Integer, Object>();
		mCacheSize = DEFAULT_CACHE_SIZE;
		mIsOpen = true;
		mLogger.debug("Session opened");
		return this;
	}

	@Override
	public Session close() {
		recycleCache();
		mIsOpen = false;
		mLogger.debug("Session closed");
		return this;
	}

	@Override
	public boolean isOpen() {
		return mIsOpen;
	}
	
	@Override
	public Session beginTransaction() {
		throw new UnsupportedOperationException("RestfulSession does not support transactions!");
	}

	@Override
	public Session commit() {
		throw new UnsupportedOperationException("RestfulSession does not support transactions!");
	}

	@Override
	public Session rollback() {
		throw new UnsupportedOperationException("RestfulSession does not support transactions!");
	}

	@Override
	public boolean isTransactionOpen() {
		throw new UnsupportedOperationException("RestfulSession does not support transactions!");
	}

	@Override
	public Session setAutocommit(boolean autocommit) {
		throw new UnsupportedOperationException("RestfulSession does not support transactions!");
	}

	@Override
	public boolean isAutocommit() {
		throw new UnsupportedOperationException("RestfulSession does not support transactions!");
	}
	
	@Override
	public Session recycleCache() {
		mSessionCache.clear();
		return this;
	}

	@Override
	public Session setCacheSize(int cacheSize) {
		mCacheSize = cacheSize;
		return this;
	}

	@Override
	public int getCacheSize() {
		return mCacheSize;
	}

	@Override
	public long save(Object model) {
		Preconditions.checkPersistenceForModify(model);
		mLogger.debug("Sending POST request to save entity");
		HttpClient httpClient = new DefaultHttpClient();
		String uri = mHost + mPolicy.getRestEndpoint(model.getClass());
		if (mIsAuthenticated && !mAuthStrategy.isHeader())
			uri += '?' + mAuthStrategy.getAuthenticationString();
		HttpPost httpPost = new HttpPost(uri);
		if (mIsAuthenticated && mAuthStrategy.isHeader())
			httpPost.addHeader(mAuthStrategy.getAuthenticationKey(), mAuthStrategy.getAuthenticationValue());
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
			return 0;
		} catch (UnsupportedEncodingException e) {
			mLogger.error("Unable to encode entity", e);
			return -1;
		} catch (ClientProtocolException e) {
			mLogger.error("Unable to send POST request", e);
			return -1;
		} catch (IOException e) {
			mLogger.error("Unable to read web service response", e);
			return -1;
		}
	}

	@Override
	public boolean delete(Object model) {
		Preconditions.checkPersistenceForModify(model);
		mLogger.debug("Sending DELETE request to delete entity");
		HttpClient httpClient = new DefaultHttpClient();
		Serializable pk = mPolicy.getPrimaryKey(model);
		String uri = mHost + mPolicy.getRestEndpoint(model.getClass()) + "/" + pk.toString();
		if (mIsAuthenticated && !mAuthStrategy.isHeader())
			uri += '?' + mAuthStrategy.getAuthenticationString();
		HttpDelete httpDelete = new HttpDelete(uri);
		if (mIsAuthenticated && mAuthStrategy.isHeader())
			httpDelete.addHeader(mAuthStrategy.getAuthenticationKey(), mAuthStrategy.getAuthenticationValue());
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
	
	@Override
	public boolean update(Object model) throws InfinitumRuntimeException {
		Preconditions.checkPersistenceForModify(model);
		mLogger.debug("Sending PUT request to update entity");
		HttpClient httpClient = new DefaultHttpClient();
		String uri = mHost + mPolicy.getRestEndpoint(model.getClass());
		if (mIsAuthenticated && !mAuthStrategy.isHeader())
			uri += '?' + mAuthStrategy.getAuthenticationString();
		HttpPut httpPut = new HttpPut(uri);
		if (mIsAuthenticated && mAuthStrategy.isHeader())
			httpPut.addHeader(mAuthStrategy.getAuthenticationKey(), mAuthStrategy.getAuthenticationValue());
		RestfulModelMap map = mMapper.mapModel(model);
		List<NameValuePair> pairs = map.getNameValuePairs();
		try {
			httpPut.setEntity(new UrlEncodedFormEntity(pairs));
			HttpResponse response = httpClient.execute(httpPut);
			StatusLine statusLine = response.getStatusLine();
			switch (statusLine.getStatusCode()) {
			case HttpStatus.SC_OK:
				return true;
			default:
				return false;
			}
		} catch (UnsupportedEncodingException e) {
			mLogger.error("Unable to encode entity", e);
			return false;
		} catch (ClientProtocolException e) {
			mLogger.error("Unable to send PUT request", e);
			return false;
		} catch (IOException e) {
			mLogger.error("Unable to send PUT request", e);
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
	@Override
	public long saveOrUpdate(Object model) {
		Preconditions.checkPersistenceForModify(model);
		mLogger.debug("Sending PUT request to save or update entity");
		HttpClient httpClient = new DefaultHttpClient();
		String uri = mHost + mPolicy.getRestEndpoint(model.getClass());
		if (mIsAuthenticated && !mAuthStrategy.isHeader())
			uri += '?' + mAuthStrategy.getAuthenticationString();
		HttpPut httpPut = new HttpPut(uri);
		if (mIsAuthenticated && mAuthStrategy.isHeader())
			httpPut.addHeader(mAuthStrategy.getAuthenticationKey(), mAuthStrategy.getAuthenticationValue());
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
	
	@Override
	public int saveOrUpdateAll(Collection<? extends Object> models)
			throws InfinitumRuntimeException {
		int count = 0;
		for (Object model : models) {
			if (saveOrUpdate(model) >= 0)
				count++;
		}
		return count;
	}
	
	@Override
	public int saveAll(Collection<? extends Object> models)
			throws InfinitumRuntimeException {
		int count = 0;
		for (Object model : models) {
		    if (save(model) == 0)
		    	count++;
		}
		return count;
	}

	@Override
	public int deleteAll(Collection<? extends Object> models)
			throws InfinitumRuntimeException {
		int count = 0;
		for (Object model : models) {
			if (delete(model))
				count++;
		}
		return count;
	}
	
	@Override
	public Session execute(String sql) throws SQLGrammarException {
		throw new UnsupportedOperationException("RestfulSession does not support SQL operations!");
	}

	@Override
	public <T> Criteria<T> createCriteria(Class<T> entityClass) {
		throw new UnsupportedOperationException("RestfulSession does not support criteria operations!");
	}

	@Override
	public <T> Session registerTypeAdapter(Class<T> type, TypeAdapter<T> adapter) {
		mMapper.registerTypeAdapter(type, adapter);
		return this;
	}

	@Override
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
		InfinitumContext context = ContextFactory.getInstance().getContext();
		HttpConnectionParams.setConnectionTimeout(httpParams, context.getRestfulConfiguration().getConnectionTimeout());
		HttpConnectionParams.setSoTimeout(httpParams, context.getRestfulConfiguration().getResponseTimeout());
		HttpConnectionParams.setTcpNoDelay(httpParams, true);
		return httpParams;
	}

}
