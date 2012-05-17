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

package com.clarionmedia.infinitum.reflection;

import java.lang.reflect.Field;
import java.util.List;

/**
 * <p>
 * This interface provides reflection methods for working with classes
 * contained within projects that are using Infinitum.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 05/17/12
 */
public interface ClassReflector {
	
	/**
	 * Retrieves the value of the given {@link Field} for the specified
	 * {@link Object} by invoking its getter method.
	 * 
	 * @param field
	 *            the {@code Field} to retrieve the value for
	 * @param object
	 *            the {@code Object} to retrieve the value for
	 * @return {@code Field} value
	 */
	Object invokeGetter(Field field, Object object);

	/**
	 * Indicates if the given {@link Object} or {@code Object} proxy is
	 * {@code null}.
	 * 
	 * @param object
	 *            the {@code Object} to check
	 * @return {@code true} if {@code object} is {@code null}, {@code false} if
	 *         not
	 */
	boolean isNull(Object object);

	/**
	 * Retrieves all {@code Fields} for the given {@link Class}.
	 * 
	 * @param c
	 *            the {@code Class} to get {@code Fields} for
	 * @return {@link List} of {@code Fields}
	 */
	List<Field> getAllFields(Class<?> c);

	/**
	 * Retrieves the {@link Field} with the given name for the given
	 * {@link Class}.
	 * 
	 * @param c
	 *            the {@code Class} to retrieve the {@code Field} from
	 * @param name
	 *            the name of the {@code Field}
	 * @return {@code Field} with the given name or {@code null} if it does not
	 *         exist
	 */
	Field getField(Class<?> c, String name);

}
