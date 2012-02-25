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

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;

import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.internal.DateFormatter;
import com.clarionmedia.infinitum.orm.ManyToManyRelationship;
import com.clarionmedia.infinitum.orm.OrmConstants;
import com.clarionmedia.infinitum.orm.annotation.ManyToMany;
import com.clarionmedia.infinitum.orm.exception.InvalidMappingException;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.PersistenceResolution;
import com.clarionmedia.infinitum.orm.sql.SqlBuilder;
import com.clarionmedia.infinitum.orm.sql.SqlExecutor;
import com.clarionmedia.infinitum.orm.sqlite.SqliteExecutor;
import com.clarionmedia.infinitum.orm.sqlite.SqliteResult;

/**
 * <p>
 * Implementation of {@link ModelFactory}, providing methods for creating new
 * instances of model classes using reflection. Also provides methods for
 * creating new, populated instances from a SQLite {@link Cursor}. It's
 * important to note that model classes must contain an empty, parameterless
 * constructor in order for these methods to work. If no such constructor is
 * present, a {@link ModelConfigurationException} will be thrown at runtime.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/16/12
 */
public class ModelFactoryImpl implements ModelFactory {

	private static final String INSTANTIATION_ERROR = "Could not instantiate Object of type '%s'.";

	private Map<Integer, Object> mObjectMap = new Hashtable<Integer, Object>();
	private SqlExecutor mExecutor;

	public ModelFactoryImpl(Context context) {
		mExecutor = new SqliteExecutor(context);
	}

	@Override
	public <T> T createFromCursor(Cursor cursor, Class<T> modelClass) throws ModelConfigurationException,
			InfinitumRuntimeException {
		T ret = createFromCursorRec(cursor, modelClass, null);
		return ret;
	}

	@SuppressWarnings("unchecked")
	private <T> T createFromCursorRec(Cursor cursor, Class<T> modelClass, Object parent)
			throws ModelConfigurationException, InfinitumRuntimeException {
		T ret = null;
		try {
			Constructor<T> ctor = modelClass.getConstructor();
			ctor.setAccessible(true);
			ret = ctor.newInstance();
		} catch (SecurityException e) {
			throw new InfinitumRuntimeException(String.format(INSTANTIATION_ERROR, modelClass.getName()));
		} catch (NoSuchMethodException e) {
			throw new ModelConfigurationException(
					String.format(OrmConstants.NO_EMPTY_CONSTRUCTOR, modelClass.getName()));
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
				if (f.isAnnotationPresent(ManyToMany.class)) {
					// TODO Only supporting Lists currently
					f.set(ret, new ArrayList<Object>());
				} else {
					f.set(ret, getCursorValue(f, cursor));
				}
			} catch (IllegalAccessException e) {
				throw new InfinitumRuntimeException(String.format(INSTANTIATION_ERROR, modelClass.getName()));
			}
		}
		int objHash = PersistenceResolution.computeModelHash(ret);
		if (mObjectMap.containsKey(objHash))
			return (T) mObjectMap.get(objHash);
		mObjectMap.put(objHash, ret);
		loadRelationships(ret);
		return ret;
	}

	private <T> void loadRelationships(T obj) {
		for (Field f : PersistenceResolution.getPersistentFields(obj.getClass())) {
			f.setAccessible(true);
			if (!f.isAnnotationPresent(ManyToMany.class))
				continue;
			try {
				ManyToManyRelationship rel = new ManyToManyRelationship(f);
				// TODO Add reflexive M:M support
				Class<?> direction = obj.getClass() == rel.getFirstType() ? rel.getSecondType() : rel.getFirstType();
				Field pk = PersistenceResolution.getPrimaryKeyField(obj.getClass());
				String sql = SqlBuilder.createManyToManyJoinQuery(rel, (Serializable) pk.get(obj), direction);
				SqliteResult result = (SqliteResult) mExecutor.execute(sql);
				// TODO Currently only supporting Lists
				List<Object> related = new LinkedList<Object>();
				while (result.getCursor().moveToNext())
					related.add(createFromCursor(result.getCursor(), direction));
				result.close();
				f.set(obj, related);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private Object getCursorValue(Field f, Cursor cursor) {
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
