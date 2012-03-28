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

import com.clarionmedia.infinitum.rest.JsonDeserializer;
import com.clarionmedia.infinitum.rest.RestfulClient;
import com.clarionmedia.infinitum.rest.RestfulClientBuilder;

/**
 * <p>
 * Implementation of {@link RestfulClientBuilder} for creating new, configured
 * instances of {@link BasicRestfulClient}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/28/12
 */
public class BasicRestfulClientBuilder implements RestfulClientBuilder {

	private RestfulClient mRestClient;

	/**
	 * Constructs a new {@code BasicRestfulClientBuilder}. Calling
	 * {@link BasicRestfulClientBuilder#build()} will return a
	 * {@link RestfulClient} instance. This instance can be configured prior to
	 * calling this method, and configurations can be removed by calling
	 * {@link BasicRestfulClientBuilder#clearConfiguration()}.
	 */
	public BasicRestfulClientBuilder() {
		mRestClient = new BasicRestfulClient();
	}

	@Override
	public RestfulClient build() {
		return mRestClient;
	}

	@Override
	public <T> void registerJsonDeserializer(Class<T> type,
			JsonDeserializer<T> deserializer) {
		mRestClient.registerJsonDeserializer(type, deserializer);
	}

	@Override
	public <T> void registerTypeAdapter(Class<T> type,
			RestfulTypeAdapter<T> adapter) {
		mRestClient.registerTypeAdapter(type, adapter);
	}

	@Override
	public void clearConfiguration() {
		mRestClient = new BasicRestfulClient();
	}

}
