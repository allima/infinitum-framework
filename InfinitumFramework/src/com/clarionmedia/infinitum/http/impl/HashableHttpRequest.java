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

import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpUriRequest;

import com.clarionmedia.infinitum.http.HttpClientRequest;

/**
 * <p>
 * Wrapper for {@link HttpUriRequest} to support hashing and equality for
 * the purpose of HTTP caching.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 08/14/12
 * @since 1.0
 */
public class HashableHttpRequest implements HttpClientRequest {

	private HttpUriRequest mHttpRequest;
	private Map<String, String> mHeaders;

	/**
	 * Creates a new {@code HashableHttpRequest} for the given
	 * {@link HttpUriRequest}.
	 * 
	 * @param request
	 *            the {@code HttpUriRequest} to wrap
	 */
	public HashableHttpRequest(HttpUriRequest request) {
		mHttpRequest = request;
		mHeaders = new HashMap<String, String>();
	}
	
	@Override
	public HttpUriRequest unwrap() {
		return mHttpRequest;
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
	public String getRequestUri() {
		return mHttpRequest.getURI().toString();
	}
	
	@Override
	public String getHttpMethod() {
		return mHttpRequest.getMethod();
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int hash = 7;
		hash *= PRIME + mHttpRequest.getMethod().hashCode();
		for (Header header : mHttpRequest.getAllHeaders()) {
			hash *= PRIME + header.getName().hashCode();
			hash *= PRIME + header.getValue().hashCode();
		}
		hash *= PRIME
				+ mHttpRequest.getProtocolVersion().getProtocol()
						.hashCode();
		hash *= PRIME + mHttpRequest.getProtocolVersion().getMajor();
		hash *= PRIME + mHttpRequest.getProtocolVersion().getMinor();
		hash *= PRIME + mHttpRequest.getURI().toString().hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (getClass() != other.getClass())
			return false;
		HashableHttpRequest otherRequest = (HashableHttpRequest) other;
		Header[] headers = mHttpRequest.getAllHeaders();
		Header[] otherHeaders = otherRequest.mHttpRequest.getAllHeaders();
		if (headers.length != otherHeaders.length)
			return false;
		boolean match = false;
		for (Header header : headers) {
			for (Header otherHeader : otherHeaders) {
				if (header.getName().equals(otherHeader.getName())
						&& header.getValue().equals(otherHeader.getValue())) {
					match = true;
					break;
				}
			}
			if (!match)
				return false;
		}
		return mHttpRequest.getMethod().equals(
				otherRequest.mHttpRequest.getMethod())
				&& mHttpRequest
						.getProtocolVersion()
						.getProtocol()
						.equals(otherRequest.mHttpRequest
								.getProtocolVersion().getProtocol())
				&& mHttpRequest.getProtocolVersion().getMajor() == otherRequest.mHttpRequest
						.getProtocolVersion().getMajor()
				&& mHttpRequest.getProtocolVersion().getMinor() == otherRequest.mHttpRequest
						.getProtocolVersion().getMinor()
				&& mHttpRequest.getURI().equals(
						otherRequest.mHttpRequest.getURI());
	}
	
	@Override
	public String toString() {
		return "[" + mHttpRequest.getMethod() + " " + mHttpRequest.getURI().toString() + "]";
	}
	
}