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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.clarionmedia.infinitum.orm.Constants.PersistenceMode;
import com.clarionmedia.infinitum.orm.annotation.Column;
import com.clarionmedia.infinitum.orm.annotation.Entity;
import com.clarionmedia.infinitum.orm.annotation.NotNull;
import com.clarionmedia.infinitum.orm.annotation.Persistence;
import com.clarionmedia.infinitum.orm.annotation.PrimaryKey;
import com.clarionmedia.infinitum.orm.annotation.Table;
import com.clarionmedia.infinitum.orm.annotation.Unique;

/**
 * <p>
 * This class provides runtime resolution for model persistence through Java
 * annotations or XML mappings. The latter has not yet been implemented. Model
 * fields can be marked as transient or persistent using the {@link Persistence}
 * annotation. If no annotation is provided, the field will be marked as
 * persistent by default.
 * </p>
 * 
 * <p>
 * If using annotation configurations, domain model classes should be located
 * within a single package which is referenced in <code>infinitum.cfg.xml</code>
 * using the <code>domainPackage</code> element. For example,
 * <code>&lt;property name="domainPackage"&gt;com.foo.bar.domain&lt;/property&gt;</code>
 * . However, domain classes can be individually registered in
 * <code>infinitum.cfg.xml</code> using
 * <code>&lt;model resource="com.foo.domain.MyModel" /&gt;</code> in the
 * <code>domain</code> element.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/12/12
 */
public class PersistenceResolution {

	// This Map caches which fields are persistent
	private static Map<Class<?>, List<Field>> sPersistenceCache;

	// This Map caches the field-column map
	private static Map<Field, String> sColumnCache;

	// This Map caches the primary key Fields for each persistent class
	private static Map<Class<?>, List<Field>> sPrimaryKeyCache;

	// This Map caches the "nullability" of Fields
	private static Map<Field, Boolean> sFieldNullableCache;

	// This Map caches the uniqueness of Fields
	private static Map<Field, Boolean> sFieldUniqueCache;

	static {
		sPersistenceCache = new Hashtable<Class<?>, List<Field>>();
		sColumnCache = new Hashtable<Field, String>();
		sPrimaryKeyCache = new Hashtable<Class<?>, List<Field>>();
		sFieldNullableCache = new Hashtable<Field, Boolean>();
		sFieldUniqueCache = new Hashtable<Field, Boolean>();
	}

	/**
	 * Indicates if the given <code>Class</code> is persistent or transient.
	 * Persistence is denoted by the {@link Entity} annotation.
	 * <code>Entity's</code> mode can be set to <code>transient</code> or
	 * <code>persistent</code. If the mode is missing or if the annotation
	 * itself is missing from a registered domain model, it will be marked
	 * persistent by default.
	 * 
	 * @param c
	 *            the <code>Class</code> to check persistence for
	 * @return <code>true</code> if persistent, <code>false</code> if transient
	 */
	public static boolean isPersistent(Class<?> c) {
		Entity entity = c.getAnnotation(Entity.class);
		if (entity == null || entity.mode() == PersistenceMode.Persistent)
			return true;
		else
			return false;
	}

	/**
	 * Retrieves the name of the database table for the specified
	 * <code>Class</code>. If the <code>Class</code> is transient, this method
	 * will return null. The table name can be specified using the {@link Table}
	 * annotation. If the annotation is missing, the class name, completely
	 * lowercase, will be used as the table name.
	 * 
	 * @param c
	 *            the <code>Class</code> to retrieve the table name for
	 * @return the name of the database table for the specified domain model
	 *         <code>Class</code>
	 */
	public static String getModelTableName(Class<?> c) {
		if (!isPersistent(c))
			return null;
		String ret;
		Table table = c.getAnnotation(Table.class);
		if (table == null)
			ret = c.getSimpleName().toLowerCase();
		else
			ret = table.name();
		return ret;
	}

	/**
	 * Retrieves a <code>List</code> of all persistent <code>Fields</code> for
	 * the given <code>Class</code>. <code>Field</code> persistence can be
	 * configured using the {@link Persistence} annotation by setting the
	 * <code>mode</code> to <code>persistent</code>. Marking a
	 * <code>Field</code> as <code>transient</code> means that it will not be
	 * persisted. If the annotation is missing, the <code>Field</code> is
	 * persistent by default.
	 * 
	 * @param c
	 *            the <code>Class</code> to retrieve persistent
	 *            <code>Fields</code> for
	 * @return <code>List</code> of all persistent <code>Fields</code> for the
	 *         specified <code>Class</code>
	 */
	public static List<Field> getPersistentFields(Class<?> c) {
		if (sPersistenceCache.containsKey(c))
			return sPersistenceCache.get(c);
		List<Field> ret = new ArrayList<Field>();
		List<Field> fields = getAllFields(c);
		for (Field f : fields) {
			Persistence persistence = f.getAnnotation(Persistence.class);
			PrimaryKey pk = f.getAnnotation(PrimaryKey.class);
			if ((persistence == null || persistence.mode() == PersistenceMode.Persistent) || pk != null)
				ret.add(f);
		}
		sPersistenceCache.put(c, ret);
		return ret;
	}

