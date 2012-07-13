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

import java.util.List;
import java.util.Set;

import com.clarionmedia.infinitum.aop.annotation.Aspect;

/**
 * <p>
 * Provides facilities to resolve {@link Aspect} advice with pointcuts.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 07/12/12
 * @since 1.0
 */
public interface AspectWeaver {

	/**
	 * Returns a {@link Set} of all {@link JoinPoint} instances making up a
	 * pointcut for the given aspect. The {@code aspect} {@link Class} must be
	 * annotated with {@link Aspect} or a runtime exception will be thrown.
	 * 
	 * @param aspect
	 *            the aspect to qualify for
	 * @return {@code Set} of {@code JoinPoints}
	 */
	Set<JoinPoint> getPointcut(Class<?> aspect);

	/**
	 * Groups the "master" pointcut into a {@link List} of sub-pointcuts based
	 * on their associated type.
	 * 
	 * @param pointcut
	 *            the "master" pointcut
	 * @return grouped pointcuts
	 */
	List<Set<JoinPoint>> groupPointcutsByType(Set<JoinPoint> pointcut);

}
