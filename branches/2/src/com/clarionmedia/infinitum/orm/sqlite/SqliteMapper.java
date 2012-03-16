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
import java.util.Date;
import java.util.List;

import android.content.ContentValues;

import com.clarionmedia.infinitum.internal.DateFormatter;
import com.clarionmedia.infinitum.internal.Pair;
import com.clarionmedia.infinitum.internal.Primitives;
import com.clarionmedia.infinitum.orm.ObjectMapper;
import com.clarionmedia.infinitum.orm.OrmConstants;
import com.clarionmedia.infinitum.orm.exception.InvalidMappingException;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.PersistenceResolution;
import com.clarionmedia.infinitum.orm.persistence.TypeResolution;
import com.clarionmedia.infinitum.orm.relationship.ManyToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.ManyToOneRelationship;
import com.clarionmedia.infinitum.orm.relationship.ModelRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToOneRelationship;
import com.clarionmedia.infinitum.reflection.ClassReflector;

/**
 * <p>
 * This class provides methods to map domain models to SQLite table columns and
 * vice versa.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/13/12
 */
public class SqliteMapper implements ObjectMapper {

	@Override
	public SqliteModelMap mapModel(Object model)
			throws InvalidMappingException, ModelConfigurationException {
		if (!PersistenceResolution.isPersistent(model.getClass()))
			return null;
		SqliteModelMap ret = new SqliteModelMap(model);
		ContentValues values = new ContentValues();
		List<Field> fields = PersistenceResolution.getPersistentFields(model
				.getClass());
		for (Field f : fields) {
			if (PersistenceResolution.isFieldPrimaryKey(f)
					&& PersistenceResolution.isPrimaryKeyAutoIncrement(f))
				continue;
			Object val = null;
			try {
				f.setAccessible(true);
				// Map relationships
				if (PersistenceResolution.isRelationship(f)) {
					mapRelationship(ret, model, f);
					continue;
				}
				// We need to use the Field's getter if model is a proxy
				if (TypeResolution.isDomainProxy(model.getClass()))
					val = ClassReflector.invokeGetter(f, model);
				else
					val = f.get(model);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Map Field values
			mapValue(values, val, f);
		}
		ret.setContentValues(values);
		return ret;
	}

	private void mapValue(ContentValues values, Object val, Field f)
			throws InvalidMappingException {
		Class<?> type = Primitives.unwrap(f.getType());
		String colName = PersistenceResolution.getFieldColumnName(f);
		// TODO: figure out a better way to do this!
		// Possibly use TypeResolution
		// also figure out way to add support for other types
		if (type == String.class)
			values.put(colName, (String) val);
		else if (type == int.class)
			values.put(colName, (Integer) val);
		else if (type == long.class)
			values.put(colName, (Long) val);
		else if (type == float.class)
			values.put(colName, (Float) val);
		else if (type == double.class)
			values.put(colName, (Double) val);
		else if (type == short.class)
			values.put(colName, (Short) val);
		else if (type == boolean.class)
			values.put(colName, (Boolean) val);
		else if (type == byte.class)
			values.put(colName, (Byte) val);
		else if (type == byte[].class)
			values.put(colName, (byte[]) val);
		else if (type == Character.class)
			values.put(colName, (String) val);
		else if (type == Date.class)
			values.put(colName,
					DateFormatter.getDateAsISO8601String((Date) val));
		else
			throw new InvalidMappingException(String.format(
					OrmConstants.CANNOT_MAP_TYPE, f.getType().getSimpleName()));
	}

	@SuppressWarnings("unchecked")
	private void mapRelationship(SqliteModelMap map, Object model, Field f) {
		try {
			if (PersistenceResolution.isRelationship(f)) {
				ModelRelationship rel = PersistenceResolution
						.getRelationship(f);
				Object related;
				switch (rel.getRelationType()) {
				case ManyToMany:
					ManyToManyRelationship mtm = (ManyToManyRelationship) rel;
					related = f.get(model);
					if (!(related instanceof Iterable))
						throw new ModelConfigurationException(String.format(
								OrmConstants.INVALID_MM_RELATIONSHIP,
								f.getName(), f.getDeclaringClass().getName()));
					map.addAggregateRelationship(new Pair<ModelRelationship, Iterable<Object>>(
							mtm, (Iterable<Object>) related));
					break;
				case ManyToOne:
					ManyToOneRelationship mto = (ManyToOneRelationship) rel;
					related = f.get(model);
					if (related != null
							&& !TypeResolution
									.isDomainModel(related.getClass()))
						throw new ModelConfigurationException(String.format(
								OrmConstants.INVALID_MO_RELATIONSHIP,
								f.getName(), f.getDeclaringClass().getName()));
					map.addManyToOneRelationship(new Pair<ManyToOneRelationship, Object>(
							mto, related));
					break;
				case OneToMany:
					OneToManyRelationship otm = (OneToManyRelationship) rel;
					related = f.get(model);
					if (!(related instanceof Iterable))
						throw new ModelConfigurationException(String.format(
								OrmConstants.INVALID_OM_RELATIONSHIP,
								f.getName(), f.getDeclaringClass().getName()));
					map.addOneToManyRelationship(new Pair<OneToManyRelationship, Iterable<Object>>(
							otm, (Iterable<Object>) related));
					break;
				case OneToOne:
					OneToOneRelationship oto = (OneToOneRelationship) rel;
					related = f.get(model);
					if (related != null
							&& !TypeResolution
									.isDomainModel(related.getClass()))
						throw new ModelConfigurationException(String.format(
								OrmConstants.INVALID_OO_RELATIONSHIP,
								f.getName(), f.getDeclaringClass().getName()));
					map.addOneToOneRelationship(new Pair<OneToOneRelationship, Object>(
							oto, related));
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
}
