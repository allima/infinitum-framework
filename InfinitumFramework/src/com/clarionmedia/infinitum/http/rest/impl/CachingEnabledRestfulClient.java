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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.http.impl.HashableHttpRequest;
import com.clarionmedia.infinitum.http.rest.RestfulClient;
import com.clarionmedia.infinitum.logging.Logger;

/**
 * <p>
 * Implementation of {@link RestfulClient} with caching support.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 07/04/12
 * @since 1.0
 */
public class CachingEnabledRestfulClient implements RestfulClient {

	protected Logger mLogger;
	protected HttpParams mHttpParams;

	/**
	 * Creates a new {@code CachingEnabledRestfulClient}.
	 */
	public CachingEnabledRestfulClient(InfinitumContext context) {
		mLogger = Logger.getInstance(context, getClass().getSimpleName());
		mHttpParams = new BasicHttpParams();
	}

	@Override
	public RestResponse executeGet(String uri) {
		return executeRequest(new HashableHttpRequest(new HttpGet(uri)));
	}

	@Override
	public RestResponse executeGet(String uri, Map<String, String> headers) {
		HttpGet httpGet = new HttpGet(uri);
		for (Entry<String, String> header : headers.entrySet()) {
			httpGet.addHeader(header.getKey(), header.getValue());
		}
		return executeRequest(new HashableHttpRequest(httpGet));
	}

