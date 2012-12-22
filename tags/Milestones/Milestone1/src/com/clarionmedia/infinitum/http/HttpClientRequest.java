package com.clarionmedia.infinitum.http;

/**
 * <p>
 * Encapsulates an HTTP response message sent back from a server.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 08/15/12
 * @since 1.0
 */
public interface HttpClientRequest extends HttpClientMessage {

	/**
	 * Returns the request URI.
	 * 
	 * @return uri
	 */
	String getRequestUri();

	/**
	 * Returns the HTTP method name, such as GET, POST, PUT, etc.
	 * 
	 * @return HTTP method name
	 */
	String getHttpMethod();

}
