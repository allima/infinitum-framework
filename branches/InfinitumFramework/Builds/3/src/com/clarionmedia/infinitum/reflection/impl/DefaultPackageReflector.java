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

import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.reflection.PackageReflector;

/**
 * <p>
 * This class provides reflection methods for working with packages
 * contained within projects that are using Infinitum and their contained
 * resources.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/14/12
 */
public class DefaultPackageReflector implements PackageReflector {

	@Override
	public Class<?> getClass(String className) {
		Class<?> c;
		try {
			c = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new InfinitumRuntimeException("Class '" + className + "' could not be resolved.");
		}
		return c;
	}

}
