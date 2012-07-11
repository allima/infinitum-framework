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

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

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
	public Set<Class<?>> getClassesWithAnnotation(
			Class<? extends Annotation> annotation) {
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
					Class<?> entryClass = dex.loadClass(entry, classLoader);
					if (entryClass != null) {
						if (entryClass.isAnnotationPresent(annotation))
							classes.add(entryClass);
					}
				}
			}

		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return classes;
	}

	@Override
	public Package scan(String packageName) {
		Set<String> subpackageNames = new TreeSet<String>();
		Set<String> classNames = new TreeSet<String>();
		Set<Class<?>> topLevelClasses = new TreeSet<Class<?>>(
				ORDER_CLASS_BY_NAME);
		findClasses(packageName, classNames, subpackageNames);
		for (String className : classNames) {
			try {
				topLevelClasses.add(Class.forName(className, false, getClass()
						.getClassLoader()));
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return new Package(this, subpackageNames, topLevelClasses);
	}

	/**
	 * Finds all classes and subpackages that are below the packageName and add
	 * them to the respective sets. Searches the package on the whole class
	 * path.
	 */
	private void findClasses(String packageName, Set<String> classNames,
			Set<String> subpackageNames) {
		String packagePrefix = packageName + '.';
		String pathPrefix = packagePrefix.replace('.', '/');
		for (String entry : getClassPath()) {
			File entryFile = new File(entry);
			if (entryFile.exists() && !entryFile.isDirectory()) {
				find(entryFile, pathPrefix, packageName, classNames,
						subpackageNames);
			}
		}
	}

	/**
	 * Gets the class path from the System Property "java.class.path" and splits
	 * it up into the individual elements.
	 */
	private String[] getClassPath() {
		String classPath = System.getProperty("java.class.path");
		String separator = System.getProperty("path.separator", ":");
		return classPath.split(Pattern.quote(separator));
	}

	/**
	 * Returns true if a given file name represents a toplevel class.
	 */
	private boolean isToplevelClass(String fileName) {
		return fileName.indexOf('$') < 0;
	}

	/**
	 * Finds all classes and sub packages that are below the packageName and add
	 * them to the respective sets. Searches the package in a single APK.
	 */
	private void find(File classPathEntry, String pathPrefix,
			String packageName, Set<String> classNames,
			Set<String> subpackageNames) {
		DexFile dexFile = null;
		try {
			dexFile = new DexFile(classPathEntry);
			Enumeration<String> apkClassNames = dexFile.entries();
			while (apkClassNames.hasMoreElements()) {
				String className = apkClassNames.nextElement();
				if (!className.startsWith(packageName)) {
					continue;
				}

				String subPackageName = packageName;
				int lastPackageSeparator = className.lastIndexOf('.');
				if (lastPackageSeparator > 0) {
					subPackageName = className.substring(0,
							lastPackageSeparator);
				}
				if (subPackageName.length() > packageName.length()) {
					subpackageNames.add(subPackageName);
				} else if (isToplevelClass(className)) {
					classNames.add(className);
				}
			}
		} catch (IOException ignore) {
			// okay, presumably the dex file didn't contain any classes
		} finally {
			if (dexFile != null) {
				try {
					dexFile.close();
				} catch (IOException ignore) {
				}
			}
		}
	}

}
