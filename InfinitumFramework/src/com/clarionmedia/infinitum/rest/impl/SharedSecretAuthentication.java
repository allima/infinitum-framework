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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.clarionmedia.infinitum.rest.AuthenticationStrategy;

/**
 * <p>
 * Used for token-based/shared-secret authentication.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/21/12
 */
public class SharedSecretAuthentication implements AuthenticationStrategy {

	private static final String ENCODING = "UTF-8";

	private String mTokenName;
	private String mToken;
	private TokenGenerator mGenerator;

	@Override
	public String getAuthenticationString() {
		return mTokenName + "=" + mToken;
	}

	/**
	 * Sets the token key name. The authentication {@code String}
	 * {@code SharedSecretAuthentication} generates, appears as
	 * {@code tokenName=token}, where {@code tokenName} is the token key name
	 * provided to this method and {@code token} is the shared secret.
	 * 
	 * @param tokenName
	 *            the token name to use for authentication
	 */
	public void setTokenName(String tokenName) {
		try {
			mTokenName = URLEncoder.encode(tokenName, ENCODING);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Returns the token key name.
	 * 
	 * @return token name
	 */
	public String getTokenName() {
		return mTokenName;
	}

	/**
	 * Sets the token value. The authentication {@code String}
	 * {@code SharedSecretAuthentication} generates, appears as
	 * {@code tokenName=token}, where {@code tokenName} is the token key name
	 * and {@code token} is the shared secret provided to this method.
	 * 
	 * @param token
	 *            the shared secret to use for authentication
	 */
	public void setToken(String token) {
		try {
			mToken = URLEncoder.encode(token, ENCODING);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Returns the token value. This is either the value provided to
	 * {@link SharedSecretAuthentication#setToken(String)} or the value
	 * generated from the {@link TokenGenerator} if one has been provided.
	 * 
	 * @return shared secret
	 */
	public String getToken() {
		if (mGenerator != null)
			return mGenerator.generateToken();
		return mToken;
	}

	/**
	 * Registers a {@link TokenGenerator} for this
	 * {@code SharedSecretAuthentication} strategy. The {@code TokenGenerator}
	 * is responsible for creating shared secrets.
	 * 
	 * @param generator
	 *            the {@code TokenGenerator} to register
	 */
	public void setTokenGenerator(TokenGenerator generator) {
		mGenerator = generator;
	}

	/**
	 * Removes any registered {@link TokenGenerator} from this
	 * {@code SharedSecretAuthentication} strategy.
	 */
	public void clearTokenGenerator() {
		mGenerator = null;
	}

	/**
	 * <p>
	 * {@code TokenGenerator} is responsible for generating shared secret
	 * tokens. Provide an implementation of this if an unchanging shared secret
	 * is undesirable.
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

}
