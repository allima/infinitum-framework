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

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import android.content.Context;

import com.clarionmedia.infinitum.aop.AdvisedProxyFactory;
import com.clarionmedia.infinitum.aop.AopProxy;
import com.clarionmedia.infinitum.aop.JoinPoint;
import com.clarionmedia.infinitum.aop.Pointcut;
import com.clarionmedia.infinitum.aop.annotation.Aspect;
import com.clarionmedia.infinitum.di.BeanFactory;
import com.clarionmedia.infinitum.reflection.ClassReflector;
import com.clarionmedia.infinitum.reflection.PackageReflector;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class AnnotationsAspectWeaverTest {
	
	private static final String BEAN_NAME = "someBean";

	@InjectMocks
	private AnnotationsAspectWeaver aspectWeaver = new AnnotationsAspectWeaver();
	
	@Mock
	private BeanFactory mockBeanFactory;
	
	@Mock
	private ClassReflector mockClassReflector;
	
	@Mock
	private PackageReflector mockPackageReflector;
	
	@Mock
	private AdvisedProxyFactory mockProxyFactory;
	
	@Mock
	private AopProxy mockProxy;
	
	private Map<String, Object> mockBeanMap;
	
	private List<String> mockBean;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mockBeanMap = new HashMap<String, Object>();
		mockBean = new ArrayList<String>();
		mockBean.add("hello");
		mockBeanMap.put(BEAN_NAME, mockBean);
	}
	
	@Test
	public void testWeave_noAdvice() {
		// Setup
		Set<Class<?>> aspects = new HashSet<Class<?>>();
		
		// Run
		aspectWeaver.weave(Robolectric.application, aspects);
		
		// Verify
		verify(mockBeanFactory, times(0)).loadBean(BEAN_NAME);
		verify(mockProxyFactory, times(0)).createProxy(any(Context.class), any(Object.class), any(Pointcut.class));
		verify(mockBeanFactory, times(0)).getBeanMap();
	}

	@Test
	public void testWeave_within() throws SecurityException, NoSuchMethodException {
		// Setup
		Set<Class<?>> aspects = new HashSet<Class<?>>();
		aspects.add(MockAspect.class);
		Method advice = MockAspect.class.getMethod("beforeAdvice_within",
				JoinPoint.class);
		List<Method> adviceMethods = new ArrayList<Method>();
		adviceMethods.add(advice);
		when(mockClassReflector.getClassInstance(MockAspect.class))
				.thenReturn(new MockAspect());
		when(mockClassReflector.getAllMethodsAnnotatedWith(MockAspect.class,
				com.clarionmedia.infinitum.aop.annotation.Before.class))
				.thenReturn(adviceMethods);
		when(mockBeanFactory.getBeanMap()).thenReturn(mockBeanMap);
		when(mockProxyFactory.createProxy(any(Context.class), any(Object.class), any(Pointcut.class))).thenReturn(mockProxy);
		when(mockProxy.getProxy()).thenReturn(mockBean);
		
		// Run
		aspectWeaver.weave(Robolectric.application, aspects);
		
		// Verify
		verify(mockBeanFactory).loadBean(BEAN_NAME);
		verify(mockProxyFactory).createProxy(any(Context.class), any(Object.class), any(Pointcut.class));
		verify(mockBeanFactory, times(2)).getBeanMap();
		assertTrue("Bean Map should have 1 bean entry", mockBeanMap.entrySet().size() == 1);
	}
	
	@Test
	public void testWeave_beans() throws SecurityException, NoSuchMethodException {
		// Setup
		Set<Class<?>> aspects = new HashSet<Class<?>>();
		aspects.add(MockAspect.class);
		Method advice = MockAspect.class.getMethod("beforeAdvice_beans", JoinPoint.class);
		Method toString = Object.class.getMethod("toString");
		List<Method> adviceMethods = new ArrayList<Method>();
		adviceMethods.add(advice);
		when(mockClassReflector.getClassInstance(MockAspect.class))
				.thenReturn(new MockAspect());
		when(mockClassReflector.getAllMethodsAnnotatedWith(MockAspect.class,
				com.clarionmedia.infinitum.aop.annotation.Before.class))
				.thenReturn(adviceMethods);
		when(mockBeanFactory.loadBean(BEAN_NAME)).thenReturn(mockBean);
		when(mockClassReflector.getMethod(Object.class, "toString")).thenReturn(toString);
		when(mockProxyFactory.createProxy(any(Context.class), any(Object.class), any(Pointcut.class))).thenReturn(mockProxy);
		when(mockProxy.getProxy()).thenReturn(mockBean);
		
		// Run
		aspectWeaver.weave(Robolectric.application, aspects);
		
		// Verify
		verify(mockBeanFactory, times(2)).loadBean(BEAN_NAME);
		verify(mockProxyFactory).createProxy(any(Context.class), any(Object.class), any(Pointcut.class));
		verify(mockBeanFactory).getBeanMap();
		assertTrue("Bean Map should have 1 bean entry", mockBeanMap.entrySet().size() == 1);
	}

	@Aspect
	private static class MockAspect {

		@com.clarionmedia.infinitum.aop.annotation.Before(within = { "java.util" })
		public void beforeAdvice_within(JoinPoint joinPoint) {

		}
		
		@com.clarionmedia.infinitum.aop.annotation.Before(beans = { BEAN_NAME + ".toString()" })
		public void beforeAdvice_beans(JoinPoint joinPoint) {

		}

	}

}
