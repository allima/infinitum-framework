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

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Encapsulates an HTTP server response from a RESTful web service.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0
 * @since 07/06/12
 */
public class RestResponse {

	private int mStatusCode;
	private byte[] mResponseData;
	private Map<String, String> mCookies;
	private Map<String, String> mHeaders;

	public RestResponse() {
		mCookies = new HashMap<String, String>();
		mHeaders = new HashMap<String, String>();
	}

	public int getStatusCode() {
		return mStatusCode;
	}

	public void setStatusCode(int statusCode) {
		mStatusCode = statusCode;
	}

	public byte[] getResponseData() {
		return mResponseData;
	}

	public void setResponseData(byte[] responseData) {
		mResponseData = responseData;
	}

	public Map<String, String> getCookies() {
		return mCookies;
	}

	public void setCookies(Map<String, String> cookies) {
		mCookies = cookies;
	}

	public Map<String, String> getHeaders() {
		return mHeaders;
	}

	public void setHeaders(Map<String, String> headers) {
		mHeaders = headers;
	}
	
	public void addHeader(String name, String value) {
		mHeaders.put(name, value);
	}

	public String getResponseDataAsString() {
		String response = "";
		if (mResponseData != null) {
			try {
				response = new String(mResponseData, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
		return response;
	}

	public void setResponseDataAsString(String responseDataStr) {
		if (responseDataStr == null) {
			mResponseData = null;
		} else {
			try {
				mResponseData = responseDataStr.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
