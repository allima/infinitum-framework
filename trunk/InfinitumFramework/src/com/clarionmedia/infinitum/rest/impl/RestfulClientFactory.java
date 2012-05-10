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

import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.context.ContextFactory;
import com.clarionmedia.infinitum.rest.JsonDeserializer;
import com.clarionmedia.infinitum.rest.RestfulClient;
import com.clarionmedia.infinitum.rest.RestfulClientBuilder;
import com.clarionmedia.infinitum.rest.RestfulTypeAdapter;

/**
 * <p>
 * Implementation of {@link RestfulClientBuilder} for creating new, configured
 * instances of {@link RestfulClient}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/28/12
 */
public class RestfulClientFactory implements RestfulClientBuilder {

	private RestfulClient mRestClient;

	/**
	 * Constructs a new {@code RestfulClientFactory}.
	 */
	public RestfulClientFactory() {
		clearConfiguration();
	}

	@Override
	public RestfulClient build() {
		return mRestClient;
	}

	@Override
	public <E> RestfulClientBuilder registerJsonDeserializer(Class<E> type, JsonDeserializer<E> deserializer) {
		mRestClient.registerJsonDeserializer(type, deserializer);
		return this;
	}

	@Override
	public <E> RestfulClientBuilder registerTypeAdapter(Class<E> type, RestfulTypeAdapter<E> adapter) {
		mRestClient.registerTypeAdapter(type, adapter);
		return this;
	}

	@Override
	public RestfulClientBuilder clearConfiguration() {
		InfinitumContext ctx = ContextFactory.getInstance().getContext();
		String client = ctx.getRestfulContext().getClientBean();
		if (client == null)
			mRestClient = new BasicRestfulClient();
		else
		    mRestClient = (RestfulClient) ctx.getBean(client);
		mRestClient.prepare();
		return this;
	}

}
