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

package com.clarionmedia.infinitum.http.rest.impl;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;

import com.clarionmedia.infinitum.http.HttpClientResponse;

/**
 * <p>
 * Encapsulates an HTTP server response from a RESTful web service.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 07/06/12
 * @since 1.0
 */
public class RestResponse implements HttpClientResponse {

	private HttpResponse mHttpResponse;
	private int mStatusCode;
	private byte[] mResponseData;
	private Map<String, String> mCookies;
	private Map<String, String> mHeaders;

	/**
	 * Creates a new {@code RestResponse}.
	 * 
	 * @param httpResponse
	 *            the {@link HttpResponse} to wrap
	 */
	public RestResponse(HttpResponse httpResponse) {
		mHttpResponse = httpResponse;
		mCookies = new HashMap<String, String>();
		mHeaders = new HashMap<String, String>();
	}
	
	/**
	 * Sets the HTTP status code.
	 * 
	 * @param statusCode
	 *            the status code to set
	 */
	public void setStatusCode(int statusCode) {
		mStatusCode = statusCode;
	}

	/**
	 * Sets the response message data as a byte array.
	 * 
	 * @param responseData
	 *            the message data byte array to set
	 */
	public void setResponseData(byte[] responseData) {
		mResponseData = responseData;
	}
	
	/**
	 * Sets the response message data as a {@link String}.
	 * 
	 * @param responseDataStr
	 *            the message data {@code String} to set
	 */
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

	@Override
	public int getStatusCode() {
		return mStatusCode;
	}
	
	@Override
	public byte[] getResponseData() {
		return mResponseData;
	}

	@Override
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

	@Override
	public Map<String, String> getCookies() {
		return mCookies;
	}

	@Override
	public Map<String, String> getHeaders() {
		return mHeaders;
	}

	@Override
	public void setHeaders(Map<String, String> headers) {
		mHeaders = headers;
		// TODO set cookies
	}

	@Override
	public void addHeader(String name, String value) {
		mHeaders.put(name, value);
		// TODO set cookies
	}

	@Override
	public HttpResponse unwrap() {
		return mHttpResponse;
	}

	@Override
	public String getHeader(String header) {
		return mHeaders.get(header);
	}

}