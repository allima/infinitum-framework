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

package com.clarionmedia.infinitum.orm.persistence;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Used to proxy an {@link Object} which has been lazily loaded. Every method
 * called on the {@code Object} will pass through this {@link InvocationHandler}
 * .
 * 
 * @author Tyler Treat
 * @version 1.0 03/12/12
 */
public abstract class LazyLoadedObject implements InvocationHandler {

	private Object mTarget;

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		if (mTarget == null) {
			mTarget = loadObject();
		}
		return method.invoke(mTarget, args);
	}

	/**
	 * Loads the proxied {@link Object}.
	 * 
	 * @return {@code Object}
	 */
	protected abstract Object loadObject();

}
