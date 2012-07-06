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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.clarionmedia.infinitum.context.BeanFactory;
import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;
import com.clarionmedia.infinitum.di.Bean;
import com.clarionmedia.infinitum.di.BeanPostProcessor;
import com.clarionmedia.infinitum.di.BeanUtils;
import com.clarionmedia.infinitum.di.annotation.Autowired;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.internal.AutowiredBeanPostProcessor;
import com.clarionmedia.infinitum.internal.Pair;
import com.clarionmedia.infinitum.internal.Primitives;
import com.clarionmedia.infinitum.reflection.ClassReflector;
import com.clarionmedia.infinitum.reflection.impl.DefaultClassReflector;

/**
 * <p>
 * Implementation of {@link BeanFactory} for storing beans that have been
 * configured in {@code infinitum.cfg.xml}. {@code ConfigurableBeanFactory} also
 * acts as a service locator for {@link InfinitumContext}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0
 * @since 04/23/12
 */
public class ConfigurableBeanFactory implements BeanFactory {

	private ClassReflector mClassReflector;
	private Map<String, Pair<Class<?>, Map<String, Object>>> mBeanDefinitions;
	private Map<String, Object> mBeanMap;

	/**
	 * Constructs a new {@code ConfigurableBeanFactory}.
	 */
	public ConfigurableBeanFactory() {
		mClassReflector = new DefaultClassReflector();
		mBeanDefinitions = new HashMap<String, Pair<Class<?>, Map<String, Object>>>();
		mBeanMap = new HashMap<String, Object>();
	}

