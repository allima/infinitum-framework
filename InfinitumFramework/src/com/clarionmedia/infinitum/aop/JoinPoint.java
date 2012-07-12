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

import java.lang.reflect.Method;

/**
 * <p>
 * Provides contextual information about a join point.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 07/12/12
 * @since 1.0
 */
public class JoinPoint {

	public Object getmTarget() {
		return mTarget;
	}

	private Object mTarget;
	private Method mMethod;
	private Object[] mArguments;

	/**
	 * Creates a new {@code JoinPoint}.
	 * 
	 * @param target
	 *            the {@link Object} in which {@code method} is being invoked
	 *            for
	 * @param method
	 *            the {@link Method} being invoked
	 * @param arguments
	 *            the parameter arguments passed to {@code method}
	 */
	public JoinPoint(Object target, Method method, Object[] arguments) {
		mTarget = target;
		mMethod = method;
		mArguments = arguments;
	}

	public void setTarget(Object target) {
		mTarget = target;
	}

	public Method getMethod() {
		return mMethod;
	}

	public void setMethod(Method method) {
		mMethod = method;
	}

	public Object[] getArguments() {
		return mArguments;
	}

	public void setArguments(Object[] arguments) {
		mArguments = arguments;
	}

}
