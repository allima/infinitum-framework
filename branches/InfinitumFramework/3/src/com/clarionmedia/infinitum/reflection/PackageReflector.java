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


/**
 * <p>
 * This interface provides reflection methods for working with packages
 * contained within projects that are using Infinitum and their contained
 * resources.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 05/17/12
 */
public interface PackageReflector {
	
	/**
	 * Retrieves a <code>Class</code> instance for the <code>Class</code>
	 * described by the given package-qualified name. Returns <code>null</code>
	 * if the <code>Class</code> can not be found.
	 * 
	 * @param className
	 *            the package-qualified name of the desired <code>Class</code>
	 * @return instance of the <code>Class</code> or <code>null</code> if the
	 *         <code>Class</code> was not found
	 */
	Class<?> getClass(String className);

}
