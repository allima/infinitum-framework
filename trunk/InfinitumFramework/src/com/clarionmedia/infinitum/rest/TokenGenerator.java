package com.clarionmedia.infinitum.rest;

/**
 * <p>
 * {@code TokenGenerator} is responsible for generating shared secret tokens.
 * Provide an implementation of this if an unchanging shared secret is
 * undesirable.
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