	/**
	 * Retrieves a <code>List</code> of all primary key <code>Fields</code> for
	 * the given <code>Class</code>. <code>Fields</code> can be marked as a
	 * primary key using the {@link PrimaryKey} annotation. If the annotation is
	 * missing from the class hierarchy, Infinitum will look for a
	 * <code>Field</code> called <code>mId</code> or <code>id</code> to use as
	 * the primary key.
	 * 
	 * @param c
	 *            the <code>Class</code> to retrieve primary key
	 *            <code>Fields</code> for
	 * @return <code>List</code> of all primary key <code>Fields</code> for the
	 *         specified <code>Class</code>
	 */
	public static List<Field> getPrimaryKeyFields(Class<?> c) {
		if (sPrimaryKeyCache.containsKey(c))
			return sPrimaryKeyCache.get(c);
		List<Field> ret = new ArrayList<Field>();
		List<Field> fields = getAllFields(c);
		for (Field f : fields) {
			PrimaryKey pk = f.getAnnotation(PrimaryKey.class);
			if (pk != null)
				ret.add(f);
		}
		// Look for id fields if the annotation is missing
		if (ret.size() == 0) {
			Field f = findPrimaryKeyField(c);
			if (f != null)
				ret.add(f);
		}
		sPrimaryKeyCache.put(c, ret);
		return ret;
	}

	/**
	 * Retrieves a <code>List</code> of all unique <code>Fields</code> for the
	 * given <code>Class</code>. <code>Fields</code> can be marked unique using
	 * the {@link Unique} annotation.
	 * 
	 * @param c
	 *            the <code>Class</code> to retrieve unique <code>Fields</code>
	 *            for
	 * @return <code>List</code> of all unique <code>Fields</code> for the
	 *         specified <code>Class</code>
	 */
	public static List<Field> getUniqueFields(Class<?> c) {
		List<Field> ret = new ArrayList<Field>();
		List<Field> fields = getPersistentFields(c);
		for (Field f : fields) {
			if (sFieldUniqueCache.containsKey(f) && sFieldUniqueCache.get(f))
				ret.add(f);
			else {
				Unique u = f.getAnnotation(Unique.class);
				boolean unique = u == null ? false : true;
				sFieldUniqueCache.put(f, unique);
				if (unique)
					ret.add(f);
			}
		}
		return ret;
	}

	/**
	 * Retrieves the name of the database column the specified
	 * <code>Field</code> maps to. The column can be specified using the
	 * {@link Column} annotation by setting the <code>name</code> value. If the
	 * annotation is missing, the column is assumed to be the name of the
	 * <code>Field</code>, sans the lowercase 'm' at the beginning of its name
	 * if Android naming conventions are followed. For example, a
	 * <code>Field</code> named <code>mFoobar</code> would map to the column
	 * <code>foobar</code>.
	 * 
	 * @param f
	 *            the <code>Field</code> to retrieve the column for
	 * @return the name of the column
	 */
	public static String getFieldColumnName(Field f) {
		if (sColumnCache.containsKey(f))
			return sColumnCache.get(f);
		String ret;
		Column c = f.getAnnotation(Column.class);
		if (c == null) {
			String name = f.getName();
			if (name.startsWith("m"))
				ret = name.substring(1).toLowerCase();
			else
				ret = name.toLowerCase();
		} else {
			ret = c.name();
		}
		sColumnCache.put(f, ret);
		return ret;
	}

	/**
	 * Checks if the specified <code>Field's</code> associated column is
	 * nullable.
	 * 
	 * @param f
	 *            the <code>Field</code> to check if nullable
	 * @return <code>true</code> if the field is nullable, <code>false</code> if
	 *         it is not nullable
	 */
	public static boolean isFieldNullable(Field f) {
		if (sFieldNullableCache.containsKey(f))
			return sFieldNullableCache.get(f);
		boolean ret;
		NotNull n = f.getAnnotation(NotNull.class);
		ret = n == null ? true : false;
		sFieldNullableCache.put(f, ret);
		return ret;
	}

	/**
	 * Checks if the specified <code>Field</code> is unique, meaning each record
	 * must have a different value in the table. This is a way of implementing a
	 * unique constraint on a column.
	 * 
	 * @param f
	 * @return
	 */
	public static boolean isFieldUnique(Field f) {
		if (sFieldUniqueCache.containsKey(f))
			return sFieldUniqueCache.get(f);
		boolean ret;
		Unique u = f.getAnnotation(Unique.class);
		ret = u == null ? false : true;
		sFieldUniqueCache.put(f, ret);
		return ret;
	}

	private static Field findPrimaryKeyField(Class<?> c) {
		List<Field> fields = getAllFields(c);
		for (Field f : fields) {
			if (f.getName().equals("mId") || f.getName().equals("mID") || f.getName().equalsIgnoreCase("id"))
				return f;
		}
		return null;
	}

	private static List<Field> getAllFields(Class<?> c) {
		return getAllFieldsRec(c, new LinkedList<Field>());
	}

	private static List<Field> getAllFieldsRec(Class<?> c, List<Field> fields) {
		Class<?> superClass = c.getSuperclass();
		if (superClass != null)
			getAllFieldsRec(superClass, fields);
		fields.addAll(Arrays.asList(c.getDeclaredFields()));
		return fields;
	}

}
