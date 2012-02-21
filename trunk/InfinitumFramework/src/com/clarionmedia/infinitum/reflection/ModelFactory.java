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

package com.clarionmedia.infinitum.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;

import android.database.Cursor;

import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.internal.DateFormatter;
import com.clarionmedia.infinitum.orm.OrmConstants;
import com.clarionmedia.infinitum.orm.exception.InvalidMappingException;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.PersistenceResolution;

/**
 * <p>
 * This class contains static methods for creating new instances of model
 * classes using reflection. Also provides methods for creating new, populated
 * instances from a SQLite {@link Cursor}. It's important to note that model
 * classes must contain an empty, parameterless constructor in order for these
 * methods to work. If no such constructor is present, a
 * {@link ModelConfigurationException} will be thrown at runtime.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/16/12
 */
public class ModelFactory {

	private static final String INSTANTIATION_ERROR = "Could not instantiate Object of type '%s'.";

	/**
	 * Constructs a domain model instance and populates its {@link Field}'s from
	 * the given {@link Cursor}. The precondition for this method is that the
	 * {@code Cursor} is currently at the row to convert to an {@link Object}
	 * from the correct table and is open.
	 * 
	 * @param cursor
	 *            the {@code Cursor} containing the row to convert to an
	 *            {@code Object}
	 * @param modelClass
	 *            the {@code Class} of the {@code Object} being instantiated
	 * @return a populated instance of the specified {@code Class}
	 * @throws ModelConfigurationException
	 *             if the specified model {@code Class} does not contain an
	 *             empty constructor
	 * @throws InfinitumRuntimeException
	 *             if the model could not be instantiated
	 */
	public static <T> T createFromCursor(Cursor cursor, Class<T> modelClass) throws ModelConfigurationException,
			InfinitumRuntimeException {
		T ret = null;
		try {
			Constructor<T> ctor = modelClass.getConstructor();
			ctor.setAccessible(true);
			ret = ctor.newInstance();
		} catch (SecurityException e) {
			throw new InfinitumRuntimeException(String.format(INSTANTIATION_ERROR, modelClass.getName()));
		} catch (NoSuchMethodException e) {
			throw new ModelConfigurationException(String.format(OrmConstants.NO_EMPTY_CONSTRUCTOR, modelClass.getName()));
		} catch (IllegalArgumentException e) {
			throw new InfinitumRuntimeException(String.format(INSTANTIATION_ERROR, modelClass.getName()));
		} catch (InstantiationException e) {
			throw new InfinitumRuntimeException(String.format(INSTANTIATION_ERROR, modelClass.getName()));
		} catch (IllegalAccessException e) {
			throw new InfinitumRuntimeException(String.format(INSTANTIATION_ERROR, modelClass.getName()));
		} catch (InvocationTargetException e) {
			throw new InfinitumRuntimeException(String.format(INSTANTIATION_ERROR, modelClass.getName()));
		}
		List<Field> fields = PersistenceResolution.getPersistentFields(modelClass);
		for (Field f : fields) {
			f.setAccessible(true);
			try {
				f.set(ret, getCursorValue(f, cursor));
			} catch (IllegalAccessException e) {
				throw new InfinitumRuntimeException(String.format(INSTANTIATION_ERROR, modelClass.getName()));
			}
		}
		// TODO: load relationships
		return ret;
	}

	private static Object getCursorValue(Field f, Cursor cursor) {
		int colIndex = cursor.getColumnIndex(PersistenceResolution.getFieldColumnName(f));
		Class<?> type = f.getType();

		// TODO: seeing this type-checking code a lot...I don't like it!
		if (type == String.class)
			return cursor.getString(colIndex);
		else if (type == Integer.class || type == int.class)
			return cursor.getInt(colIndex);
		else if (type == Long.class || type == long.class)
			return cursor.getLong(colIndex);
		else if (type == Float.class || type == float.class)
			return cursor.getFloat(colIndex);
		else if (type == Double.class || type == double.class)
			return cursor.getDouble(colIndex);
		else if (type == Short.class || type == short.class)
			return cursor.getShort(colIndex);
		else if (type == Boolean.class || type == boolean.class) {
			int b = cursor.getInt(colIndex);
			return b == 0 ? false : true;
		} else if (type == Byte.class || type == byte.class)
			return cursor.getBlob(colIndex)[0];
		else if (type == byte[].class)
			return cursor.getBlob(colIndex);
		else if (type == Character.class || type == char.class)
			return cursor.getString(colIndex).charAt(0);
		else if (type == Date.class) {
			String dateStr = cursor.getString(colIndex);
			return DateFormatter.parseStringAsDate(dateStr);
		} else
			throw new InvalidMappingException(String.format(OrmConstants.CANNOT_MAP_TYPE, f.getType().getSimpleName()));
	}
}
