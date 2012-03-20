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

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.context.InfinitumContextFactory;
import com.clarionmedia.infinitum.orm.persistence.PersistenceResolution;
import com.clarionmedia.infinitum.rest.RestfulClient;

public class BasicRestfulClient implements RestfulClient {
	
	private final String mHost;
	
	public BasicRestfulClient() {
		InfinitumContext context = InfinitumContextFactory.getInfinitumContext();
		mHost = context.getRestHost();
	}

	@Override
	public boolean save(Object model) {
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