	@Override
	public RestResponse executePost(String uri, String messageBody,
			String contentType) {
		HttpPost httpPost = new HttpPost(uri);
		httpPost.addHeader("content-type", contentType);
		try {
			httpPost.setEntity(new StringEntity(messageBody, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			mLogger.error(
					"Unable to send POST request (could not encode message body)",
					e);
			return null;
		}
		return executeRequest(new HashableHttpRequest(httpPost));
	}

	@Override
	public RestResponse executePost(String uri, String messageBody,
			String contentType, Map<String, String> headers) {
		HttpPost httpPost = new HttpPost(uri);
		for (Entry<String, String> header : headers.entrySet()) {
			httpPost.addHeader(header.getKey(), header.getValue());
		}
		httpPost.addHeader("content-type", contentType);
		try {
			httpPost.setEntity(new StringEntity(messageBody, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			mLogger.error(
					"Unable to send POST request (could not encode message body)",
					e);
			return null;
		}
		return executeRequest(new HashableHttpRequest(httpPost));
	}

	@Override
	public RestResponse executePost(String uri, HttpEntity httpEntity) {
		HttpPost httpPost = new HttpPost(uri);
		httpPost.addHeader("content-type", httpEntity.getContentType()
				.getValue());
		httpPost.setEntity(httpEntity);
		return executeRequest(new HashableHttpRequest(httpPost));
	}

	@Override
	public RestResponse executePost(String uri, HttpEntity httpEntity,
			Map<String, String> headers) {
		HttpPost httpPost = new HttpPost(uri);
		for (Entry<String, String> header : headers.entrySet()) {
			httpPost.addHeader(header.getKey(), header.getValue());
		}
		httpPost.addHeader("content-type", httpEntity.getContentType()
				.getValue());
		httpPost.setEntity(httpEntity);
		return executeRequest(new HashableHttpRequest(httpPost));
	}

	@Override
	public RestResponse executePost(String uri, InputStream messageBody,
			int messageBodyLength, String contentType) {
		HttpPost httpPost = new HttpPost(uri);
		httpPost.addHeader("content-type", contentType);
		httpPost.setEntity(new InputStreamEntity(messageBody, messageBodyLength));
		return executeRequest(new HashableHttpRequest(httpPost));
	}

	@Override
	public RestResponse executePost(String uri, InputStream messageBody,
			int messageBodyLength, String contentType,
			Map<String, String> headers) {
		HttpPost httpPost = new HttpPost(uri);
		for (Entry<String, String> header : headers.entrySet()) {
			httpPost.addHeader(header.getKey(), header.getValue());
		}
		httpPost.addHeader("content-type", contentType);
		httpPost.setEntity(new InputStreamEntity(messageBody, messageBodyLength));
		return executeRequest(new HashableHttpRequest(httpPost));
	}

	@Override
	public RestResponse executeDelete(String uri) {
		return executeRequest(new HashableHttpRequest(new HttpDelete(uri)));
	}

	@Override
	public RestResponse executeDelete(String uri, Map<String, String> headers) {
		HttpDelete httpDelete = new HttpDelete(uri);
		for (Entry<String, String> header : headers.entrySet()) {
			httpDelete.addHeader(header.getKey(), header.getValue());
		}
		return executeRequest(new HashableHttpRequest(httpDelete));
	}

	@Override
	public RestResponse executePut(String uri, String messageBody,
			String contentType) {
		HttpPut httpPut = new HttpPut(uri);
		httpPut.addHeader("content-type", contentType);
		try {
			httpPut.setEntity(new StringEntity(messageBody, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			mLogger.error(
					"Unable to send PUT request (could not encode message body)",
					e);
			return null;
		}
		return executeRequest(new HashableHttpRequest(httpPut));
	}

	@Override
	public RestResponse executePut(String uri, String messageBody,
			String contentType, Map<String, String> headers) {
		HttpPut httpPut = new HttpPut(uri);
		for (Entry<String, String> header : headers.entrySet()) {
			httpPut.addHeader(header.getKey(), header.getValue());
		}
		httpPut.addHeader("content-type", contentType);
		try {
			httpPut.setEntity(new StringEntity(messageBody, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			mLogger.error(
					"Unable to send PUT request (could not encode message body)",
					e);
			return null;
		}
		return executeRequest(new HashableHttpRequest(httpPut));
	}

	@Override
	public RestResponse executePut(String uri, HttpEntity httpEntity) {
		HttpPut httpPut = new HttpPut(uri);
		httpPut.addHeader("content-type", httpEntity.getContentType()
				.getValue());
		httpPut.setEntity(httpEntity);
		return executeRequest(new HashableHttpRequest(httpPut));
	}

	@Override
	public RestResponse executePut(String uri, HttpEntity httpEntity,
			Map<String, String> headers) {
		HttpPut httpPut = new HttpPut(uri);
		for (Entry<String, String> header : headers.entrySet()) {
			httpPut.addHeader(header.getKey(), header.getValue());
		}
		httpPut.addHeader("content-type", httpEntity.getContentType()
				.getValue());
		httpPut.setEntity(httpEntity);
		return executeRequest(new HashableHttpRequest(httpPut));
	}

	@Override
	public RestResponse executePut(String uri, InputStream messageBody,
			int messageBodyLength, String contentType) {
		HttpPut httpPut = new HttpPut(uri);
		httpPut.addHeader("content-type", contentType);
		httpPut.setEntity(new InputStreamEntity(messageBody, messageBodyLength));
		return executeRequest(new HashableHttpRequest(httpPut));
	}

	@Override
	public RestResponse executePut(String uri, InputStream messageBody,
			int messageBodyLength, String contentType,
			Map<String, String> headers) {
		HttpPut httpPut = new HttpPut(uri);
		for (Entry<String, String> header : headers.entrySet()) {
			httpPut.addHeader(header.getKey(), header.getValue());
		}
		httpPut.addHeader("content-type", contentType);
		httpPut.setEntity(new InputStreamEntity(messageBody, messageBodyLength));
		return executeRequest(new HashableHttpRequest(httpPut));
	}

	@Override
	public void setConnectionTimeout(int timeout) {
		HttpConnectionParams.setConnectionTimeout(mHttpParams, timeout);
	}

	@Override
	public void setResponseTimeout(int timeout) {
		HttpConnectionParams.setSoTimeout(mHttpParams, timeout);
	}

	@Override
	public void setHttpParams(HttpParams httpParams) {
		mHttpParams = httpParams;
	}

	private RestResponse executeRequest(HashableHttpRequest hashableHttpRequest) {
		HttpUriRequest httpRequest = hashableHttpRequest.unwrap();
		mLogger.debug("Sending " + httpRequest.getMethod() + " request to " + httpRequest.getURI() + " with " + httpRequest.getAllHeaders().length + " headers");
		HttpClient httpClient = new DefaultHttpClient(mHttpParams);
		try {
			HttpResponse response = httpClient.execute(httpRequest);
			RestResponse restResponse = new RestResponse(response);
			StatusLine statusLine = response.getStatusLine();
			restResponse.setStatusCode(statusLine.getStatusCode());
			for (Header header : response.getAllHeaders()) {
				restResponse.addHeader(header.getName(), header.getValue());
			}
			HttpEntity entity = response.getEntity();
			if (entity == null) {
				restResponse.setResponseData(new byte[] {});
			} else {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				entity.writeTo(out);
				out.close();
				restResponse.setResponseData(out.toByteArray());
			}
			return restResponse;
		} catch (ClientProtocolException e) {
			mLogger.error("Unable to send " + httpRequest.getMethod() + " request", e);
			return null;
		} catch (IOException e) {
			mLogger.error("Unable to read web service response", e);
			return null;
		}
	}

}
