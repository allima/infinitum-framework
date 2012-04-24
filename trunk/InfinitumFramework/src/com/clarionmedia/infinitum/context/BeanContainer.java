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

package com.clarionmedia.infinitum.context;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;
import com.clarionmedia.infinitum.internal.Pair;
import com.clarionmedia.infinitum.internal.Primitives;
import com.clarionmedia.infinitum.reflection.ClassReflector;

/**
 * <p>
 * Stores beans that have been configured in {@code infinitum.cfg.xml}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 04/23/12
 */
public class BeanContainer {

	private Map<String, Pair<String, Map<String, String>>> mBeanMap = new HashMap<String, Pair<String, Map<String, String>>>();

	/**
	 * Retrieves an instance of the bean with the given name. The name is configured
	 * in {@code infinitum.cfg.xml}.
	 * 
	 * @param name
	 *            the name of the bean to retrieve
	 * @return an instance of the bean.
	 * @throws InfinitumConfigurationException
	 *             if the bean does not exist or could not be constructed
	 */
	public Object loadBean(String name) throws InfinitumConfigurationException {
		Pair<String, Map<String, String>> pair = mBeanMap.get(name);
		if (pair == null)
			throw new InfinitumConfigurationException("Bean '" + name + "' could not be resolved");
		String beanClass = pair.getFirst();
		if (beanClass == null)
			throw new InfinitumConfigurationException("Bean '" + name + "' could not be resolved");
		Class<?> c;
		try {
			c = Class.forName(beanClass);
		} catch (ClassNotFoundException e) {
			throw new InfinitumConfigurationException("Bean '" + name + "' could not be resolved");
		}
		Object bean = null;
		try {
			bean = c.newInstance();
		} catch (IllegalAccessException e) {
			throw new InfinitumConfigurationException("Bean '" + name + "' could not be constructed");
		} catch (InstantiationException e) {
			throw new InfinitumConfigurationException("Bean '" + name + "' could not be constructed");
		}
		Map<String, String> params = pair.getSecond();
		for (Entry<String, String> e : params.entrySet()) {
			try {
				Field f = ClassReflector.getField(c, e.getKey());
				if (f == null)
					continue;
				f.setAccessible(true);
				Class<?> type = Primitives.unwrap(f.getType());
				String argStr = e.getValue();
				Object arg = null;
				if (type == int.class)
					arg = Integer.parseInt(argStr);
				else if (type == double.class)
					arg = Double.parseDouble(argStr);
				else if (type == float.class)
					arg = Float.parseFloat(argStr);
				else if (type == long.class)
					arg = Long.parseLong(argStr);
				else if (type == char.class)
					arg = argStr.charAt(0);
				else if (type == String.class)
					arg = argStr;
				f.set(bean, arg);
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalArgumentException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return bean;
	}

	/**
	 * Checks if a bean with the given name exists.
	 * 
	 * @param name
	 *            the name to check
	 * @return {@code true} if it exists, {@code false} if not
	 */
	public boolean beanExists(String name) {
		return mBeanMap.containsKey(name);
	}

	/**
	 * Registers the bean with the given name and class name with the
	 * {@code BeanContainer}.
	 * 
	 * @param name
	 *            the name of the bean
	 * @param beanClass
	 *            the class name of the bean
	 * @param args
	 *            a {@link Map} of parameter names and their arguments
	 */
	public void registerBean(String name, String beanClass, Map<String, String> args) {
		Pair<String, Map<String, String>> pair = new Pair<String, Map<String, String>>(beanClass, args);
		mBeanMap.put(name, pair);
	}

}
