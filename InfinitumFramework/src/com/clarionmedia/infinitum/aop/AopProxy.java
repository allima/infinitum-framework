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
	
	public abstract boolean isProxy(Object object);
	
	public abstract Object getProxy();
	
	public abstract InvocationHandler getInvocationHandler(Object proxy);

}
