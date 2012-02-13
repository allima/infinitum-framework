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
import java.util.Date;
import java.util.List;

import com.clarionmedia.infinitum.datetime.DateFormatter;

import android.content.ContentValues;

/**
 * This class provides methods to map domain models to database columns and vice
 * versa.
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
	 *            the {@link AbstractModel} to map
	 * @return <code>ContentValues</code> with the <code>AbstractModel</code>
	 *         entity's persistent fields mapped to their columns
	 */
	public static ContentValues mapModel(AbstractModel model) {
		if (!PersistenceResolution.isPersistent(model.getClass()))
			return null;
		ContentValues ret = new ContentValues();
		List<Field> fields = PersistenceResolution.getPersistentFields(model.getClass());
		for (Field f : fields) {
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

			// TODO: figure out a better way to do this!
			if (val instanceof String)
				ret.put(PersistenceResolution.getFieldColumnName(f), (String) val);
			else if (val instanceof Integer)
				ret.put(PersistenceResolution.getFieldColumnName(f), (Integer) val);
			else if (val instanceof Long)
				ret.put(PersistenceResolution.getFieldColumnName(f), (Long) val);
			else if (val instanceof Float)
				ret.put(PersistenceResolution.getFieldColumnName(f), (Float) val);
			else if (val instanceof Double)
				ret.put(PersistenceResolution.getFieldColumnName(f), (Double) val);
			else if (val instanceof Short)
				ret.put(PersistenceResolution.getFieldColumnName(f), (Short) val);
			else if (val instanceof Boolean)
				ret.put(PersistenceResolution.getFieldColumnName(f), (Boolean) val);
			else if (val instanceof Byte)
				ret.put(PersistenceResolution.getFieldColumnName(f), (Byte) val);
			else if (val instanceof byte[])
				ret.put(PersistenceResolution.getFieldColumnName(f), (byte[]) val);
			else if (val instanceof Long)
				ret.put(PersistenceResolution.getFieldColumnName(f), (Long) val);
			else if (val instanceof Character)
				ret.put(PersistenceResolution.getFieldColumnName(f), (String) val);
			else if (val instanceof Date)
				ret.put(PersistenceResolution.getFieldColumnName(f), DateFormatter.getDateAsISO8601String((Date) val));
			// TODO: add additional data types

		}
		return ret;
	}

}
