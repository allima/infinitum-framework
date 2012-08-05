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

package com.clarionmedia.infinitum.di;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.clarionmedia.infinitum.aop.AopProxy;
import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;
import com.clarionmedia.infinitum.di.annotation.Autowired;
import com.clarionmedia.infinitum.di.annotation.PostConstruct;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.internal.Preconditions;
import com.clarionmedia.infinitum.internal.Primitives;
import com.clarionmedia.infinitum.reflection.ClassReflector;
import com.clarionmedia.infinitum.reflection.impl.DefaultClassReflector;
import com.clarionmedia.infinitum.di.impl.SingletonBeanDefinition;
import com.clarionmedia.infinitum.di.impl.PrototypeBeanDefinition;

/**
 * <p>
 * Describes a bean instance, including its name, type, property values, and
 * constructor arguments.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 08/04/12
 * @since 1.0
 * @see SingletonBeanDefinition
 * @see PrototypeBeanDefinition
 */
public abstract class AbstractBeanDefinition {

	protected String mName;
	protected Class<?> mType;
	protected Map<String, Object> mProperties;
	protected Map<Field, Object> mFieldInjections;
	protected Map<Method, Object> mSetterInjections;
	protected ClassReflector mClassReflector;
	protected BeanFactory mBeanFactory;
	protected AopProxy mBeanProxy;

	/**
	 * Creates a new {@code AbstractBeanDefinition}.
	 * 
	 * @param beanFactory
	 *            the {@link BeanFactory} this bean definition is scoped to
	 */
	public AbstractBeanDefinition(BeanFactory beanFactory) {
		mClassReflector = new DefaultClassReflector();
		mBeanFactory = beanFactory;
		mFieldInjections = new HashMap<Field, Object>();
		mSetterInjections = new HashMap<Method, Object>();
	}

	/**
	 * Returns an instance of the bean, which could be a proxy for it.
	 * 
	 * @return bean or bean proxy
	 */
	public abstract Object getBeanInstance();

	/**
	 * Returns an instance of the bean, guaranteeing that it is not a proxy for
	 * the bean.
	 * 
	 * @return bean
	 */
	public abstract Object getNonProxiedBeanInstance();

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public Class<?> getType() {
		return mType;
	}

	public void setType(Class<?> type) {
		mType = type;
	}

	public Map<String, Object> getProperties() {
		return mProperties;
	}

	public void setProperties(Map<String, Object> properties) {
		mProperties = properties;
	}

	public AopProxy getBeanProxy() {
		return mBeanProxy;
	}

	public void setBeanProxy(AopProxy beanProxy) {
		mBeanProxy = beanProxy;
	}

	public Map<Field, Object> getFieldInjections() {
		return mFieldInjections;
	}

	public void setFieldInjections(Map<Field, Object> injections) {
		mFieldInjections = injections;
	}

	public void addFieldInjection(Field field, Object value) {
		mFieldInjections.put(field, value);
	}

	public Map<Method, Object> getSetterInjections() {
		return mSetterInjections;
	}

	public void setSetterInjections(Map<Method, Object> setterInjections) {
		mSetterInjections = setterInjections;
	}

	public void addSetterInjection(Method setter, Object value) {
		mSetterInjections.put(setter, value);
	}

	protected Object createBean() {
		Object bean;
		Constructor<?> target = null;
		for (Constructor<?> ctor : mClassReflector.getAllConstructors(mType)) {
			if (ctor.isAnnotationPresent(Autowired.class)) {
				if (target != null)
					throw new InfinitumConfigurationException(
							"Only 1 constructor may be autowired (found more than 1 in class '"
									+ mType.getName() + "').");
				target = ctor;
			}
		}
		if (target == null) {
			bean = mClassReflector.getClassInstance(mType);
		} else {
			if (target.getParameterTypes().length > 1)
				throw new InfinitumConfigurationException(
						"Autowired constructor in bean '" + mName
								+ "' may only have 1 argument.");
			try {
				target.setAccessible(true);
				if (target.getParameterTypes().length == 1) {
					Class<?> paramType = target.getParameterTypes()[0];
					Object argument = BeanUtils.findCandidateBean(mBeanFactory,
							paramType);
					if (argument == null)
						throw new InfinitumConfigurationException(
								"Could not autowire property of type '"
										+ mType.getName() + "' in bean '"
										+ mName
										+ "' (no autowire candidates found)");
					bean = target.newInstance(argument);
				} else {
					bean = target.newInstance();
				}
			} catch (IllegalArgumentException e) {
				throw new InfinitumConfigurationException(
						"Unable to instantiate bean '" + mName + "'.");
			} catch (InstantiationException e) {
				throw new InfinitumConfigurationException(
						"Unable to instantiate bean '" + mName + "'.");
			} catch (IllegalAccessException e) {
				throw new InfinitumConfigurationException(
						"Unable to instantiate bean '" + mName + "'.");
			} catch (InvocationTargetException e) {
				throw new InfinitumConfigurationException(
						"Unable to instantiate bean '" + mName + "'.");
			}
		}
		return bean;
	}

	protected void inject(Object bean) {
		for (Entry<Field, Object> injection : mFieldInjections.entrySet()) {
			mClassReflector.setFieldValue(bean, injection.getKey(),
					injection.getValue());
		}
		for (Entry<Method, Object> injection : mSetterInjections.entrySet()) {
			mClassReflector.invokeMethod(bean, injection.getKey(),
					injection.getValue());
		}
	}

	protected void postConstruct(Object bean) {
		List<Method> postConstructs = mClassReflector
				.getAllMethodsAnnotatedWith(mType, PostConstruct.class);
		if (postConstructs.size() > 1)
			throw new InfinitumRuntimeException(
					"Only 1 method may be annotated with PostConstruct (found "
							+ postConstructs.size() + " in '" + mType.getName()
							+ "')");
		if (postConstructs.size() == 1)
			mClassReflector.invokeMethod(bean, postConstructs.get(0));
	}

	protected void setFields(Object bean) {
		Preconditions.checkNotNull(bean);
		if (mProperties == null || mProperties.size() == 0)
			return;
		for (Entry<String, Object> e : mProperties.entrySet()) {
			// Find the field
			Field field = mClassReflector.getField(bean.getClass(), e.getKey());
			if (field == null)
				continue;
			Class<?> type = Primitives.unwrap(field.getType());
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
			mClassReflector.setFieldValue(bean, field, arg);
		}
	}

}
