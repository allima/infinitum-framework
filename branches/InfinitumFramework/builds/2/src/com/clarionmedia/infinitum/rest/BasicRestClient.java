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

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import android.util.Log;
import com.clarionmedia.infinitum.context.ApplicationContext;
import com.clarionmedia.infinitum.context.ApplicationContextFactory;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;

/**
 * This is a basic implementation of {@link RestClient}. This class can be
 * extended or re-implemented to cater to specific business needs or web service
 * requirements.
 * 
 * @author Tyler Treat
 * @version 1.0 02/27/12
 */
public class BasicRestClient implements RestClient {

	private static final String TAG = "RestClientImpl";

	private ApplicationContext mAppContext;

	public BasicRestClient() throws InfinitumRuntimeException {
		mAppContext = ApplicationContextFactory.getApplicationContext();
		if (!mAppContext.hasRestfulService()
				|| mAppContext.getRestHost() == null)
			throw new InfinitumRuntimeException("REST host not defined.");
	}

	@Override
	public boolean save(Object model) {
		if (mAppContext.isDebug())
			Log.d(TAG, "Sending POST request to save model");
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = null;
		// TODO
		return false;
	}

	@Override
	public boolean delete(Object model) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean update(Object model) {
		// TODO Auto-generated method stub
		return false;
	}

}
