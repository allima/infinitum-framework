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

package com.clarionmedia.infinitum.orm.persistence.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.persistence.TypeResolution;
import com.clarionmedia.infinitum.orm.relationship.ManyToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.ManyToOneRelationship;
import com.clarionmedia.infinitum.orm.relationship.ModelRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToOneRelationship;
import com.clarionmedia.infinitum.reflection.ClassReflector;

/**
 * <p>
 * This class provides runtime resolution for model persistence through Java
 * annotations. Model fields can be marked as transient or persistent using the
 * {@link Persistence} annotation. If no annotation is provided, the field will
 * be marked as persistent by default.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/12/12
 */
public class AnnotationPersistencePolicy extends PersistencePolicy {

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
	@Override
	public boolean isPersistent(Class<?> c) {
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
	@Override
	public String getModelTableName(Class<?> c) throws IllegalArgumentException {
		if (!isPersistent(c) || !TypeResolution.isDomainModel(c))
			throw new IllegalArgumentException("Class '" + c.getName() + "' is transient.");
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
	@Override
	public List<Field> getPersistentFields(Class<?> c) {
		if (mPersistenceCache.containsKey(c))
			return mPersistenceCache.get(c);
		List<Field> ret = new ArrayList<Field>();
		List<Field> fields = ClassReflector.getAllFields(c);
		for (Field f : fields) {
			if (Modifier.isStatic(f.getModifiers()) || TypeResolution.isDomainProxy(f.getDeclaringClass()))
				continue;
			Persistence persistence = f.getAnnotation(Persistence.class);
			PrimaryKey pk = f.getAnnotation(PrimaryKey.class);
			if ((persistence == null || persistence.value() == PersistenceMode.Persistent) || pk != null)
				ret.add(f);
		}
		mPersistenceCache.put(c, ret);
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
	@Override
	public Field findPersistentField(Class<?> c, String name) {
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
	@Override
	public Field getPrimaryKeyField(Class<?> c) throws ModelConfigurationException {
		if (mPrimaryKeyCache.containsKey(c))
			return mPrimaryKeyCache.get(c);
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
		mPrimaryKeyCache.put(c, ret);
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
	@Override
	public List<Field> getUniqueFields(Class<?> c) {
		List<Field> ret = new ArrayList<Field>();
		List<Field> fields = getPersistentFields(c);
		for (Field f : fields) {
			if (mFieldUniqueCache.containsKey(f) && mFieldUniqueCache.get(f))
				ret.add(f);
			else {
				boolean unique = f.isAnnotationPresent(Unique.class) ? true : false;
				if (f.isAnnotationPresent(OneToOne.class))
					unique = true;
				mFieldUniqueCache.put(f, unique);
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
	@Override
	public String getFieldColumnName(Field f) {
		if (mColumnCache.containsKey(f))
			return mColumnCache.get(f);
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
		mColumnCache.put(f, ret);
		return ret;
	}

	/**
	 * Determines if the given {@link Field} is a primary key.
	 * 
	 * @param f
	 *            the {@code Field} to check
	 * @return {@code true} if it is a primary key, {@code false} if it's not
	 */
	@Override
	public boolean isFieldPrimaryKey(Field f) {
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
	@Override
	public boolean isPrimaryKeyAutoIncrement(Field f) throws InfinitumRuntimeException {
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
	@Override
	public boolean isFieldNullable(Field f) {
		if (mFieldNullableCache.containsKey(f))
			return mFieldNullableCache.get(f);
		boolean ret;
		ret = f.isAnnotationPresent(NotNull.class) ? false : true;
		if (f.isAnnotationPresent(OneToOne.class))
			ret = false;
		mFieldNullableCache.put(f, ret);
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
	@Override
	public boolean isFieldUnique(Field f) {
		if (mFieldUniqueCache.containsKey(f))
			return mFieldUniqueCache.get(f);
		boolean ret;
		Unique u = f.getAnnotation(Unique.class);
		ret = u == null ? false : true;
		mFieldUniqueCache.put(f, ret);
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
	@Override
	public Set<ManyToManyRelationship> getManyToManyRelationships(Class<?> c) {
		Set<ManyToManyRelationship> ret = new HashSet<ManyToManyRelationship>();
		for (ManyToManyRelationship r : mManyToManyCache) {
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
			mManyToManyCache.add(rel);
			ret.add(rel);
		}
		return ret;
	}

	/**
	 * Indicates if the given persistent {@link Class} has cascading enabled.
	 * 
	 * @param c
	 *            the {@code Class} to check for cascading
	 * @return {@code true} if it is cascading, {@code false} if not
	 */
	@Override
	public boolean isCascading(Class<?> c) {
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
	@Override
	public boolean isPKNullOrZero(Object model) {
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
	@Override
	public boolean isRelationship(Field f) {
		return f.isAnnotationPresent(ManyToMany.class) || f.isAnnotationPresent(ManyToOne.class)
				|| f.isAnnotationPresent(OneToMany.class) || f.isAnnotationPresent(OneToOne.class);
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
	@Override
	public ModelRelationship getRelationship(Field f) {
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
	@Override
	public Field findRelationshipField(Class<?> c, ModelRelationship rel) {
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
	@Override
	public boolean isLazy(Class<?> c) {
		if (mLazyLoadingCache.containsKey(c))
			return mLazyLoadingCache.get(c);
		boolean ret;
		if (!c.isAnnotationPresent(Entity.class)) {
			ret = true;
		} else {
			Entity entity = c.getAnnotation(Entity.class);
			ret = entity.lazy();
		}
		mLazyLoadingCache.put(c, ret);
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
	@Override
	public String getRestfulResource(Class<?> c) throws IllegalArgumentException {
		if (!isPersistent(c) || !TypeResolution.isDomainModel(c))
			throw new IllegalArgumentException();
		if (mRestResourceCache.containsKey(c))
			return mRestResourceCache.get(c);
		String ret;
		if (!c.isAnnotationPresent(Entity.class)) {
			ret = c.getSimpleName().toLowerCase();
		} else {
			Entity entity = c.getAnnotation(Entity.class);
			ret = entity.resource();
			if (ret.equals(""))
				ret = c.getSimpleName().toLowerCase();
		}
		mRestResourceCache.put(c, ret);
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
	@Override
	public String getResourceFieldName(Field f) throws IllegalArgumentException {
		if (!isPersistent(f.getDeclaringClass()) || !TypeResolution.isDomainModel(f.getDeclaringClass()))
			throw new IllegalArgumentException();
		if (f.isAnnotationPresent(Persistence.class)) {
			Persistence p = f.getAnnotation(Persistence.class);
			if (p.value() == PersistenceMode.Transient)
				throw new IllegalArgumentException();
		}
		if (mRestFieldCache.containsKey(f))
			return mRestFieldCache.get(f);
		String ret;
		if (!f.isAnnotationPresent(Rest.class)) {
			ret = f.getName().toLowerCase();
			if (ret.startsWith("m"))
				ret = ret.substring(1);
		} else {
			Rest rest = f.getAnnotation(Rest.class);
			ret = rest.value();
		}
		mRestFieldCache.put(f, ret);
		return ret;
	}

}