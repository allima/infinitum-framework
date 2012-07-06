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

import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.clarionmedia.infinitum.context.ContextFactory;
import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.logging.Logger;

/**
 * <p>
 * Provides an abstracted interface for communicating with RESTful web services.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0
 * @since 07/04/12
 */
public abstract class RestfulClient {

	protected InfinitumContext mContext;
	protected String mHost;
	protected boolean mIsAuthenticated;
	protected AuthenticationStrategy mAuthStrategy;
	protected Logger mLogger;

	/**
	 * Constructs a new {@code RestfulClient}. You must call
	 * {@link ContextFactory#configure(android.content.Context, int)} before
	 * invoking this constructor.
	 */
	public RestfulClient() {
		mLogger = Logger.getInstance(getClass().getSimpleName());
	}

	/**
	 * Prepares this {@code RestfulClient} for use. This must be called before
	 * using it.
	 */
	public final void prepare() {
		mContext = ContextFactory.getInstance().getContext();
		mHost = mContext.getRestfulConfiguration().getRestHost();
		mIsAuthenticated = mContext.getRestfulConfiguration().isRestAuthenticated();
		mAuthStrategy = mContext.getRestfulConfiguration().getAuthStrategy();
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
