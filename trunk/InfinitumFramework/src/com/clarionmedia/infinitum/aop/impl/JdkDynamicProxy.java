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
import java.lang.reflect.Proxy;

import com.clarionmedia.infinitum.aop.AdvisedProxy;
import com.clarionmedia.infinitum.aop.AopProxy;
import com.clarionmedia.infinitum.aop.JoinPoint;
import com.clarionmedia.infinitum.aop.Pointcut;
import com.clarionmedia.infinitum.internal.Preconditions;

/**
 * <p>
 * Implementation of {@link AopProxy} that relies on the JDK-provided
 * {@link Proxy} in order to proxy interfaces.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 07/14/12
 * @since 1.0
 */
public class JdkDynamicProxy extends AdvisedProxy {

	private Class<?>[] mInterfaces;
	
	public JdkDynamicProxy(Object target, Pointcut pointcut, Class<?>... interfaces) {
		super(pointcut);
	    Preconditions.checkNotNull(target);
		mInterfaces = interfaces;
		mTarget = target;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		// TODO revisit
		for (JoinPoint joinPoint : mBeforeAdvice) {
			joinPoint.setMethod(method);
			joinPoint.setArguments(args);
			joinPoint.invoke();
		}
		Object ret = method.invoke(mTarget, args);
		for (JoinPoint joinPoint : mAfterAdvice) {
			joinPoint.setMethod(method);
			joinPoint.setArguments(args);
			joinPoint.invoke();
		}
		return ret;
	}

	@Override
	public Object getTarget() {
		return mTarget;
	}

	@Override
	public boolean isProxy(Object object) {
		return Proxy.isProxyClass(object.getClass());
	}

	@Override
	public Object getProxy() {
		return Proxy.newProxyInstance(Thread.currentThread()
				.getContextClassLoader(), mInterfaces, this);
	}

	@Override
	public InvocationHandler getInvocationHandler(Object proxy) {
		if (!isProxy(proxy))
			return null;
		return Proxy.getInvocationHandler(proxy);
	}
}
