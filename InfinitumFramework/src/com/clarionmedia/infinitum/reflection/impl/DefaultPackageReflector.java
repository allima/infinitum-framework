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

package com.clarionmedia.infinitum.reflection.impl;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.reflection.PackageReflector;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

/**
 * <p>
 * This class provides reflection methods for working with packages contained
 * within projects that are using Infinitum and their contained resources.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/14/12
 * @since 1.0
 */
public class DefaultPackageReflector implements PackageReflector {

	@Override
	public Class<?> getClass(String className) {
		Class<?> c;
		try {
			c = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new InfinitumRuntimeException("Class '" + className
					+ "' could not be resolved.");
		}
		return c;
	}

	@Override
	public Set<Class<?>> getPackageClasses(String... packageNames) {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		Field dexField;
		try {
			dexField = PathClassLoader.class.getDeclaredField("mDexs");
			dexField.setAccessible(true);

			PathClassLoader classLoader = (PathClassLoader) Thread
					.currentThread().getContextClassLoader();

			DexFile[] dexs = (DexFile[]) dexField.get(classLoader);
			for (DexFile dex : dexs) {
				Enumeration<String> entries = dex.entries();
				while (entries.hasMoreElements()) {
					String entry = entries.nextElement();
					for (String packageName : packageNames) {
						if (entry.toLowerCase().startsWith(packageName.toLowerCase())) {
							Class<?> entryClass = dex.loadClass(entry, classLoader);
							classes.add(entryClass);
							break;
						}
					}
				}
			}

		} catch (SecurityException e) {
			throw new InfinitumRuntimeException("Component-scanning is not supported in this environment.");
		} catch (NoSuchFieldException e) {
			throw new InfinitumRuntimeException("Component-scanning is not supported in this environment.");
		} catch (IllegalArgumentException e) {
			throw new InfinitumRuntimeException("Component-scanning is not supported in this environment.");
		} catch (IllegalAccessException e) {
			throw new InfinitumRuntimeException("Component-scanning is not supported in this environment.");
		} catch (ClassCastException e) {
			throw new InfinitumRuntimeException("Component-scanning is not supported in this environment.");
		}
		return classes;
	}

}
