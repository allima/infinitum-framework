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

package com.clarionmedia.infinitum.aop;

import java.lang.reflect.InvocationHandler;

import com.clarionmedia.infinitum.aop.impl.DexMakerProxy;

/**
 * <p>
 * Provides proxy support for the AOP framework.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 07/13/12
 * @since 1.0
 */
public abstract class AopProxy implements InvocationHandler {

	protected Object mTarget;
	
	/**
	 * Retrieves an {@code AopProxy} instance for the given proxy.
	 * 
	 * @param object
	 *            the {@link Object} to retrieve a proxy instance for
	 * @return {@code AopProxy} or {@code null} if {@code object} is not a proxy
	 */
	public static AopProxy getProxy(Object object) {
		AopProxy proxy = DexMakerProxy.getProxy(object);
		if (proxy != null)
			return proxy;
		// TODO
		return null;
	}

	/**
	 * Returns the proxied {@link Object}.
	 * 
	 * @return target {@code Object}
	 */
	public abstract Object getTarget();

	/**
	 * Indicates if the given {@link Object} is a proxy or not
	 * 
	 * @param object
	 *            the {@code Object} to check
	 * @return {@code true} if it is a proxy, {@code false} if not
	 */
	public abstract boolean isProxy(Object object);

	/**
	 * Creates a new proxy
	 * 
	 * @return proxy {@link Object}
	 */
	public abstract Object getProxy();

	/**
	 * Returns the {@link InvocationHandler} for the given proxy.
	 * 
	 * @param proxy
	 *            the proxy to retrieve the {@code InvocationHandler} for
	 * @return {@code InvocationHandler} or {@code null} if the given
	 *         {@code Object} is not a proxy
	 */
	public abstract InvocationHandler getInvocationHandler(Object proxy);

}
