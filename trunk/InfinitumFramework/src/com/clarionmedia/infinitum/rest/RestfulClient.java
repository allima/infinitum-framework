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

import java.io.InputStream;
import java.util.Map;

/**
 * <p>
 * Provides an interface for communicating with a RESTful web service.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0
 * @since 07/06/12
 */
public interface RestfulClient {

	/**
	 * Executes an HTTP GET request to the given URI.
	 * 
	 * @param uri
	 *            the URI to execute the request for
	 * @return HTTP response
	 */
	RestResponse executeGet(String uri);

	/**
	 * Executes an HTTP GET request to the given URI
	 * 
	 * @param uri
	 *            the URI to execute the request for
	 * @param headers
	 *            the headers to send with the request
	 * @return HTTP response
	 */
	RestResponse executeGet(String uri, Map<String, String> headers);

	/**
	 * Executes an HTTP POST request to the given URI using the given content
	 * type and message body.
	 * 
	 * @param uri
	 *            the URI to execute the request for
	 * @param messageBody
	 *            the message body
	 * @param contentType
	 *            the content type of the message body
	 * @return HTTP response
	 */
	RestResponse executePost(String uri, String messageBody, String contentType);

	/**
	 * Executes an HTTP POST request to the given URI using the given content
	 * type, message body, and headers.
	 * 
	 * @param uri
	 *            the URI to execute the request for
	 * @param messageBody
	 *            the message body
	 * @param contentType
	 *            the content type of the message body
	 * @param headers
	 *            the headers to send with the request
	 * 
	 * @return HTTP response
	 */
	RestResponse executePost(String uri, String messageBody, String contentType, Map<String, String> headers);

	/**
	 * Executes an HTTP POST request to the given URI using the given content
	 * type and message body.
	 * 
	 * @param uri
	 *            the URI to execute the request for
	 * @param messageBody
	 *            the message body
	 * @param messageBodyLength
	 *            the length of the message body
	 * @param contentType
	 *            the content type of the message body
	 * @return HTTP response
	 */
	RestResponse executePost(String uri, InputStream messageBody, int messageBodyLength, String contentType);

	/**
	 * Executes an HTTP POST request to the given URI using the given content
	 * type, message body, and headers.
	 * 
	 * @param uri
	 *            the URI to execute the request for
	 * @param messageBody
	 *            the message body
	 * @param messageBodyLength
	 *            the length of the message body
	 * @param contentType
	 *            the content type of the message body
	 * @param headers
	 *            the headers to send with the request
	 * @return HTTP response
	 */
	RestResponse executePost(String uri, InputStream messageBody, int messageBodyLength, String contentType,
			Map<String, String> headers);

	/**
	 * Executes an HTTP DELETE request to the given URI.
	 * 
	 * @param uri
	 *            the URI to execute the request for
	 * @return HTTP response
	 */
	RestResponse executeDelete(String uri);

	/**
	 * Executes an HTTP DELETE request to the given URI using the given headers.
	 * 
	 * @param uri
	 *            the URI to execute the request for
	 * @param headers
	 *            the headers to send with the request
	 * @return HTTP response
	 */
	RestResponse executeDelete(String uri, Map<String, String> headers);

	/**
	 * Executes an HTTP PUT request to the given URI using the given content
	 * type and message body.
	 * 
	 * @param uri
	 *            the URI to execute the request for
	 * @param messageBody
	 *            the message body
	 * @param contentType
	 *            the content type of the message body
	 * @return HTTP response
	 */
	RestResponse executePut(String uri, String messageBody, String contentType);

	/**
	 * Executes an HTTP PUT request to the given URI using the given content
	 * type, message body, and headers.
	 * 
	 * @param uri
	 *            the URI to execute the request for
	 * @param messageBody
	 *            the message body
	 * @param contentType
	 *            the content type of the message body
	 * @param headers
	 *            the headers to send with the request
	 * 
	 * @return HTTP response
	 */
	RestResponse executePut(String uri, String messageBody, String contentType, Map<String, String> headers);

	/**
	 * Executes an HTTP PUT request to the given URI using the given content
	 * type and message body.
	 * 
	 * @param uri
	 *            the URI to execute the request for
	 * @param messageBody
	 *            the message body
	 * @param messageBodyLength
	 *            the length of the message body
	 * @param contentType
	 *            the content type of the message body
	 * @return HTTP response
	 */
	RestResponse executePut(String uri, InputStream messageBody, int messageBodyLength, String contentType);

	/**
	 * Executes an HTTP PUT request to the given URI using the given content
	 * type, message body, and headers.
	 * 
	 * @param uri
	 *            the URI to execute the request for
	 * @param messageBody
	 *            the message body
	 * @param messageBodyLength
	 *            the length of the message body
	 * @param contentType
	 *            the content type of the message body
	 * @param headers
	 *            the headers to send with the request
	 * @return HTTP response
	 */
	RestResponse executePut(String uri, InputStream messageBody, int messageBodyLength, String contentType,
			Map<String, String> headers);

	/**
	 * Sets the connection timeout in milliseconds. This is the timeout used
	 * until a connection is established with the web service.
	 * 
	 * @param timeout
	 *            the timeout to set in milliseconds
	 */
	void setConnectionTimeout(int timeout);

	/**
	 * Sets the response timeout in milliseconds. This is the timeout for
	 * waiting for data from the web service. A timeout of zero is interpreted
	 * as an infinite timeout.
	 * 
	 * @param timeout
	 *            the timeout to set in milliseconds
	 */
	void setResponseTimeout(int timeout);

}
