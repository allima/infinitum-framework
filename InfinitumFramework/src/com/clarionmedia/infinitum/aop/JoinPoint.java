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
import com.clarionmedia.infinitum.aop.annotation.Aspect;

/**
 * <p>
 * Provides contextual information about a join point. A {@code JoinPoint} is
 * passed to an {@link Aspect} advice when invoked to provide it with reflective
 * state data.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 07/12/12
 */
public interface JoinPoint {

	public static enum Location {
		Before, After, Around
	};

	/**
	 * Returns the {@link Method} being invoked at this {@code JoinPoint}.
	 * 
	 * @return {@code Method} to be advised
	 */
	Method getMethod();

	/**
	 * Sets the {@link Method} to be invoked at this {@code JoinPoint}.
	 * 
	 * @param method
	 *            {@code Method} to be advised
	 */
	void setMethod(Method method);

	/**
	 * Returns the arguments to be passed into this {@code JoinPoint}.
	 * 
	 * @return {@code Object[]} of arguments
	 */
	Object[] getArguments();

	/**
	 * Sets the arguments to be passed into this {@code JoinPoint}.
	 * 
	 * @param args
	 *            {@code Object[]} of arguments
	 */
	void setArguments(Object[] args);

	/**
	 * Returns the target {@link Object} to be invoked at this {@code JoinPoint}
	 * .
	 * 
	 * @return {@code Object} where {@code JoinPoint} is to be executed
	 */
	Object getTarget();

	/**
	 * Sets the target {@link Object} to be invoked at this {@code JoinPoint}.
	 * 
	 * @param target
	 *            {@code Object} where {@code JoinPoint} is to be executed
	 */
	void setTarget(Object target);

	/**
	 * Returns the {@link Class} of the {@code JoinPoint} target.
	 * 
	 * @return {@code Class} of target
	 */
	Class<?> getTargetType();

	/**
	 * Returns the name of the bean to be invoked at this {@code JoinPoint}.
	 * 
	 * @return bean name
	 */
	String getBeanName();

	/**
	 * Sets the name of the bean to be invoked at this {@code JoinPoint}.
	 * 
	 * @param beanName
	 *            bean name
	 */
	void setBeanName(String beanName);

	/**
	 * Indicates if the {@code JoinPoint} applies to the entire target
	 * {@link Class} or just a specific {@link Method}.
	 * 
	 * @return {@code true} if it is {@code Class} scope, {@code false} if not
	 */
	boolean isClassScope();

	/**
	 * Sets the value indicating if the {@code JoinPoint} applies to the entire
	 * target {@link Class} or just a specific {@link Method}.
	 * 
	 * @param isClassScope
	 *            {@code true} if it is {@code Class} scope, {@code false} if
	 *            not
	 */
	void setClassScope(boolean isClassScope);

	/**
	 * Returns the advice location.
	 * 
	 * @return advice location
	 */
	Location getLocation();

	/**
	 * Sets the advice location.
	 * 
	 * @param location
	 *            advice location
	 */
	void setLocation(Location location);

}
