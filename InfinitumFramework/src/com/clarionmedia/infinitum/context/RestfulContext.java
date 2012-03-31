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

package com.clarionmedia.infinitum.context;

import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;
import com.clarionmedia.infinitum.rest.AuthenticationStrategy;
import com.clarionmedia.infinitum.rest.impl.SharedSecretAuthentication;

/**
 * <p>
 * Container for RESTful web service configuration.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/30/12
 */
public class RestfulContext {
	
	private String mRestHost;
	private boolean mIsRestAuthenticated;
	private AuthenticationStrategy mAuthStrategy;
	private int mConnectionTimeout;
	private int mResponseTimeout;
	
	public String getRestHost() {
		return mRestHost;
	}

	public void setRestHost(String restHost) {
		if (!restHost.endsWith("/"))
			restHost += '/';
		mRestHost = restHost;
	}

	public void setRestAuthenticated(boolean isRestAuthenticated) {
		mIsRestAuthenticated = isRestAuthenticated;
	}

	public boolean isRestAuthenticated() {
		return mIsRestAuthenticated;
	}

	public void setAuthStrategy(String strategy) {
		if ("token".equalsIgnoreCase(strategy))
			mAuthStrategy = new SharedSecretAuthentication();
		else
		    throw new InfinitumConfigurationException("Unrecognized authentication strategy '" + strategy + "'.");
	}

	public AuthenticationStrategy getAuthStrategy() {
		return mAuthStrategy;
	}

	public int getConnectionTimeout() {
		return mConnectionTimeout;
	}

	public void setConnectionTimeout(int mConnectionTimeout) {
		this.mConnectionTimeout = mConnectionTimeout;
	}

	public int getResponseTimeout() {
		return mResponseTimeout;
	}

	public void setResponseTimeout(int mResponseTimeout) {
		this.mResponseTimeout = mResponseTimeout;
	}

}
