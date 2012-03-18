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

package com.clarionmedia.infinitum.orm.sqlite;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;

import com.clarionmedia.infinitum.internal.Pair;
import com.clarionmedia.infinitum.internal.Primitives;
import com.clarionmedia.infinitum.orm.ObjectMapper;
import com.clarionmedia.infinitum.orm.OrmConstants;
import com.clarionmedia.infinitum.orm.exception.InvalidMappingException;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.PersistenceResolution;
import com.clarionmedia.infinitum.orm.persistence.TypeResolution;
import com.clarionmedia.infinitum.orm.persistence.TypeResolution.SqliteDataType;
import com.clarionmedia.infinitum.orm.relationship.ManyToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.ManyToOneRelationship;
import com.clarionmedia.infinitum.orm.relationship.ModelRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToOneRelationship;
import com.clarionmedia.infinitum.reflection.ClassReflector;

/**
 * <p>
 * This implementation of {@link ObjectMapper} provides methods to map domain
 * models to SQLite table columns and vice versa.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/13/12
 */
public class SqliteMapper implements ObjectMapper {

	private Map<Class<?>, SqliteTypeAdapter<?>> mTypeAdapters = new HashMap<Class<?>, SqliteTypeAdapter<?>>();
	
	public SqliteMapper() {
		mTypeAdapters.putAll(TypeResolution.sSqliteTypeResolvers);
	}

	@Override
	public SqliteModelMap mapModel(Object model) throws InvalidMappingException, ModelConfigurationException {
		// We do not map transient classes!
		if (!PersistenceResolution.isPersistent(model.getClass()))
			return null;
		SqliteModelMap ret = new SqliteModelMap(model);
		ContentValues values = new ContentValues();
		for (Field f : PersistenceResolution.getPersistentFields(model.getClass())) {
			// Don't map primary keys if they are autoincrementing
			if (PersistenceResolution.isFieldPrimaryKey(f) && PersistenceResolution.isPrimaryKeyAutoIncrement(f))
				continue;
			try {
				f.setAccessible(true);
				// Map relationships
				if (PersistenceResolution.isRelationship(f)) {
					mapRelationship(ret, model, f);
					continue;
				}
				// Map Field values
				mapField(values, model, f);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ret.setContentValues(values);
		return ret;
	}
	
	/**
	 * Retrieves a {@link SqliteTypeAdapter} for the given {@link Class}.
	 * 
	 * @param type
	 *            the {@code Class} to retrieve a {@code SqliteTypeResolver} for
	 * @return {@code SqliteTypeResolver} for the given type
	 * @throws InvalidMappingException
	 *             if no {@code SqliteTypeResolver} exists for the given
	 *             {@code Class}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> SqliteTypeAdapter<T> resolveType(Class<T> type) throws InvalidMappingException {
		type = Primitives.unwrap(type);
		if (mTypeAdapters.containsKey(type))
		    return (SqliteTypeAdapter<T>) mTypeAdapters.get(type);
		throw new InvalidMappingException(String.format(OrmConstants.CANNOT_MAP_TYPE, type.getSimpleName()));
	}
	
	@Override
	public <T> void registerTypeAdapter(Class<T> type, SqliteTypeAdapter<T> adapter) {
		mTypeAdapters.put(type, adapter);
	}

	@Override
	public Map<Class<?>, SqliteTypeAdapter<?>> getRegisteredTypeAdapters() {
		return mTypeAdapters;
	}
	
	@Override
	public boolean isTextColumn(Field f) {
		return getSqliteDataType(f) == SqliteDataType.TEXT;
	}
	
	/**
	 * Retrieves the SQLite data type associated with the given
	 * <code>Field</code>.
	 * 
	 * @param field
	 *            the <code>Field</code> to retrieve the SQLite data type for
	 * @return <code>SqliteDataType</code> that matches the given
	 *         <code>Field</code>
	 */
	public SqliteDataType getSqliteDataType(Field field) {
		SqliteDataType ret = null;
		Class<?> c = Primitives.unwrap(field.getType());
		if (mTypeAdapters.containsKey(c))
			ret = mTypeAdapters.get(c).getSqliteType();
		else if (TypeResolution.isDomainModel(c))
			ret = getSqliteDataType(PersistenceResolution.getPrimaryKeyField(c));
		return ret;
	}

	@SuppressWarnings("unchecked")
	private void mapRelationship(SqliteModelMap map, Object model, Field f) {
		try {
			if (PersistenceResolution.isRelationship(f)) {
				ModelRelationship rel = PersistenceResolution.getRelationship(f);
				Object related;
				switch (rel.getRelationType()) {
				case ManyToMany:
					ManyToManyRelationship mtm = (ManyToManyRelationship) rel;
					related = f.get(model);
					if (!(related instanceof Iterable))
						throw new ModelConfigurationException(String.format(OrmConstants.INVALID_MM_RELATIONSHIP, f.getName(), f.getDeclaringClass().getName()));
					map.addManyToManyRelationship(new Pair<ManyToManyRelationship, Iterable<Object>>(mtm, (Iterable<Object>) related));
					break;
				case ManyToOne:
					ManyToOneRelationship mto = (ManyToOneRelationship) rel;
					related = f.get(model);
					if (related != null && !TypeResolution.isDomainModel(related.getClass()))
						throw new ModelConfigurationException(String.format(OrmConstants.INVALID_MO_RELATIONSHIP, f.getName(), f.getDeclaringClass().getName()));
					map.addManyToOneRelationship(new Pair<ManyToOneRelationship, Object>(mto, related));
					break;
				case OneToMany:
					OneToManyRelationship otm = (OneToManyRelationship) rel;
					related = f.get(model);
					if (!(related instanceof Iterable))
						throw new ModelConfigurationException(String.format(OrmConstants.INVALID_OM_RELATIONSHIP, f.getName(), f.getDeclaringClass().getName()));
					map.addOneToManyRelationship(new Pair<OneToManyRelationship, Iterable<Object>>(otm, (Iterable<Object>) related));
					break;
				case OneToOne:
					OneToOneRelationship oto = (OneToOneRelationship) rel;
					related = f.get(model);
					if (related != null && !TypeResolution.isDomainModel(related.getClass()))
						throw new ModelConfigurationException(String.format(OrmConstants.INVALID_OO_RELATIONSHIP, f.getName(), f.getDeclaringClass().getName()));
					map.addOneToOneRelationship(new Pair<OneToOneRelationship, Object>(oto, related));
					break;
				}
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Map Field value to ContentValues
	private void mapField(ContentValues values, Object model, Field field) throws InvalidMappingException, IllegalArgumentException, IllegalAccessException {
		Object val = null;
		// We need to use the Field's getter if model is a proxy
		if (TypeResolution.isDomainProxy(model.getClass()))
			val = ClassReflector.invokeGetter(field, model);
		// Otherwise just use normal reflection...
		else
			val = field.get(model);
		String colName = PersistenceResolution.getFieldColumnName(field);
		resolveType(field.getType()).mapObjectToColumn(val, colName, values);
	}
	
}
