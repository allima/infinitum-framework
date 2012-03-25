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

package com.clarionmedia.infinitum.rest;

import java.io.Serializable;

import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.rest.impl.BasicRestfulClient;

/**
 * <p>
 * This interface provides an API for communicating with a RESTful web service
 * using objects. Infinitum provides an implementation called
 * {@link BasicRestfulClient}, which can be extended or re-implemented for
 * specific business needs.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/27/12
 */
public interface RestfulClient {

	/**
	 * Makes an HTTP request to the web service to save the given model.
	 * 
	 * @param model
	 *            the model to save
	 * @return {@code true} if the save succeeded, {@code false} if not
	 */
	boolean save(Object model);

	/**
	 * Makes an HTTP reques to the web service to delete the given model.
	 * 
	 * @param model
	 *            the model to delete
	 * @return {@code true} if the delete succeeded, {@code false} if not
	 */
	boolean delete(Object model);

	/**
	 * Makes an HTTP request to the web service to update the given model or
	 * save it if it does not exist in the database.
	 * 
	 * @param model
	 *            the model to save or update
	 * @return 0 if the model was updated, 1 if the model was saved, or -1 if
	 *         the operation failed
	 */
	int saveOrUpdate(Object model);

	/**
	 * Returns an instance of the given persistent model {@link Class} as
	 * identified by the specified primary key or {@code null} if no such entity
	 * exists.
	 * 
	 * @param c
	 *            the {@code Class} of the persistent instance to load
	 * @param id
	 *            the primary key value of the persistent instance to load
	 * @return the persistent instance
	 * @throws InfinitumRuntimeException
	 *             if the specified {@code Class} is marked transient
	 * @throws IllegalArgumentException
	 *             if an invalid primary key is provided
	 */
	<T> T load(Class<T> c, Serializable id) throws InfinitumRuntimeException, IllegalArgumentException;

	/**
	 * Registers the given {@link JsonDeserializer} for the given {@link Class}
	 * type. Registering a {@code JsonDeserializer} for a {@code Class} which
	 * already has a {@code JsonDeserializer} registered for it will result in
	 * the previous {@code JsonDeserializer} being overridden.
	 * 
	 * @param type
	 *            the {@code Class} to associate this deserializer with
	 * @param deserializer
	 *            the {@code JsonDeserializer} to use when deserializing
	 *            {@code Objects} of the given type
	 */
	<T> void registerJsonDeserializer(Class<T> type, JsonDeserializer<T> deserializer);

}
