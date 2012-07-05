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

package com.clarionmedia.infinitum.internal;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.clarionmedia.infinitum.context.BeanFactory;
import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;
import com.clarionmedia.infinitum.di.BeanPostProcessor;
import com.clarionmedia.infinitum.di.annotation.Autowired;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.reflection.ClassReflector;
import com.clarionmedia.infinitum.reflection.impl.DefaultClassReflector;

/**
 * <p>
 * Implementation of {@link BeanPostProcessor} used to inject autowired bean
 * dependencies.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0
 * @since 07/05/12
 */
public class AutowiredBeanPostProcessor implements BeanPostProcessor {

	private ClassReflector mClassReflector;
	private BeanFactory mBeanFactory;

	/**
	 * Constructs a new {@code AutowiredBeanPostProcessor}.
	 */
	public AutowiredBeanPostProcessor(BeanFactory beanFactory) {
		mClassReflector = new DefaultClassReflector();
		mBeanFactory = beanFactory;
	}

	@Override
	public void postProcessBean(String beanName, Object bean) {
		injectFields(bean);
		injectSetters(bean);
	}

	private void injectFields(Object bean) {
		for (Field field : mClassReflector.getAllFields(bean.getClass())) {
			if (!field.isAnnotationPresent(Autowired.class))
				continue;
			Autowired autowired = field.getAnnotation(Autowired.class);
			injectBeanCandidate(bean, field, autowired.value());
		}
	}

	private void injectSetters(Object bean) {
		for (Method method : mClassReflector.getAllMethods(bean.getClass())) {
			if (!method.isAnnotationPresent(Autowired.class))
				continue;
			Autowired autowired = method.getAnnotation(Autowired.class);
			injectBeanCandidate(bean, method, autowired.value());
		}
	}

	private void injectBeanCandidate(Object bean, Field field, String candidate) {
		Object value;
		if (candidate != null) {
			value = mBeanFactory.loadBean(candidate);
		} else {
			value = findCandidateBean(field.getType());
		}
		try {
			field.set(bean, value);
		} catch (IllegalArgumentException e) {
			throw new InfinitumRuntimeException(
					"Could not set field in object of type '"
							+ bean.getClass().getName() + "'");
		} catch (IllegalAccessException e) {
			throw new InfinitumRuntimeException(
					"Could not set field in object of type '"
							+ bean.getClass().getName() + "'");
		}
	}

	private void injectBeanCandidate(Object bean, Method setter,
			String candidate) {
		Object value;
		if (candidate != null) {
			value = mBeanFactory.loadBean(candidate);
		} else {
			if (setter.getParameterTypes().length != 1)
				throw new InfinitumConfigurationException(
						"Autowired setter method must contain 1 parameter.");
			value = findCandidateBean(setter.getParameterTypes()[0]);
		}
		try {
			setter.invoke(bean, value);
		} catch (IllegalArgumentException e) {
			throw new InfinitumRuntimeException(
					"Could not invoke setter in object of type '"
							+ bean.getClass().getName() + "'");
		} catch (IllegalAccessException e) {
			throw new InfinitumRuntimeException(
					"Could not invoke setter in object of type '"
							+ bean.getClass().getName() + "'");
		} catch (InvocationTargetException e) {
			throw new InfinitumRuntimeException(
					"Could not invoke setter in object of type '"
							+ bean.getClass().getName() + "'");
		}
	}

	private Object findCandidateBean(Class<?> clazz) {
		String candidate = invert(mBeanFactory.getBeanDefinitions()).get(clazz);
		return mBeanFactory.loadBean(candidate);
	}

	private <V, K> Map<V, K> invert(Map<K, V> map) {
		Map<V, K> inv = new HashMap<V, K>();
		for (Entry<K, V> entry : map.entrySet()) {
			if (inv.containsKey(entry.getValue()))
				throw new InfinitumConfigurationException(
						"More than 1 bean candidate found of type '"
								+ entry.getValue() + "'.");
			inv.put(entry.getValue(), entry.getKey());
		}
		return inv;
	}
}
