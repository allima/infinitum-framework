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

import com.clarionmedia.infinitum.rest.impl.SharedSecretAuthentication;

/**
 * <p>
 * Describes how web service requests are authenticated. This should be
 * implemented for specific web service authentication strategies. If using
 * token or shared-secret authentication, {@link SharedSecretAuthentication}
 * should be used.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/21/12
 */
public interface AuthenticationStrategy {

	/**
	 * Retrieves the authentication {@link String} for this
	 * {@code AuthenticationStrategy}. The authentication {@code String} is the
	 * value used to authenticate web service requests and is typically appended
	 * to the request URL.
	 * 
	 * @return authentication {@code String}
	 */
	String getAuthenticationString();

}
