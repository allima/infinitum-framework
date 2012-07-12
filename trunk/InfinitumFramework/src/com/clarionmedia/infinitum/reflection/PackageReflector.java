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

package com.clarionmedia.infinitum.reflection;

import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.Set;

import com.clarionmedia.infinitum.reflection.impl.Package;

/**
 * <p>
 * This interface provides reflection methods for working with packages
 * contained within projects that are using Infinitum and their contained
 * resources.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 05/17/12
 * @since 1.0
 */
public interface PackageReflector {

	/**
	 * Retrieves a {@link Class} instance for the {@code Class} described by the
	 * given package-qualified name. Returns {@code null} if the {@code Class}
	 * can not be found.
	 * 
	 * @param className
	 *            the package-qualified name of the desired {@code Class}
	 * @return instance of the {@code Class} or {@code null}if the {@code Class}
	 *         was not found
	 */
	Class<?> getClass(String className);

	/**
	 * Retrieves a {@link Package} describing the loadable classes whose package
	 * name is {@code packageName}.
	 */
	Package scan(String packageName);

	/**
	 * Retrieves a {@link Set} containing the {@link Class} instances which
	 * contain the given {@code Class}-level annotation(s).
	 * 
	 * @param annotations
	 *            the annotations to retrieve {@code Classes} by
	 * @return {@code Set} of {@code Classes} which contain {@code annotation}
	 */
	Set<Class<?>> getClassesWithAnnotations(Class<? extends Annotation>... annotations);

	/**
	 * {@link Comparator} for ordering a collection of classes based on their
	 * names.
	 */
	public static final Comparator<Class<?>> ORDER_CLASS_BY_NAME = new Comparator<Class<?>>() {

		@Override
		public int compare(Class<?> a, Class<?> b) {
			return a.getName().compareTo(b.getName());
		}

	};

}
