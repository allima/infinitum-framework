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

package com.clarionmedia.infinitum.http.impl;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;

import com.clarionmedia.infinitum.http.HttpClientResponse;

/**
 * <p>
 * Basic implementation of {@link HttpClientResponse}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 08/15/12
 * @since 1.0
 */
public class BasicHttpClientResponse implements HttpClientResponse {

	private Map<String, String> mHeaders;
	private byte[] mResponseData;
	private int mStatusCode;
	
	/**
	 * Creates a new {@code BasicHttpClientResponse}.
	 */
	public BasicHttpClientResponse(int statusCode, byte[] responseData) {
		mStatusCode = statusCode;
		mResponseData = responseData;
		mHeaders = new HashMap<String, String>();
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
	
	@Override
	public HttpResponse unwrap() {
		return null;
	}
	
	@Override
	public Map<String, String> getHeaders() {
		return mHeaders;
	}
	
	@Override
	public String getHeader(String header) {
		return mHeaders.get(header);
	}
	
	@Override
	public void setHeaders(Map<String, String> headers) {
		mHeaders = headers;
	}
	
	@Override
	public void addHeader(String name, String value) {
		mHeaders.put(name, value);
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
		// TODO Auto-generated method stub
		return null;
	}
	
	
}