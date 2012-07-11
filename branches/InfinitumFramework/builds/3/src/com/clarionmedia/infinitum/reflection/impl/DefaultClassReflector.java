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

package com.clarionmedia.infinitum.reflection.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.internal.StringUtil;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.TypeResolutionPolicy;
import com.clarionmedia.infinitum.orm.persistence.impl.DefaultTypeResolutionPolicy;
import com.clarionmedia.infinitum.reflection.ClassReflector;

/**
 * <p>
 * This class provides reflection methods for working with classes contained
 * within projects that are using Infinitum.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/15/12
 */
public class DefaultClassReflector implements ClassReflector {

	private TypeResolutionPolicy mTypePolicy;

	/**
	 * Constructs a new {@code DefaultClassReflector}.
	 */
	public DefaultClassReflector() {
		mTypePolicy = new DefaultTypeResolutionPolicy();
	}

	@Override
	public Object invokeGetter(Field field, Object object) {
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
			throw new InfinitumRuntimeException("Unable to invoke getter for object of type '"
					+ object.getClass().getName() + "'");
		} catch (NoSuchMethodException e) {
			throw new ModelConfigurationException("Field '" + field.getName() + "' in model '"
					+ object.getClass().getName() + "' does not have an associated getter method.");
		} catch (IllegalArgumentException e) {
			throw new InfinitumRuntimeException("Unable to invoke getter for object of type '"
					+ object.getClass().getName() + "' (illegal argument)");
		} catch (IllegalAccessException e) {
			throw new InfinitumRuntimeException("Unable to invoke getter for object of type '"
					+ object.getClass().getName() + "' (illegal access)");
		} catch (InvocationTargetException e) {
			throw new InfinitumRuntimeException("Unable to invoke getter for object of type '"
					+ object.getClass().getName() + "'");
		}
	}

	@Override
	public boolean isNull(Object object) {
		if (object == null)
			return true;
		if (mTypePolicy.isDomainProxy(object.getClass()))
			return object.getClass().isInstance(object);
		return false;
	}

	@Override
	public List<Field> getAllFields(Class<?> c) {
		return getAllFieldsRec(c, new LinkedList<Field>());
	}

	@Override
	public Field getField(Class<?> c, String name) {
		for (Field f : getAllFields(c)) {
			if (f.getName().equals(name))
				return f;
		}
		return null;
	}

	private List<Field> getAllFieldsRec(Class<?> c, List<Field> fields) {
		Class<?> superClass = c.getSuperclass();
		if (superClass != null)
			getAllFieldsRec(superClass, fields);
		fields.addAll(Arrays.asList(c.getDeclaredFields()));
		return fields;
	}

}
