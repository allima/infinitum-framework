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

package com.clarionmedia.infinitum.orm.persistence;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;

import com.clarionmedia.infinitum.datetime.DateFormatter;
import com.clarionmedia.infinitum.orm.Constants;
import com.clarionmedia.infinitum.orm.exception.InvalidMappingException;

/**
 * <p>
 * This class provides methods to map domain models to database columns and vice
 * versa.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/13/12
 */
public class ObjectMapper {

	/**
	 * Returns a <code>ContentValues</code> object containing persistent model
	 * data values mapped to their respective columns.
	 * 
	 * @param model
	 *            the <code>Object</code> to map
	 * @return <code>ContentValues</code> with the entity's persistent fields
	 *         mapped to their columns
	 * @throws InvalidMappingException
	 *             if a type cannot be mapped
	 */
	public ContentValues mapModel(Object model) throws InvalidMappingException {
		if (!PersistenceResolution.isPersistent(model.getClass()))
			return null;
		ContentValues ret = new ContentValues();
		List<Field> fields = PersistenceResolution.getPersistentFields(model.getClass());
		for (Field f : fields) {
			if (PersistenceResolution.isFieldPrimaryKey(f) && PersistenceResolution.isPrimaryKeyAutoIncrement(f))
				continue;
			Object val = null;
			try {
				f.setAccessible(true);
				val = f.get(model);
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
			if (val instanceof String)
				ret.put(colName, (String) val);
			else if (val instanceof Integer)
				ret.put(colName, (Integer) val);
			else if (val instanceof Long)
				ret.put(colName, (Long) val);
			else if (val instanceof Float)
				ret.put(colName, (Float) val);
			else if (val instanceof Double)
				ret.put(colName, (Double) val);
			else if (val instanceof Short)
				ret.put(colName, (Short) val);
			else if (val instanceof Boolean)
				ret.put(colName, (Boolean) val);
			else if (val instanceof Byte)
				ret.put(colName, (Byte) val);
			else if (val instanceof byte[])
				ret.put(colName, (byte[]) val);
			else if (val instanceof Character)
				ret.put(colName, (String) val);
			else if (val instanceof Date)
				ret.put(colName, DateFormatter.getDateAsISO8601String((Date) val));
			else
				throw new InvalidMappingException(String.format(Constants.CANNOT_MAP_TYPE, f.getType().getSimpleName()));

		}
		return ret;
	}

}
