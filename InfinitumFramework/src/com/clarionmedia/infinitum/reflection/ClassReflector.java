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
import com.clarionmedia.infinitum.internal.StringUtil;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;

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
	 * @throws ModelConfigurationException
	 *             if there is no getter method for the specified {@code Field}
	 */
	public static Object invokeGetter(Field field, Object object)
			throws ModelConfigurationException {
		field.setAccessible(true);
		String name = field.getName();
		if (name.startsWith("m") && name.length() > 1) {
			if (Character.isUpperCase(name.charAt(1)))
				name = name.substring(1);
		}
		try {
			Method getter = object.getClass().getMethod(
					StringUtil.getterName(name));
			return getter.invoke(object);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			throw new ModelConfigurationException("Field '" + field.getName()
					+ "' in model '" + object.getClass().getName()
					+ "' does not have an associated getter method.");
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

}
