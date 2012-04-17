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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.OrmConstants;
import com.clarionmedia.infinitum.orm.OrmConstants.PersistenceMode;
import com.clarionmedia.infinitum.orm.annotation.Column;
import com.clarionmedia.infinitum.orm.annotation.Entity;
import com.clarionmedia.infinitum.orm.annotation.ManyToMany;
import com.clarionmedia.infinitum.orm.annotation.ManyToOne;
import com.clarionmedia.infinitum.orm.annotation.NotNull;
import com.clarionmedia.infinitum.orm.annotation.OneToMany;
import com.clarionmedia.infinitum.orm.annotation.OneToOne;
import com.clarionmedia.infinitum.orm.annotation.Persistence;
import com.clarionmedia.infinitum.orm.annotation.PrimaryKey;
import com.clarionmedia.infinitum.orm.annotation.Rest;
import com.clarionmedia.infinitum.orm.annotation.Table;
import com.clarionmedia.infinitum.orm.annotation.Unique;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.relationship.ManyToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.ManyToOneRelationship;
import com.clarionmedia.infinitum.orm.relationship.ModelRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToOneRelationship;
import com.clarionmedia.infinitum.reflection.ClassReflector;

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

	// This Map caches the primary key Field for each persistent class
	private static Map<Class<?>, Field> sPrimaryKeyCache;

	// This Map caches the "nullability" of Fields
	private static Map<Field, Boolean> sFieldNullableCache;

	// This Map caches the uniqueness of Fields
	private static Map<Field, Boolean> sFieldUniqueCache;

	// This Set caches the many-to-many relationships
	private static Set<ManyToManyRelationship> sManyToManyCache;

	// This Map caches the lazy-loading status for each persistent class
	private static Map<Class<?>, Boolean> sLazyLoadingCache;

	// This Map caches the resource names for models
	private static Map<Class<?>, String> sRestResourceCache;

	// This Map caches the resource field names for model Fields
	private static Map<Field, String> sRestFieldCache;

	static {
		// Initialize the caches
		sPersistenceCache = new HashMap<Class<?>, List<Field>>();
		sColumnCache = new HashMap<Field, String>();
		sPrimaryKeyCache = new HashMap<Class<?>, Field>();
		sFieldNullableCache = new HashMap<Field, Boolean>();
		sFieldUniqueCache = new HashMap<Field, Boolean>();
		sManyToManyCache = new HashSet<ManyToManyRelationship>();
		sLazyLoadingCache = new HashMap<Class<?>, Boolean>();
		sRestResourceCache = new HashMap<Class<?>, String>();
		sRestFieldCache = new HashMap<Field, String>();
	}

	public static Set<ManyToManyRelationship> getManyToManyCache() {
		return sManyToManyCache;
	}

	/**
	 * Indicates if the given <code>Class</code> is persistent or transient.
	 * Persistence is denoted by the {@link Entity} annotation.
	 * <code>Entity's</code> mode can be set to <code>transient</code> or
	 * <code>persistent</code>. If the mode is missing or if the annotation
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
	 * @throws IllegalArgumentException
	 *             if the given {@code Class} is transient
	 */
	public static String getModelTableName(Class<?> c) throws IllegalArgumentException {
		if (!isPersistent(c) || !TypeResolution.isDomainModel(c))
			throw new IllegalArgumentException();
		String ret;
		Table table = c.getAnnotation(Table.class);
		if (table == null) {
			if (TypeResolution.isDomainProxy(c)) {
				ret = c.getName();
				ret = ret.substring(0, ret.lastIndexOf("_Proxy")).toLowerCase();
			} else {
				ret = c.getSimpleName().toLowerCase();
			}
		} else {
			ret = table.value();
		}
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
			if (Modifier.isStatic(f.getModifiers()) || TypeResolution.isDomainProxy(f.getDeclaringClass()))
				continue;
			Persistence persistence = f.getAnnotation(Persistence.class);
			PrimaryKey pk = f.getAnnotation(PrimaryKey.class);
			if ((persistence == null || persistence.value() == PersistenceMode.Persistent) || pk != null)
				ret.add(f);
		}
		sPersistenceCache.put(c, ret);
		return ret;
	}

	/**
	 * Finds the persistent {@link Field} for the given {@link Class} which has
	 * the specified name. Returns {@code null} if no such {@code Field} exists.
	 * 
	 * @param c
	 *            the {@code Class} containing the {@code Field}
	 * @param name
	 *            the name of the {@code Field} to retrieve
	 * @return {@code Field} with specified name
	 */
	public static Field findPersistentField(Class<?> c, String name) {
		List<Field> fields = getPersistentFields(c);
		for (Field f : fields) {
			if (f.getName().equalsIgnoreCase(name))
				return f;
		}
		return null;
	}

	/**
	 * Retrieves the primary key {@code Field} for the given {@code Class}. A
	 * {@code Field} can be marked as a primary key using the {@link PrimaryKey}
	 * annotation. If the annotation is missing from the class hierarchy,
	 * Infinitum will look for a {@code Field} called {@code mId} or {@code id}
	 * to use as the primary key.
	 * 
	 * @param c
	 *            the {@code Class} to retrieve the primary key {@code Field}
	 *            for
	 * @return the primary key {@code Field} for the specified {@code Class}
	 * @throws ModelConfigurationException
	 *             if multiple primary keys are declared in {@code c}
	 */
	public static Field getPrimaryKeyField(Class<?> c) throws ModelConfigurationException {
		if (sPrimaryKeyCache.containsKey(c))
			return sPrimaryKeyCache.get(c);
		Field ret = null;
		boolean found = false;
		List<Field> fields = getPersistentFields(c);
		for (Field f : fields) {
			PrimaryKey pk = f.getAnnotation(PrimaryKey.class);
			if (pk != null && !found) {
				ret = f;
				found = true;
			} else if (pk != null && found) {
				throw new ModelConfigurationException(String.format(OrmConstants.MULTIPLE_PK_ERROR, c.getName()));
			}
		}
		// Look for id fields if the annotation is missing
		if (ret == null) {
			Field f = findPrimaryKeyField(c);
			if (f != null)
				ret = f;
		}

		if (ret == null)
			return null;
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
				boolean unique = f.isAnnotationPresent(Unique.class) ? true : false;
				if (f.isAnnotationPresent(OneToOne.class))
					unique = true;
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
			if (f.isAnnotationPresent(ManyToOne.class))
				ret = new ManyToOneRelationship(f).getColumn();
			else if (f.isAnnotationPresent(OneToOne.class))
				ret = new OneToOneRelationship(f).getColumn();
		} else {
			ret = c.value();
		}
		sColumnCache.put(f, ret);
		return ret;
	}

	/**
	 * Determines if the given {@link Field} is a primary key.
	 * 
	 * @param f
	 *            the {@code Field} to check
	 * @return {@code true} if it is a primary key, {@code false} if it's not
	 */
	public static boolean isFieldPrimaryKey(Field f) {
		return f.equals(getPrimaryKeyField(f.getDeclaringClass()));
	}

	/**
	 * Determines if the given primary key {@link Field} is set to
	 * autoincrement. This method assumes, as a precondition, that the
	 * {@code Field} being passed is guaranteed to be a primary key, whether
	 * implicitly or explicitly.
	 * 
	 * @param f
	 *            the primary key {@code Field} to check if it's set to
	 *            autoincrement
	 * @return {@code true} if it is set to autoincrement, {@code false} if it's
	 *         not
	 * @throws InfinitumRuntimeException
	 *             if an explicit primary key that is set to autoincrement is
	 *             not of type int or long
	 */
	public static boolean isPrimaryKeyAutoIncrement(Field f) throws InfinitumRuntimeException {
		PrimaryKey pk = f.getAnnotation(PrimaryKey.class);
		if (pk == null) {
			if (f.getType() == int.class || f.getType() == Integer.class || f.getType() == long.class
					|| f.getType() == Long.class)
				return true;
			else
				return false;
		}
		boolean ret = pk.autoincrement();
		if (!ret)
			return false;
		// throw runtime exception if explicit PK is not an int or long
		if (f.getType() == int.class || f.getType() == Integer.class || f.getType() == long.class
				|| f.getType() == Long.class)
			return true;
		else
			throw new InfinitumRuntimeException(String.format(OrmConstants.EXPLICIT_PK_TYPE_ERROR, f.getName(), f
					.getDeclaringClass().getName()));
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
		ret = f.isAnnotationPresent(NotNull.class) ? false : true;
		if (f.isAnnotationPresent(OneToOne.class))
			ret = false;
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

	/**
	 * Retrieves a {@link Set} of all {@link ManyToManyRelationship} instances
	 * for the given {@link Class}.
	 * 
	 * @param c
	 *            the {@code Class} to get relationships for
	 * @return {@code Set} of all many-to-many relationships
	 */
	public static Set<ManyToManyRelationship> getManyToManyRelationships(Class<?> c) {
		Set<ManyToManyRelationship> ret = new HashSet<ManyToManyRelationship>();
		for (ManyToManyRelationship r : sManyToManyCache) {
			if (r.contains(c))
				ret.add(r);
		}
		if (ret.size() > 0)
			return ret;
		List<Field> fields = getPersistentFields(c);
		for (Field f : fields) {
			if (!f.isAnnotationPresent(ManyToMany.class))
				continue;
			ManyToManyRelationship rel = new ManyToManyRelationship(f);
			sManyToManyCache.add(rel);
			ret.add(rel);
		}
		return ret;
	}

	/**
	 * Calculates a hash code for the specified persistent model based on its
	 * {@link Class} and primary key.
	 * 
	 * @param model
	 *            the model entity to compute the hash for
	 * @return hash code for the model
	 */
	public static int computeModelHash(Object model) {
		final int PRIME = 31;
		int hash = 7;
		hash *= PRIME + model.getClass().hashCode();
		Field f = getPrimaryKeyField(model.getClass());
		f.setAccessible(true);
		Object o = null;
		try {
			o = f.get(model);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
		if (o != null)
			hash *= PRIME + o.hashCode();
		return hash;
	}

	/**
	 * Indicates if the given persistent {@link Class} has cascading enabled.
	 * 
	 * @param c
	 *            the {@code Class} to check for cascading
	 * @return {@code true} if it is cascading, {@code false} if not
	 */
	public static boolean isCascading(Class<?> c) {
		if (!c.isAnnotationPresent(Entity.class))
			return true;
		Entity entity = c.getAnnotation(Entity.class);
		return entity.cascade();
	}

	/**
	 * Indicates if the primary key {@link Field} for the given model is 0 or
	 * {@code null}.
	 * 
	 * @param model
	 *            the model to check the primary key value for
	 * @return {@code true} if it is 0 or {@code null}, false if not
	 */
	public static boolean isPKNullOrZero(Object model) {
		Field f = getPrimaryKeyField(model.getClass());
		f.setAccessible(true);
		Object pk = null;
		try {
			pk = f.get(model);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (pk == null)
			return true;
		if (pk instanceof Integer)
			return (((Integer) pk) == 0);
		else if (pk instanceof Long)
			return (((Long) pk) == 0);
		else if (pk instanceof Float)
			return (((Float) pk) == 0);
		else if (pk instanceof Double)
			return (((Double) pk) == 0);
		return false;
	}

	/**
	 * Indicates if the given persistent {@link Field} is part of an entity
	 * relationship, either many-to-many, many-to-one, one-to-many, or
	 * one-to-one.
	 * 
	 * @param f
	 *            the {@code Field} to check
	 * @return {@code true} if it is part of a relationship, {@code false} if
	 *         not
	 */
	public static boolean isRelationship(Field f) {
		return f.isAnnotationPresent(ManyToMany.class) || f.isAnnotationPresent(ManyToOne.class)
				|| f.isAnnotationPresent(OneToMany.class) || f.isAnnotationPresent(OneToOne.class);
	}

	public static boolean isSingularRelationship(Field f) {
		return f.isAnnotationPresent(ManyToOne.class) || f.isAnnotationPresent(OneToOne.class);
	}

	/**
	 * Retrieves the {@link ModelRelationship} the given {@link Field} is a part
	 * of.
	 * 
	 * @param f
	 *            the {@code Field} to retrieve the relationship for
	 * @return the {@code ModelRelationship} for {@code f} or {@code null} if
	 *         there is none
	 */
	public static ModelRelationship getRelationship(Field f) {
		if (f.isAnnotationPresent(ManyToMany.class))
			return new ManyToManyRelationship(f);
		if (f.isAnnotationPresent(ManyToOne.class))
			return new ManyToOneRelationship(f);
		if (f.isAnnotationPresent(OneToMany.class))
			return new OneToManyRelationship(f);
		if (f.isAnnotationPresent(OneToOne.class))
			return new OneToOneRelationship(f);
		return null;
	}

	/**
	 * Retrieves the primary key value for the given persistent model.
	 * 
	 * @param model
	 *            the model to retrieve the primary key for
	 * @return primary key value
	 */
	public static Object getPrimaryKey(Object model) {
		Object ret = null;
		Field pkField = getPrimaryKeyField(model.getClass());
		pkField.setAccessible(true);
		if (TypeResolution.isDomainProxy(model.getClass())) {
			// Need to invoke getter if it's a proxy
			ret = ClassReflector.invokeGetter(pkField, model);
		} else {
			try {
				ret = pkField.get(model);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ret;
	}

	/**
	 * Retrieves the {@link Field} pertaining to the given
	 * {@link ModelRelationship} for the specified {@link Class}. If no such
	 * {@code Field} exists, {@code null} is returned.
	 * 
	 * @param c
	 *            the {@code Class} to retrieve the {@code Field} from
	 * @param rel
	 *            the {@code ModelRelationship} to retrieve the {@code Field}
	 *            for
	 * @return {@code Field} pertaining to the relationship or {@code null}
	 */
	public static Field findRelationshipField(Class<?> c, ModelRelationship rel) {
		for (Field f : getPersistentFields(c)) {
			f.setAccessible(true);
			if (!isRelationship(f))
				continue;
			switch (rel.getRelationType()) {
			case ManyToMany:
				ManyToMany mtm = f.getAnnotation(ManyToMany.class);
				if (rel.getName().equalsIgnoreCase(mtm.name()))
					return f;
				break;
			case ManyToOne:
				ManyToOne mto = f.getAnnotation(ManyToOne.class);
				if (rel.getName().equalsIgnoreCase(mto.name()))
					return f;
				break;
			case OneToMany:
				OneToMany otm = f.getAnnotation(OneToMany.class);
				if (rel.getName().equalsIgnoreCase(otm.name()))
					return f;
				break;
			case OneToOne:
				OneToOne oto = f.getAnnotation(OneToOne.class);
				if (rel.getName().equalsIgnoreCase(oto.name()))
					return f;
			}
		}
		return null;
	}

	/**
	 * Indicates if the given persistent {@link Class} has lazy loading enabled
	 * or not.
	 * 
	 * @param c
	 *            the {@code Class} to check lazy-loading status
	 * @return {@code true} if lazy loading is enabled, {@code false} if not
	 */
	public static boolean isLazy(Class<?> c) {
		if (sLazyLoadingCache.containsKey(c))
			return sLazyLoadingCache.get(c);
		boolean ret;
		if (!c.isAnnotationPresent(Entity.class)) {
			ret = true;
		} else {
			Entity entity = c.getAnnotation(Entity.class);
			ret = entity.lazy();
		}
		sLazyLoadingCache.put(c, ret);
		return ret;
	}

	/**
	 * Retrieves the RESTful resource name for the given persistent
	 * {@link Class}.
	 * 
	 * @param c
	 *            the {@code Class} to retrieve the RESTful resource name for
	 * @return resource name
	 * @throws IllegalArgumentException
	 *             if the given {@code Class} is not a domain model or
	 *             persistent
	 */
	public static String getRestfulResource(Class<?> c) throws IllegalArgumentException {
		if (!isPersistent(c) || !TypeResolution.isDomainModel(c))
			throw new IllegalArgumentException();
		if (sRestResourceCache.containsKey(c))
			return sRestResourceCache.get(c);
		String ret;
		if (!c.isAnnotationPresent(Entity.class)) {
			ret = c.getSimpleName().toLowerCase();
		} else {
			Entity entity = c.getAnnotation(Entity.class);
			ret = entity.resource();
			if (ret.equals(""))
				ret = c.getSimpleName().toLowerCase();
		}
		sRestResourceCache.put(c, ret);
		return ret;
	}

	/**
	 * Retrieves the RESTful resource field name for the given persistent
	 * {@link Field}.
	 * 
	 * @param f
	 *            the {@code Field} to retrieve the resource field name for
	 * @return resource field name
	 * @throws IllegalArgumentException
	 *             if the containing {@link Class} of the given {@code Field} is
	 *             transient or if the {@code Field} itself is marked transient
	 */
	public static String getResourceFieldName(Field f) throws IllegalArgumentException {
		if (!isPersistent(f.getDeclaringClass()) || !TypeResolution.isDomainModel(f.getDeclaringClass()))
			throw new IllegalArgumentException();
		if (f.isAnnotationPresent(Persistence.class)) {
			Persistence p = f.getAnnotation(Persistence.class);
			if (p.value() == PersistenceMode.Transient)
				throw new IllegalArgumentException();
		}
		if (sRestFieldCache.containsKey(f))
			return sRestFieldCache.get(f);
		String ret;
		if (!f.isAnnotationPresent(Rest.class)) {
			ret = f.getName().toLowerCase();
		} else {
			Rest rest = f.getAnnotation(Rest.class);
			ret = rest.value();
		}
		sRestFieldCache.put(f, ret);
		return ret;
	}

	private static Field findPrimaryKeyField(Class<?> c) {
		List<Field> fields = getPersistentFields(c);
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
