package com.clarionmedia.infinitum.http.rest;

import com.clarionmedia.infinitum.http.rest.impl.SharedSecretAuthentication;

/**
 * <p>
 * {@code TokenGenerator} is responsible for generating shared secret tokens.
 * Provide an implementation of this if an unchanging shared secret is
 * undesirable. A {@code TokenGenerator} can be injected into a
 * {@link SharedSecretAuthentication} bean.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/21/12
 */
public interface TokenGenerator {

	/**
	 * Creates a new shared secret token.
	 * 
	 * @return authentication token
	 */
	String generateToken();

}