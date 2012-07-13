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

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import android.content.Context;

import com.clarionmedia.infinitum.aop.AopProxy;
import com.clarionmedia.infinitum.aop.Pointcut;
import com.clarionmedia.infinitum.internal.DexCaching;
import com.clarionmedia.infinitum.internal.Preconditions;
import com.google.dexmaker.stock.ProxyBuilder;

/**
 * <p>
 * Implementation of {@link AopProxy} that relies on DexMaker in order to proxy
 * non-final classes in addition to interfaces.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 07/13/12
 * @since 1.0
 */
public class DexMakerProxy extends AopProxy {

	private Object mTarget;
	private Pointcut mPointcut;
	private Context mContext;

	public DexMakerProxy(Context context, Object target, Pointcut pointcut) {
		Preconditions.checkNotNull(pointcut);
		mTarget = target;
		mPointcut = pointcut;
		mContext = context;
	}
	
	public static DexMakerProxy getProxy(Object object) {
		if (!ProxyBuilder.isProxyClass(object.getClass()))
			return null;
		return (DexMakerProxy) ProxyBuilder.getInvocationHandler(object);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		// TODO
		return method.invoke(mTarget, args);
	}

	@Override
	public Object getTarget() {
		return mTarget;
	}

	@Override
	public Object getProxy() {
		try {
			return ProxyBuilder.forClass(mTarget.getClass())
			.handler(this)
			.dexCache(DexCaching.getDexCache(mContext)).build();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean isProxy(Object object) {
		return ProxyBuilder.isProxyClass(object.getClass());
	}
	
	@Override
	public InvocationHandler getInvocationHandler(Object proxy) {
		return ProxyBuilder.getInvocationHandler(proxy);
	}

}
