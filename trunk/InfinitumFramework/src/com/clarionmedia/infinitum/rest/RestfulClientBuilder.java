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

import java.lang.reflect.Field;

/**
 * <p>
 * Provides an API for constructing new {@link RestfulClient} instances. In
 * addition to acting as a factory, the purpose of this interface is to allow
 * for {@code RestfulClients} to be configured before retrieving instances.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/28/12
 */
public interface RestfulClientBuilder<T extends RestfulClient> {

	/**
	 * Builds a configured {@link RestfulClient} instance.
	 * 
	 * @return {@code RestfulClient}
	 */
	T build();

	/**
	 * Registers a {@link JsonDeserializer} for the given {@link Class} type.
	 * This deserializer will be used to deserialize any objects of the given
	 * type. Registering a {@code JsonDeserializer} for a {@code Class} which
	 * already has a {@code JsonDeserializer} registered for it will result in
	 * the previous {@code JsonDeserializer} being overridden.
	 * 
	 * @param type
	 *            the {@code Class} associated with this deserializer
	 * @param deserializer
	 *            the {@code JsonDeserializer} to register
	 */
	<E> void registerJsonDeserializer(Class<E> type, JsonDeserializer<E> deserializer);

	/**
	 * Registers the given {@link RestfulTypeAdapter} for the specified
	 * {@link Class} with this {@code RestfulClientBuilder} instance. The
	 * {@code RestfulTypeAdapter} allows a {@link Field} of this type to be
	 * mapped to a resource field in a web service. Registering a
	 * {@code RestfulTypeAdapter} for a {@code Class} which already has a
	 * {@code RestfulTypeAdapter} registered for it will result in the previous
	 * {@code RestfulTypeAdapter} being overridden.
	 * 
	 * @param type
	 *            the {@code Class} this {@code RestfulTypeAdapter} is for
	 * @param adapter
	 *            the {@code RestfulTypeAdapter} to register
	 */
	<E> void registerTypeAdapter(Class<E> type, RestfulTypeAdapter<E> adapter);

	/**
	 * Removes any current configurations for the {@link RestfulClient}.
	 */
	void clearConfiguration();

}
