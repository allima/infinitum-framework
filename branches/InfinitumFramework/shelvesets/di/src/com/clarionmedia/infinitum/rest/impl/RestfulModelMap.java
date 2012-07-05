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

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import com.clarionmedia.infinitum.orm.ModelMap;

/**
 * <p>
 * Concrete implementation of {@link ModelMap} representing a domain model
 * instance mapped to a RESTful web service resource.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/21/12
 */
public class RestfulModelMap extends ModelMap {
	
	private List<NameValuePair> mNameValuePairs;

	public RestfulModelMap(Object model) {
		super(model);
		setNameValuePairs(new ArrayList<NameValuePair>());
	}

	public List<NameValuePair> getNameValuePairs() {
		return mNameValuePairs;
	}

	public void setNameValuePairs(List<NameValuePair> nameValuePairs) {
		mNameValuePairs = nameValuePairs;
	}
	
	public void addNameValuePair(NameValuePair pair) {
		mNameValuePairs.add(pair);
	}

}
