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

import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;
import com.clarionmedia.infinitum.di.BeanFactory;
import com.clarionmedia.infinitum.di.BeanPostProcessor;
import com.clarionmedia.infinitum.di.BeanUtils;
import com.clarionmedia.infinitum.di.annotation.Autowired;
import com.clarionmedia.infinitum.di.annotation.Component;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.reflection.ClassReflector;
import com.clarionmedia.infinitum.reflection.impl.DefaultClassReflector;

/**
 * <p>
 * Implementation of {@link BeanPostProcessor} used to inject autowired bean
 * dependencies after beans have been initialized.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 07/05/12
 * @since 1.0
 */
@Component
public class AutowiredBeanPostProcessor implements BeanPostProcessor {

	private ClassReflector mClassReflector;

	/**
	 * Constructs a new {@code AutowiredBeanPostProcessor}.
	 */
	public AutowiredBeanPostProcessor() {
		mClassReflector = new DefaultClassReflector();
	}

	@Override
	public void postProcessBean(BeanFactory beanFactory, String beanName, Object bean) {
		injectFields(beanFactory, bean);
		injectSetters(beanFactory, bean);
	}

	private void injectFields(BeanFactory beanFactory, Object bean) {
		for (Field field : mClassReflector.getAllFields(bean.getClass())) {
			if (!field.isAnnotationPresent(Autowired.class))
				continue;
			Autowired autowired = field.getAnnotation(Autowired.class);
			injectBeanCandidate(beanFactory, bean, field, autowired.value());
		}
	}

	private void injectSetters(BeanFactory beanFactory, Object bean) {
		for (Method method : mClassReflector.getAllMethods(bean.getClass())) {
			if (!method.isAnnotationPresent(Autowired.class))
				continue;
			Autowired autowired = method.getAnnotation(Autowired.class);
			injectBeanCandidate(beanFactory, bean, method, autowired.value());
		}
	}

	private void injectBeanCandidate(BeanFactory beanFactory, Object bean, Field field, String candidate) {
		Object value;
		field.setAccessible(true);
		if (candidate != null && candidate.trim().length() > 0) {
			value = beanFactory.loadBean(candidate);
		} else {
			value = BeanUtils.findCandidateBean(beanFactory, field.getType());
		}
		try {
			field.set(bean, value);
		} catch (IllegalArgumentException e) {
			throw new InfinitumRuntimeException("Could not set field in object of type '" + bean.getClass().getName()
					+ "'");
		} catch (IllegalAccessException e) {
			throw new InfinitumRuntimeException("Could not set field in object of type '" + bean.getClass().getName()
					+ "'");
		}
	}

	private void injectBeanCandidate(BeanFactory beanFactory, Object bean, Method setter, String candidate) {
		Object value;
		if (candidate != null && candidate.trim().length() > 0) {
			value = beanFactory.loadBean(candidate);
		} else {
			if (setter.getParameterTypes().length != 1)
				throw new InfinitumConfigurationException("Autowired setter method must contain 1 parameter.");
			value = BeanUtils.findCandidateBean(beanFactory, setter.getParameterTypes()[0]);
		}
		try {
			setter.invoke(bean, value);
		} catch (IllegalArgumentException e) {
			throw new InfinitumRuntimeException("Could not invoke setter in object of type '"
					+ bean.getClass().getName() + "'");
		} catch (IllegalAccessException e) {
			throw new InfinitumRuntimeException("Could not invoke setter in object of type '"
					+ bean.getClass().getName() + "'");
		} catch (InvocationTargetException e) {
			throw new InfinitumRuntimeException("Could not invoke setter in object of type '"
					+ bean.getClass().getName() + "'");
		}
	}

}
