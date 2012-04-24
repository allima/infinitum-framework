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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.clarionmedia.infinitum.internal.StringUtil;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.TypeResolution;

/**
 * <p>
 * This class provides static reflection methods for working with classes
 * contained within projects that are using Infinitum.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/15/12
 */
public class ClassReflector {

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
	public static Object invokeGetter(Field field, Object object) {
		field.setAccessible(true);
		String name = field.getName();
		if (name.startsWith("m") && name.length() > 1) {
			if (Character.isUpperCase(name.charAt(1)))
				name = name.substring(1);
		}
		try {
			Method getter = object.getClass().getMethod(StringUtil.getterName(name));
			return getter.invoke(object);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			throw new ModelConfigurationException("Field '" + field.getName() + "' in model '"
					+ object.getClass().getName() + "' does not have an associated getter method.");
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Indicates if the given {@link Object} or {@code Object} proxy is
	 * {@code null}.
	 * 
	 * @param object
	 *            the {@code Object} to check
	 * @return {@code true} if {@code object} is {@code null}, {@code false} if
	 *         not
	 */
	public static boolean isNull(Object object) {
		if (object == null)
			return true;
		if (TypeResolution.isDomainProxy(object.getClass()))
			return object.getClass().isInstance(object);
		return false;
	}

	/**
	 * Retrieves all {@code Fields} for the given {@link Class}.
	 * 
	 * @param c
	 *            the {@code Class} to get {@code Fields} for
	 * @return {@link List} of {@code Fields}
	 */
	public static List<Field> getAllFields(Class<?> c) {
		return getAllFieldsRec(c, new LinkedList<Field>());
	}

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
	public static Field getField(Class<?> c, String name) {
		for (Field f : getAllFields(c)) {
			if (f.getName().equals(name))
				return f;
		}
		return null;
	}

	private static List<Field> getAllFieldsRec(Class<?> c, List<Field> fields) {
		Class<?> superClass = c.getSuperclass();
		if (superClass != null)
			getAllFieldsRec(superClass, fields);
		fields.addAll(Arrays.asList(c.getDeclaredFields()));
		return fields;
	}

}
