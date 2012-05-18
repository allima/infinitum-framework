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

package com.clarionmedia.infinitum.context.impl;

import com.clarionmedia.infinitum.context.RestfulConfiguration;
import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;
import com.clarionmedia.infinitum.rest.AuthenticationStrategy;
import com.clarionmedia.infinitum.rest.impl.SharedSecretAuthentication;

/**
 * <p>
 * Implementation of {@link RestfulConfiguration} containing RESTful web service
 * configuration information.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/30/12
 */
public class RestfulContext implements RestfulConfiguration {

	private String mRestHost;
	private boolean mIsRestAuthenticated;
	private AuthenticationStrategy mAuthStrategy;
	private int mConnectionTimeout;
	private int mResponseTimeout;
	private String mClientBean;

	@Override
	public String getRestHost() {
		return mRestHost;
	}

	@Override
	public void setRestHost(String restHost) {
		if (!restHost.endsWith("/"))
			restHost += '/';
		mRestHost = restHost;
	}

	@Override
	public void setRestAuthenticated(boolean isRestAuthenticated) {
		mIsRestAuthenticated = isRestAuthenticated;
	}

	@Override
	public boolean isRestAuthenticated() {
		return mIsRestAuthenticated;
	}

	@Override
	public void setAuthStrategy(String strategy)
			throws InfinitumConfigurationException {
		if ("token".equalsIgnoreCase(strategy))
			mAuthStrategy = new SharedSecretAuthentication();
		else
			throw new InfinitumConfigurationException(
					"Unrecognized authentication strategy '" + strategy + "'.");
	}

	@Override
	public <T extends AuthenticationStrategy> void setAuthStrategy(T strategy) {
		mAuthStrategy = strategy;
	}

	@Override
	public AuthenticationStrategy getAuthStrategy() {
		return mAuthStrategy;
	}

	@Override
	public int getConnectionTimeout() {
		return mConnectionTimeout;
	}

	@Override
	public void setConnectionTimeout(int mConnectionTimeout) {
		this.mConnectionTimeout = mConnectionTimeout;
	}

	@Override
	public int getResponseTimeout() {
		return mResponseTimeout;
	}

	@Override
	public void setResponseTimeout(int mResponseTimeout) {
		this.mResponseTimeout = mResponseTimeout;
	}

	@Override
	public String getClientBean() {
		return mClientBean;
	}

	@Override
	public void setClientBean(String clientBean) {
		mClientBean = clientBean;
	}

}
