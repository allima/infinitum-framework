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

import com.clarionmedia.infinitum.context.impl.ContextFactory;
import com.clarionmedia.infinitum.internal.Pair;
import com.clarionmedia.infinitum.logging.Logger;
import com.clarionmedia.infinitum.orm.exception.InvalidMappingException;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.persistence.TypeAdapter;
import com.clarionmedia.infinitum.orm.persistence.TypeResolutionPolicy;
import com.clarionmedia.infinitum.orm.persistence.impl.DefaultTypeResolutionPolicy;
import com.clarionmedia.infinitum.orm.relationship.ManyToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.ManyToOneRelationship;
import com.clarionmedia.infinitum.orm.relationship.ModelRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToOneRelationship;
import com.clarionmedia.infinitum.orm.sqlite.impl.SqliteMapper;
import com.clarionmedia.infinitum.reflection.ClassReflector;
import com.clarionmedia.infinitum.reflection.impl.DefaultClassReflector;
import com.clarionmedia.infinitum.rest.impl.RestfulMapper;

/**
 * <p>
 * {@code ObjectMapper} provides an API for mapping domain objects to database
 * tables and vice versa. For mapping to SQLite databases, see this class's
 * concrete implementation {@link SqliteMapper} and for mapping to a RESTful web
 * service, see the {@link RestfulMapper} implementation.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/23/12
 */
public abstract class ObjectMapper {

	protected TypeResolutionPolicy mTypePolicy;
	protected ClassReflector mClassReflector;
	protected Logger mLogger;

	/**
	 * Constructs a new {@code ObjectMapper}.
	 */
	public ObjectMapper() {
		mTypePolicy = new DefaultTypeResolutionPolicy();
		mClassReflector = new DefaultClassReflector();
		mLogger = Logger.getInstance(getClass().getSimpleName());
	}

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
	public abstract ModelMap mapModel(Object model) throws InvalidMappingException, ModelConfigurationException;

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
	public abstract <T> void registerTypeAdapter(Class<T> type, TypeAdapter<T> adapter);

	/**
	 * Returns a {@link Map} containing all {@link TypeAdapter} instances
	 * registered with this {@code ObjectMapper} and the {@link Class} instances
	 * in which they are registered for.
	 * 
	 * @return {@code Map<Class<?>, TypeAdapter<?>>
	 */
	public abstract Map<Class<?>, ? extends TypeAdapter<?>> getRegisteredTypeAdapters();

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
	public abstract <T> TypeAdapter<T> resolveType(Class<T> type) throws InvalidMappingException;

	/**
	 * Indicates if the given {@link Field} is a "text" data type as represented
	 * in a database.
	 * 
	 * @param f
	 *            the {@code Field} to check
	 * @return {@code true} if it is a text type, {@code false} if not
	 */
	public abstract boolean isTextColumn(Field f);

	/**
	 * Maps the given relationship {@link Field} to the given {@link ModelMap}.
	 * 
	 * @param map
	 *            the {@code ModelMap} to add the relationship to
	 * @param model
	 *            the model containing the relationship
	 * @param f
	 *            the relationship {@code Field}
	 */
	@SuppressWarnings("unchecked")
	protected void mapRelationship(ModelMap map, Object model, Field f) {
		try {
			PersistencePolicy policy = ContextFactory.getInstance().getPersistencePolicy();
			if (policy.isRelationship(f)) {
				ModelRelationship rel = policy.getRelationship(f);
				Object related;
				switch (rel.getRelationType()) {
				case ManyToMany:
					ManyToManyRelationship mtm = (ManyToManyRelationship) rel;
					related = f.get(model);
					if (!(related instanceof Iterable))
						throw new ModelConfigurationException(String.format(OrmConstants.INVALID_MM_RELATIONSHIP,
								f.getName(), f.getDeclaringClass().getName()));
					map.addManyToManyRelationship(new Pair<ManyToManyRelationship, Iterable<Object>>(mtm,
							(Iterable<Object>) related));
					break;
				case ManyToOne:
					ManyToOneRelationship mto = (ManyToOneRelationship) rel;
					related = f.get(model);
					if (related != null && !mTypePolicy.isDomainModel(related.getClass()))
						throw new ModelConfigurationException(String.format(OrmConstants.INVALID_MO_RELATIONSHIP,
								f.getName(), f.getDeclaringClass().getName()));
					map.addManyToOneRelationship(new Pair<ManyToOneRelationship, Object>(mto, related));
					break;
				case OneToMany:
					OneToManyRelationship otm = (OneToManyRelationship) rel;
					related = f.get(model);
					if (!(related instanceof Iterable))
						throw new ModelConfigurationException(String.format(OrmConstants.INVALID_OM_RELATIONSHIP,
								f.getName(), f.getDeclaringClass().getName()));
					map.addOneToManyRelationship(new Pair<OneToManyRelationship, Iterable<Object>>(otm,
							(Iterable<Object>) related));
					break;
				case OneToOne:
					OneToOneRelationship oto = (OneToOneRelationship) rel;
					related = f.get(model);
					if (related != null && !mTypePolicy.isDomainModel(related.getClass()))
						throw new ModelConfigurationException(String.format(OrmConstants.INVALID_OO_RELATIONSHIP,
								f.getName(), f.getDeclaringClass().getName()));
					map.addOneToOneRelationship(new Pair<OneToOneRelationship, Object>(oto, related));
					break;
				}
			}
		} catch (IllegalArgumentException e) {
			mLogger.error("Unable to map relationship for field " + f.getName() + " in '"
					+ f.getDeclaringClass().getName() + "'", e);
		} catch (IllegalAccessException e) {
			mLogger.error("Unable to map relationship for field " + f.getName() + " in '"
					+ f.getDeclaringClass().getName() + "'", e);
		}
	}

}