	@Override
	public Object loadBean(String name) throws InfinitumConfigurationException {
		if (!mBeanMap.containsKey(name))
			throw new InfinitumConfigurationException("Bean '" + name + "' could not be resolved");
		return mBeanMap.get(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T loadBean(String name, Class<T> clazz) throws InfinitumConfigurationException {
		Object bean = loadBean(name);
		if (!clazz.isInstance(bean))
			throw new InfinitumConfigurationException("Bean '" + name + "' was not of type '" + clazz.getName() + "'.");
		return (T) bean;
	}

	@Override
	public boolean beanExists(String name) {
		return mBeanDefinitions.containsKey(name);
	}

	@Override
	public void registerBeans(List<Bean> beans) {
		for (Bean bean : beans) {
			Map<String, Object> propertiesMap = new HashMap<String, Object>();
			for (Bean.Property property : bean.getProperties()) {
				String name = property.getName();
				String ref = property.getRef();
				if (ref != null) {
					propertiesMap.put(name, loadBean(ref));
				} else {
					String value = property.getValue();
					propertiesMap.put(name, value);
				}
			}
			registerBean(bean.getId(), bean.getClassName(), propertiesMap);
		}
		// Execute bean post processor
		postProcess();
	}

	@Override
	public void registerBean(String name, String beanClass, Map<String, Object> args) {
		// args: Map<fieldName, fieldValue>
		Class<?> clazz;
		try {
			clazz = Class.forName(beanClass);
		} catch (ClassNotFoundException e) {
			throw new InfinitumConfigurationException("Bean '" + name + "' could not be resolved ('" + beanClass + "' not found)");
		}
		Pair<Class<?>, Map<String, Object>> pair = new Pair<Class<?>, Map<String, Object>>(clazz, args);
		// pair: Pair<bean, beanArgsMap>
		mBeanDefinitions.put(name, pair);
		// First we construct an instance of the bean
		Object bean = createBean(name, beanClass);
		// Then we populate its fields
		setFields(bean, args);
		mBeanMap.put(name, bean);
	}

	@Override
	public Map<String, Class<?>> getBeanDefinitions() {
		Map<String, Class<?>> beanDefinitions = new HashMap<String, Class<?>>();
		for (Entry<String, Pair<Class<?>, Map<String, Object>>> entry : mBeanDefinitions.entrySet()) {
			beanDefinitions.put(entry.getKey(), entry.getValue().getFirst());
		}
		return beanDefinitions;
	}

	private void postProcess() {
		BeanPostProcessor beanPostProcessor = new AutowiredBeanPostProcessor(this);
		for (Entry<String, Object> bean : mBeanMap.entrySet()) {
			beanPostProcessor.postProcessBean(bean.getKey(), bean.getValue());
		}
	}

	private Object createBean(String name, String beanClass) {
		if (beanClass == null)
			throw new InfinitumConfigurationException("Bean '" + name + "' could not be resolved");
		Class<?> clazz;
		try {
			// Find the class
			clazz = Class.forName(beanClass);
		} catch (ClassNotFoundException e) {
			throw new InfinitumConfigurationException("Bean '" + name + "' could not be resolved ('" + beanClass
					+ "' not found)");
		}
		// Instantiate it
		return instantiateBean(name, clazz);
	}

	private Object instantiateBean(String name, Class<?> clazz) {
		Constructor<?> target = null;
		for (Constructor<?> ctor : mClassReflector.getAllConstructors(clazz)) {
			if (ctor.isAnnotationPresent(Autowired.class)) {
				if (target != null)
					throw new InfinitumConfigurationException(
							"Only 1 constructor may be autowired (found more than 1 in class '" + clazz.getName()
									+ "').");
				target = ctor;
			}
		}
		if (target == null) {
			try {
				return clazz.newInstance();
			} catch (IllegalAccessException e) {
				throw new InfinitumConfigurationException("Bean '" + name + "' could not be constructed");
			} catch (InstantiationException e) {
				throw new InfinitumConfigurationException("Bean '" + name + "' could not be constructed");
			}
		}
		if (target.getParameterTypes().length > 1)
			throw new InfinitumConfigurationException("Autowired constructor in bean '" + name
					+ "' may only have 1 argument.");
		try {
			target.setAccessible(true);
			if (target.getParameterTypes().length == 1) {
				Class<?> paramType = target.getParameterTypes()[0];
				Object argument = BeanUtils.findCandidateBean(this, paramType);
				if (argument == null)
					throw new InfinitumConfigurationException("Could not autowire property of type '" + clazz.getName() + "' in bean '" + name + "' (no autowire candidates found)");
				return target.newInstance(argument);
			} else {
				return target.newInstance();
			}
		} catch (IllegalArgumentException e) {
			throw new InfinitumConfigurationException("Unable to instantiate bean '" + name + "'.");
		} catch (InstantiationException e) {
			throw new InfinitumConfigurationException("Unable to instantiate bean '" + name + "'.");
		} catch (IllegalAccessException e) {
			throw new InfinitumConfigurationException("Unable to instantiate bean '" + name + "'.");
		} catch (InvocationTargetException e) {
			throw new InfinitumConfigurationException("Unable to instantiate bean '" + name + "'.");
		}
	}

	private void setFields(Object bean, Map<String, Object> params) {
		for (Entry<String, Object> e : params.entrySet()) {
			try {
				// Find the field
				Field f = mClassReflector.getField(bean.getClass(), e.getKey());
				if (f == null)
					continue;
				f.setAccessible(true);
				Class<?> type = Primitives.unwrap(f.getType());
				Object val = e.getValue();
				String argStr = null;
				if (val.getClass() == String.class)
					argStr = (String) val;
				Object arg = null;
				// Parse the string value into the proper type
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
				// Populate the field's value
				f.set(bean, arg);
			} catch (SecurityException e1) {
				throw new InfinitumRuntimeException("Could not set field in object of type '"
						+ bean.getClass().getName() + "'");
			} catch (IllegalArgumentException e1) {
				throw new InfinitumRuntimeException("Could not set field in object of type '"
						+ bean.getClass().getName() + "'");
			} catch (IllegalAccessException e1) {
				throw new InfinitumRuntimeException("Could not set field in object of type '"
						+ bean.getClass().getName() + "'");
			}
		}
	}

}
