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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import android.content.Context;
import com.clarionmedia.infinitum.aop.Advice;
import com.clarionmedia.infinitum.aop.AdvisedProxyFactory;
import com.clarionmedia.infinitum.aop.AopProxy;
import com.clarionmedia.infinitum.aop.AspectWeaver;
import com.clarionmedia.infinitum.aop.JoinPoint;
import com.clarionmedia.infinitum.aop.Pointcut;
import com.clarionmedia.infinitum.aop.annotation.After;
import com.clarionmedia.infinitum.aop.annotation.Around;
import com.clarionmedia.infinitum.aop.annotation.Aspect;
import com.clarionmedia.infinitum.aop.annotation.Before;
import com.clarionmedia.infinitum.di.BeanFactory;
import com.clarionmedia.infinitum.reflection.ClassReflector;
import com.clarionmedia.infinitum.reflection.impl.DefaultClassReflector;

/**
 * <p>
 * Implementation of {@link AspectWeaver} for processing aspects annotated with
 * {@link Aspect}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 07/12/12
 * @since 1.0
 */
public class AnnotationsAspectWeaver implements AspectWeaver {

	private ClassReflector mClassReflector;
	private BeanFactory mBeanFactory;
	private AdvisedProxyFactory mProxyFactory;

	/**
	 * Creates a new {@code AnnotationsAspectWeaver} with the given
	 * {@link BeanFactory}.
	 * 
	 * @param beanFactory
	 *            the {@code BeanFactory} to retrieve beans from
	 */
	public AnnotationsAspectWeaver(BeanFactory beanFactory) {
		mClassReflector = new DefaultClassReflector();
		mProxyFactory = new DelegatingAdvisedProxyFactory();
		mBeanFactory = beanFactory;
	}

	@Override
	public void weave(Context context, Set<Class<?>> aspects) {
		for (Pointcut pointcut : getPointcuts(aspects)) {
			String beanName = pointcut.getBeanName();
			Object bean = mBeanFactory.loadBean(beanName);
			AopProxy proxy = mProxyFactory.createProxy(context, bean, pointcut);
			mBeanFactory.getBeanMap().put(beanName, proxy.getProxy());
		}
	}

	private Collection<Pointcut> getPointcuts(Set<Class<?>> aspects) {
		Map<String, Pointcut> pointcutMap = new HashMap<String, Pointcut>();
		for (Class<?> aspect : aspects) {
			if (!aspect.isAnnotationPresent(Aspect.class))
				continue;
			Object advisor = mClassReflector.getClassInstance(aspect);
			// Process @Before advice
			processAdvice(advisor, Before.class, pointcutMap);
			// Process @After advice
			processAdvice(advisor, After.class, pointcutMap);
			// Process @Around advice
			processAdvice(advisor, Around.class, pointcutMap);
		}
		return pointcutMap.values();
	}

	private void processAdvice(Object advisor,
			Class<? extends Annotation> adviceType,
			Map<String, Pointcut> pointcutMap) {
		List<Method> methods = mClassReflector.getAllMethodsAnnotatedWith(
				advisor.getClass(), adviceType);
		for (Method adviceMethod : methods) {
			Advice advice = new Advice(adviceMethod.getAnnotation(adviceType));
			for (String bean : advice.beans()) {
				bean = bean.trim();
				if (bean.length() == 0)
					continue;
				String beanName = bean;
				boolean isClassScope = false;
				if (bean.contains(".")) {
					beanName = bean.substring(0, bean.indexOf('.'));
				} else {
					isClassScope = true;
				}
				JoinPoint joinPoint = advice.isAround()
						? new BasicProceedingJoinPoint(advisor, adviceMethod)
						: new BasicJoinPoint(advisor, adviceMethod,
								advice.getLocation());
				joinPoint.setBeanName(beanName);
				joinPoint.setTarget(mBeanFactory.loadBean(beanName));
				joinPoint.setOrder(advice.order());
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
