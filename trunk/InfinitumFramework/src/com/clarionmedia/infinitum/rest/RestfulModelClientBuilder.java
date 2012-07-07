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
 * Provides an API for constructing new {@link RestfulModelClient} instances. In
 * addition to acting as a factory, the purpose of this interface is to allow
 * for {@code RestfulModelClients} to be configured before retrieving instances.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/28/12
 */
public interface RestfulModelClientBuilder {

	/**
	 * Builds a configured {@link RestfulModelClient} instance.
	 * 
	 * @return {@code RestfulModelClient}
	 */
	RestfulModelClient build();

	/**
	 * Registers a {@link Deserializer} for the given {@link Class} type. This
	 * deserializer will be used to deserialize any objects of the given type.
	 * Registering a {@code Deserializer} for a {@code Class} which already has
	 * a {@code Deserializer} registered for it will result in the previous
	 * {@code Deserializer} being overridden.
	 * 
	 * @param type
	 *            the {@code Class} associated with this deserializer
	 * @param deserializer
	 *            the {@code JsonDeserializer} to register
	 * @return {@code RestfulModelClientBuilder} to allow for method chaining
	 */
	<E> RestfulModelClientBuilder registerDeserializer(Class<E> type, Deserializer<E> deserializer);

	/**
	 * Registers the given {@link RestfulTypeAdapter} for the specified
	 * {@link Class} with this {@code RestfulModelClientBuilder} instance. The
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
	 * @return {@code RestfulModelClientBuilder} to allow for method chaining
	 */
	<E> RestfulModelClientBuilder registerTypeAdapter(Class<E> type, RestfulTypeAdapter<E> adapter);

	/**
	 * Removes any current configurations for the {@link RestfulModelClient}.
	 * 
	 * @return {@code RestfulModelClientBuilder} to allow for method chaining
	 */
	RestfulModelClientBuilder clearConfiguration();

}
