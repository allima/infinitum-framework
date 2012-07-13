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

package com.clarionmedia.infinitum.aop.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Set;

import com.clarionmedia.infinitum.aop.JoinPoint;

/**
 * <p>
 * Represents an advised {@link Object} in which aspect advice has been woven
 * in.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 07/13/12
 * @since 1.0
 */
public class AdvisedObject implements InvocationHandler {

	private Set<JoinPoint> mPointcut;

	/**
	 * Creates a new {@code AdvisedObject}.
	 * 
	 * @param pointcut
	 *            the {@link Set} of {@code JoinPoint} instances for this
	 *            {@link Object}
	 */
	public AdvisedObject(Set<JoinPoint> pointcut) {
		mPointcut = pointcut;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

}
