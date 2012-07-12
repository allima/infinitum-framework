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

	/**
	 * Returns the {@link Method} being invoked at this {@code JoinPoint}.
	 * 
	 * @return {@code Method} to be advised
	 */
	Method getMethod();

	/**
	 * Returns the arguments being passed to this {@code JoinPoint}.
	 * 
	 * @return {@code Object[]} of arguments
	 */
	Object[] getArguments();

	/**
	 * Returns the target {@link Object} being invoked at this {@code JoinPoint}
	 * .
	 * 
	 * @return {@code Object} where {@code JoinPoint} is being executed
	 */
	Object getTarget();

}
