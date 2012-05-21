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

package com.clarionmedia.infinitum.context.impl;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.clarionmedia.infinitum.context.BeanService;
import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;
import com.clarionmedia.infinitum.internal.Pair;
import com.clarionmedia.infinitum.internal.Primitives;
import com.clarionmedia.infinitum.logging.Logger;
import com.clarionmedia.infinitum.reflection.ClassReflector;
import com.clarionmedia.infinitum.reflection.impl.DefaultClassReflector;

/**
 * <p>
 * Implementation of {@link BeanService} for storing beans that have been
 * configured in {@code infinitum.cfg.xml}. {@code BeanFactory} also acts as a
 * service locator for {@link ApplicationContext}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 04/23/12
 */
public class BeanFactory implements BeanService {
	
	private ClassReflector mClassReflector;
	private Map<String, Pair<String, Map<String, Object>>> mBeanMap;
	private Logger mLogger;

	public BeanFactory() {
		mClassReflector = new DefaultClassReflector();
		mBeanMap = new HashMap<String, Pair<String, Map<String, Object>>>();
		mLogger = Logger.getInstance(getClass().getSimpleName());
	}

	@Override
	public Object loadBean(String name) throws InfinitumConfigurationException {
		Pair<String, Map<String, Object>> pair = mBeanMap.get(name);
		if (pair == null)
			throw new InfinitumConfigurationException("Bean '" + name
					+ "' could not be resolved");
		String beanClass = pair.getFirst();
		if (beanClass == null)
			throw new InfinitumConfigurationException("Bean '" + name
					+ "' could not be resolved");
		Class<?> c;
		try {
			c = Class.forName(beanClass);
		} catch (ClassNotFoundException e) {
			throw new InfinitumConfigurationException("Bean '" + name
					+ "' could not be resolved");
		}
		Object bean = null;
		try {
			bean = c.newInstance();
		} catch (IllegalAccessException e) {
			throw new InfinitumConfigurationException("Bean '" + name
					+ "' could not be constructed");
		} catch (InstantiationException e) {
			throw new InfinitumConfigurationException("Bean '" + name
					+ "' could not be constructed");
		}
		Map<String, Object> params = pair.getSecond();
		for (Entry<String, Object> e : params.entrySet()) {
			try {
				Field f = mClassReflector.getField(c, e.getKey());
				if (f == null)
					continue;
				f.setAccessible(true);
				Class<?> type = Primitives.unwrap(f.getType());
				Object val = e.getValue();
				String argStr = null;
				if (val.getClass() == String.class)
					argStr = (String) val;
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
				else
					arg = val;
				f.set(bean, arg);
			} catch (SecurityException e1) {
				mLogger.error("Could not set field", e1);
			} catch (IllegalArgumentException e1) {
				mLogger.error("Could not set field", e1);
			} catch (IllegalAccessException e1) {
				mLogger.error("Could not set field", e1);
			}
		}
		return bean;
	}

	@Override
	public boolean beanExists(String name) {
		return mBeanMap.containsKey(name);
	}

	@Override
	public void registerBean(String name, String beanClass,
			Map<String, Object> args) {
		// args: Map<argName, argValue>
		Pair<String, Map<String, Object>> pair = new Pair<String, Map<String, Object>>(
				beanClass, args);
		// pair: Pair<bean, beanArgsMap>
		mBeanMap.put(name, pair);
	}

}
