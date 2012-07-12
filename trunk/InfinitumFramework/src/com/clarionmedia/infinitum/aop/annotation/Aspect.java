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

package com.clarionmedia.infinitum.aop.annotation;

import com.clarionmedia.infinitum.aop.JoinPoint;

/**
 * <p>
 * Separates cross-cutting concerns from core application code by providing
 * pointcut advice. This annotation indicates a {@link Class} contains advice
 * functionality which can be applied to a {@link JoinPoint}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 07/12/12
 * @since 1.0
 */
public @interface Aspect {

	/**
	 * Declares the name of this {@code Aspect}.
	 * 
	 * @return the {@code Aspect} name
	 */
	String value() default "";

}
