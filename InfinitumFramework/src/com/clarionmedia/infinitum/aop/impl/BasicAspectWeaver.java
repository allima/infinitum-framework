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

package com.clarionmedia.infinitum.aop.impl;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;

import com.clarionmedia.infinitum.aop.AspectWeaver;
import com.clarionmedia.infinitum.aop.JoinPoint;
import com.clarionmedia.infinitum.aop.JoinPoint.Location;
import com.clarionmedia.infinitum.aop.Pointcut;
import com.clarionmedia.infinitum.aop.annotation.After;
import com.clarionmedia.infinitum.aop.annotation.Around;
import com.clarionmedia.infinitum.aop.annotation.Aspect;
import com.clarionmedia.infinitum.aop.annotation.Before;
import com.clarionmedia.infinitum.di.BeanFactory;
import com.clarionmedia.infinitum.internal.DexCaching;
import com.clarionmedia.infinitum.reflection.ClassReflector;
import com.clarionmedia.infinitum.reflection.impl.DefaultClassReflector;
import com.google.dexmaker.stock.ProxyBuilder;

/**
 * <p>
 * Basic implementation of {@link AspectWeaver}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 07/12/12
 * @since 1.0
 */
public class BasicAspectWeaver implements AspectWeaver {

	private ClassReflector mClassReflector;
	private BeanFactory mBeanFactory;

	/**
	 * Creates a new {@code BasicAspectWeaver} with the given
	 * {@link BeanFactory}.
	 * 
	 * @param beanFactory
	 *            the {@code BeanFactory} to retrieve beans from
	 */
	public BasicAspectWeaver(BeanFactory beanFactory) {
		mClassReflector = new DefaultClassReflector();
		mBeanFactory = beanFactory;
	}

	@Override
	public void weave(Context context, Set<Class<?>> aspects) {
		for (Pointcut pointcut : getPointcuts(aspects)) {
			String beanName = pointcut.getBeanName();
			Object bean = mBeanFactory.loadBean(beanName);
			try {
				bean = ProxyBuilder.forClass(bean.getClass())
						.handler(new AdvisedObject(pointcut))
						.dexCache(DexCaching.getDexCache(context)).build();
				mBeanFactory.getBeanMap().put(beanName, bean);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private Collection<Pointcut> getPointcuts(Set<Class<?>> aspects) {
		Map<String, Pointcut> pointcutMap = new HashMap<String, Pointcut>();
		for (Class<?> aspect : aspects) {
			if (!aspect.isAnnotationPresent(Aspect.class))
				continue;
			// Process @Before advice
			List<Method> methods = mClassReflector.getAllMethodsAnnotatedWith(
					aspect, Before.class);
			for (Method method : methods) {
				Before before = method.getAnnotation(Before.class);
				for (String bean : before.beans()) {
					bean = bean.trim();
					if (bean.length() == 0)
						continue;
					String beanName = bean;
					boolean isClassScope = false;
					if (bean.contains("#")) {
						beanName = bean.substring(0, bean.indexOf('#'));
					} else {
						isClassScope = true;
					}
					JoinPoint joinPoint = new BasicJoinPoint(Location.Before);
					joinPoint.setBeanName(beanName);
					joinPoint.setTarget(mBeanFactory.loadBean(beanName));
					if (isClassScope) {
						joinPoint.setClassScope(true);
					} else {
						// TODO Add support for specific method join points
					}
					putJoinPoint(pointcutMap, joinPoint);
				}
				// TODO Add within support
			}
			// Process @After advice
			methods = mClassReflector.getAllMethodsAnnotatedWith(aspect,
					After.class);
			for (Method method : methods) {
				After after = method.getAnnotation(After.class);
				for (String bean : after.beans()) {
					bean = bean.trim();
					if (bean.length() == 0)
						continue;
					String beanName = bean;
					boolean isClassScope = false;
					if (bean.contains("#")) {
						beanName = bean.substring(0, bean.indexOf('#'));
					} else {
						isClassScope = true;
					}
					JoinPoint joinPoint = new BasicJoinPoint(Location.After);
					joinPoint.setBeanName(beanName);
					joinPoint.setTarget(mBeanFactory.loadBean(beanName));
					if (isClassScope) {
						joinPoint.setClassScope(true);
					} else {
						// TODO Add support for specific method join points
					}
					putJoinPoint(pointcutMap, joinPoint);
				}
				// TODO Add within support
			}
			// Process @Around advice
			methods = mClassReflector.getAllMethodsAnnotatedWith(aspect,
					Around.class);
			for (Method method : methods) {
				Around around = method.getAnnotation(Around.class);
				for (String bean : around.beans()) {
					bean = bean.trim();
					if (bean.length() == 0)
						continue;
					String beanName = bean;
					boolean isClassScope = false;
					if (bean.contains("#")) {
						beanName = bean.substring(0, bean.indexOf('#'));
					} else {
						isClassScope = true;
					}
					JoinPoint joinPoint = new BasicJoinPoint(Location.Around);
					joinPoint.setBeanName(beanName);
					joinPoint.setTarget(mBeanFactory.loadBean(beanName));
					if (isClassScope) {
						joinPoint.setClassScope(true);
					} else {
						// TODO Add support for specific method join points
					}
					putJoinPoint(pointcutMap, joinPoint);
				}
				// TODO Add within support
			}
		}
		return pointcutMap.values();
	}

	private void putJoinPoint(Map<String, Pointcut> pointcutMap,
			JoinPoint joinPoint) {
		if (pointcutMap.containsKey(joinPoint.getBeanName())) {
			pointcutMap.get(joinPoint.getBeanName()).addJoinPoint(joinPoint);
		} else {
			Pointcut pointcut = new Pointcut(joinPoint.getBeanName(),
					joinPoint.getTargetType());
			pointcut.addJoinPoint(joinPoint);
			pointcutMap.put(joinPoint.getBeanName(), pointcut);
		}
	}

}
