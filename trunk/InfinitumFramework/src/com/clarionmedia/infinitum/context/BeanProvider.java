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

import java.util.Map;

import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;

/**
 * <p>
 * Stores beans that have been configured in {@code infinitum.cfg.xml}. The
 * {@code BeanProvider} acts as a service locator for {@link InfinitumContext}.
 * Beans are retrieved by their name and registered by providing a name, class,
 * and field values.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 05/18/12
 */
public interface BeanProvider {

	/**
	 * Retrieves an instance of the bean with the given name. The name is
	 * configured in {@code infinitum.cfg.xml}.
	 * 
	 * @param name
	 *            the name of the bean to retrieve
	 * @return an instance of the bean
	 * @throws InfinitumConfigurationException
	 *             if the bean does not exist or could not be constructed
	 */
	Object loadBean(String name) throws InfinitumConfigurationException;

	/**
	 * Retrieves an instance of the bean with the given name and {@link Class}.
	 * The name is configured in {@code infinitum.cfg.xml}.
	 * 
	 * @param name
	 *            the name of the bean to retrieve
	 * @param clazz
	 *            the type of the bean to retrieve
	 * @return an instance of the bean
	 * @throws InfinitumConfigurationException
	 *             if the bean does not exist, could not be constructed, or is
	 *             of the wrong type
	 */
	<T> T loadBean(String name, Class<T> clazz)
			throws InfinitumConfigurationException;

	/**
	 * Checks if a bean with the given name exists.
	 * 
	 * @param name
	 *            the name to check
	 * @return {@code true} if it exists, {@code false} if not
	 */
	boolean beanExists(String name);

	/**
	 * Registers the bean with the given name and class name with the
	 * {@code BeanService}.
	 * 
	 * @param name
	 *            the name of the bean
	 * @param beanClass
	 *            the class name of the bean
	 * @param args
	 *            a {@link Map} of {@link Field} names and their values
	 */
	void registerBean(String name, String beanClass, Map<String, Object> args);

}
