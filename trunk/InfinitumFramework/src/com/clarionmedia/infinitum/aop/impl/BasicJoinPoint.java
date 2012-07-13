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

import java.lang.reflect.Method;
import com.clarionmedia.infinitum.aop.JoinPoint;

/**
 * <p>
 * Basic implementation of {@link JoinPoint}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 07/12/12
 * @since 1.0
 */
public class BasicJoinPoint implements JoinPoint {

	// TODO Implement equals() and hashCode()!

	private Object mTarget;
	private Method mMethod;
	private Object[] mArguments;
	private String mBeanName;
	private boolean mIsClassScope;
	private Location mLocation;

	/**
	 * Creates a new {@code BasicJoinPoint}.
	 * 
	 * @param location
	 *            advice location
	 */
	public BasicJoinPoint(Location location) {
		mLocation = location;
	}

	@Override
	public Method getMethod() {
		return mMethod;
	}

	@Override
	public Object[] getArguments() {
		return mArguments;
	}

	@Override
	public Object getTarget() {
		return mTarget;
	}

	@Override
	public void setBeanName(String beanName) {
		mBeanName = beanName;
	}

	@Override
	public String getBeanName() {
		return mBeanName;
	}

	@Override
	public void setMethod(Method method) {
		mMethod = method;
	}

	@Override
	public void setArguments(Object[] args) {
		mArguments = args;
	}

	@Override
	public void setTarget(Object target) {
		mTarget = target;
	}

	@Override
	public boolean isClassScope() {
		return mIsClassScope;
	}

	@Override
	public void setClassScope(boolean isClassScope) {
		mIsClassScope = isClassScope;
	}

	@Override
	public Location getLocation() {
		return mLocation;
	}

	@Override
	public void setLocation(Location location) {
		mLocation = location;
	}

}
