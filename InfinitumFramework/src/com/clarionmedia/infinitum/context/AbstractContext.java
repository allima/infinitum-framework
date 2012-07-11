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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.clarionmedia.infinitum.di.BeanComponent;
import com.clarionmedia.infinitum.di.BeanFactory;
import com.clarionmedia.infinitum.di.BeanFactoryPostProcessor;
import com.clarionmedia.infinitum.di.BeanPostProcessor;
import com.clarionmedia.infinitum.di.annotation.Bean;
import com.clarionmedia.infinitum.di.annotation.Component;
import com.clarionmedia.infinitum.di.impl.ConfigurableBeanFactory;
import com.clarionmedia.infinitum.internal.StringUtil;
import com.clarionmedia.infinitum.reflection.PackageReflector;
import com.clarionmedia.infinitum.reflection.impl.DefaultPackageReflector;

/**
 * <p>
 * Abstract implementation of {@link InfinitumContext}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 07/11/12
 * @since 1.0
 */
public abstract class AbstractContext implements InfinitumContext {

	protected BeanFactory mBeanFactory;

	/**
	 * Returns a {@link List} of {@link BeanComponent} instances that were
	 * registered with the context through the Infinitum XML configuration.
	 * 
	 * @return {@code List} of {@code BeanComponents}
	 */
	protected abstract List<BeanComponent> getBeans();

	/**
	 * Returns the {@link RestfulContext} that was registered with the context
	 * through the Infinitum XML configuration.
	 * 
	 * @return {@code RestfulContext} or {@code null} if none was registered
	 */
	protected abstract RestfulContext getRestContext();

	/**
	 * Returns a {@link Set} of all {@link Class} instances containing the
	 * {@link Component} annotation from the classpath.
	 * 
	 * @return {@code Set} of {@code Classes}
	 */
	protected Set<Class<?>> getClasspathComponents() {
		PackageReflector reflector = new DefaultPackageReflector();
		return reflector.getClassesWithAnnotation(Component.class);
	}

	/**
	 * Must be executed after the {@link InfinitumContext} has been constructed.
	 */
	protected void postProcess() {
		getRestContext().setParentContext(this);
		mBeanFactory = new ConfigurableBeanFactory();
		mBeanFactory.registerBeans(getBeans());

		// Scan for Components
		Set<Class<?>> components = getClasspathComponents();

		// Categorize the Components while filtering down the original Set
		Set<Class<BeanPostProcessor>> beanPostProcessors = getAndRemoveBeanPostProcessors(components);
		Set<Class<BeanFactoryPostProcessor>> beanFactoryPostProcessors = getAndRemoveBeanFactoryPostProcessors(components);

		// Register scanned Bean candidates
		for (Class<?> candidate : components) {
			if (candidate.isAnnotationPresent(Bean.class)) {
				Bean bean = candidate.getAnnotation(Bean.class);
				String beanName = bean.value().trim().equals("") ? StringUtil
						.toCamelCase(candidate.getSimpleName()) : bean.value()
						.trim();
				mBeanFactory.registerBean(beanName, candidate.getName(), null);
			}
		}

		// Execute post processors
		executeBeanPostProcessors(beanPostProcessors);
		executeBeanFactoryPostProcessors(beanFactoryPostProcessors);
	}

	@SuppressWarnings("unchecked")
	private Set<Class<BeanFactoryPostProcessor>> getAndRemoveBeanFactoryPostProcessors(
			Collection<Class<?>> components) {
		Set<Class<BeanFactoryPostProcessor>> postProcessors = new HashSet<Class<BeanFactoryPostProcessor>>();
		Iterator<Class<?>> iter = components.iterator();
		while (iter.hasNext()) {
			Class<?> component = iter.next();
			if (BeanFactoryPostProcessor.class.isAssignableFrom(component)) {
				postProcessors.add((Class<BeanFactoryPostProcessor>) component);
				iter.remove();
			}
		}
		return postProcessors;
	}

	@SuppressWarnings("unchecked")
	private Set<Class<BeanPostProcessor>> getAndRemoveBeanPostProcessors(
			Collection<Class<?>> components) {
		Set<Class<BeanPostProcessor>> postProcessors = new HashSet<Class<BeanPostProcessor>>();
		Iterator<Class<?>> iter = components.iterator();
		while (iter.hasNext()) {
			Class<?> component = iter.next();
			if (BeanPostProcessor.class.isAssignableFrom(component)) {
				postProcessors.add((Class<BeanPostProcessor>) component);
				iter.remove();
			}
		}
		return postProcessors;
	}

	private void executeBeanPostProcessors(
			Collection<Class<BeanPostProcessor>> postProcessors) {
		for (Class<BeanPostProcessor> postProcessor : postProcessors) {
			try {
				BeanPostProcessor postProcessorInstance = postProcessor
						.newInstance();
				for (Entry<String, Object> bean : mBeanFactory.getBeanMap()
						.entrySet()) {
					postProcessorInstance.postProcessBean(bean.getKey(),
							bean.getValue());
				}
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void executeBeanFactoryPostProcessors(
			Collection<Class<BeanFactoryPostProcessor>> postProcessors) {
		for (Class<BeanFactoryPostProcessor> postProcessor : postProcessors) {
			try {
				BeanFactoryPostProcessor postProcessorInstance = postProcessor
						.newInstance();
				postProcessorInstance.postProcessBeanFactory(mBeanFactory);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
