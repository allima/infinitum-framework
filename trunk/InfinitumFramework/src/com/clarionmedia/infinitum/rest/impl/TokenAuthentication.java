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
 * Used for token-based authentication.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/21/12
 */
public class TokenAuthentication implements AuthenticationStrategy {
	
	private static final String ENCODING = "UTF-8";
	
	private String mTokenName;
	private String mToken;
	private TokenGenerator mGenerator;
	
	@Override
	public String getAuthenticationString() {
		return mTokenName + "=" + mToken;
	}

	public void setTokenName(String tokenName) {
		try {
			mTokenName = URLEncoder.encode(tokenName, ENCODING);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getTokenName() {
		return mTokenName;
	}

	public void setToken(String token) {
		try {
			mToken = URLEncoder.encode(token, ENCODING);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getToken() {
		if (mGenerator != null)
			return mGenerator.generateToken();
		return mToken;
	}
	
	public void setTokenGenerator(TokenGenerator generator) {
		mGenerator = generator;
	}
	
	public void clearTokenGenerator() {
		mGenerator = null;
	}
	
	public interface TokenGenerator {
		String generateToken();
	}

}
