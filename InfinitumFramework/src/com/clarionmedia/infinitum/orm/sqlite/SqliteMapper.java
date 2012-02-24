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
import com.clarionmedia.infinitum.internal.Primitives;
import com.clarionmedia.infinitum.orm.ObjectMapper;
import com.clarionmedia.infinitum.orm.OrmConstants;
import com.clarionmedia.infinitum.orm.annotation.ManyToMany;
import com.clarionmedia.infinitum.orm.exception.InvalidMappingException;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.PersistenceResolution;

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

	@SuppressWarnings("unchecked")
	@Override
	public SqliteModelMap mapModel(Object model) throws InvalidMappingException, ModelConfigurationException {
		if (!PersistenceResolution.isPersistent(model.getClass()))
			return null;
		SqliteModelMap ret = new SqliteModelMap(model);
		ContentValues values = new ContentValues();
		List<Field> fields = PersistenceResolution.getPersistentFields(model.getClass());
		for (Field f : fields) {
			if (PersistenceResolution.isFieldPrimaryKey(f) && PersistenceResolution.isPrimaryKeyAutoIncrement(f))
				continue;
			Class<?> type = Primitives.unwrap(f.getType());
			Object val = null;
			try {
				f.setAccessible(true);
				val = f.get(model);

				// Map relationships
				if (f.isAnnotationPresent(ManyToMany.class)) {
					Object relationship = f.get(model);
					if (!(relationship instanceof Iterable))
						throw new ModelConfigurationException(String.format(OrmConstants.INVALID_MM_RELATIONSHIP,
								f.getName(), f.getDeclaringClass().getName()));
					ret.addRelationship((Iterable<Object>) relationship);
					continue;
				}

			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

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
				values.put(colName, DateFormatter.getDateAsISO8601String((Date) val));
			else
				throw new InvalidMappingException(String.format(OrmConstants.CANNOT_MAP_TYPE, f.getType()
						.getSimpleName()));

		}
		ret.setContentValues(values);
		return ret;
	}
}
