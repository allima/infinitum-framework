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

package com.clarionmedia.infinitum.orm;

import java.lang.reflect.Field;
import java.util.Map;

import com.clarionmedia.infinitum.orm.exception.InvalidMappingException;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.sqlite.SqliteMapper;
import com.clarionmedia.infinitum.orm.sqlite.SqliteTypeAdapter;
import com.clarionmedia.infinitum.orm.persistence.TypeAdapter;

/**
 * <p>
 * {@code ObjectMapper} provides an API for mapping domain objects to database
 * tables and vice versa. For mapping to SQLite databases, see this interface's
 * implementation {@link SqliteMapper}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/23/12
 */
public interface ObjectMapper {

	/**
	 * Returns a {@link ModelMap} object containing persistent model data values
	 * mapped to their respective columns.
	 * 
	 * @param model
	 *            the {@link Object} to map
	 * @return {@code ModelMap} with the entity's persistent fields mapped to
	 *         their columns
	 * @throws InvalidMappingException
	 *             if a type cannot be mapped
	 * @throws ModelConfigurationException
	 *             if the model is configured incorrectly
	 */
	ModelMap mapModel(Object model) throws InvalidMappingException, ModelConfigurationException;

	/**
	 * Registers the given {@link TypeAdapter} for the specified {@link Class}
	 * with this {@code SqliteMapper} instance. The {@code TypeAdapter} allows a
	 * {@link Field} of this type to be mapped to a database column. Registering
	 * a {@code TypeAdapter} for a {@code Class} which already has a
	 * {@code TypeAdapter} registered for it will result in the previous
	 * {@code TypeAdapter} being overridden.
	 * 
	 * @param type
	 *            the {@code Class} this {@code TypeAdapter} is for
	 * @param adapter
	 *            the {@code TypeAdapter} to register
	 */
	<T> void registerTypeAdapter(Class<T> type, SqliteTypeAdapter<T> adapter);
	
	/**
	 * Returns a {@link Map} containing all {@link TypeAdapter} instances
	 * registered with this {@code Session} and the {@link Class} instances in
	 * which they are registered for.
	 * 
	 * @return {@code Map<Class<?>, TypeAdapter<?>>
	 */
	Map<Class<?>, ? extends TypeAdapter<?>> getRegisteredTypeAdapters();

	/**
	 * Retrieves the {@link TypeAdapter} registered for the given {@link Class}.
	 * 
	 * @param type
	 *            the {@code Class} to retrieve the {@code TypeAdapter} for
	 * @return {@code TypeAdapter} for the specified type
	 * @throws InvalidMappingException
	 *             if there is no registered {@code TypeAdapter} for the given
	 *             {@code Class}
	 */
	<T> TypeAdapter<T> resolveType(Class<T> type) throws InvalidMappingException;

	/**
	 * Indicates if the given {@link Field} is a "text" data type as represented
	 * in a database.
	 * 
	 * @param f
	 *            the {@code Field} to check
	 * @return {@code true} if it is a text type, {@code false} if not
	 */
	boolean isTextColumn(Field f);

}
