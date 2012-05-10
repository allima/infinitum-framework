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
import java.util.List;
import java.util.Set;

import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.relationship.ManyToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.ModelRelationship;

/**
 * <p>
 * This class provides runtime resolution for model persistence through XML map
 * files ({@code imf.xml}). Each persistent entity should have an
 * {@code imf.xml} file associated with it and placed in assets/xml. If an
 * entity has no such file, it is marked as transient.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 05/09/12
 */
public class XmlPersistencePolicy extends PersistencePolicy {

	@Override
	public boolean isPersistent(Class<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getModelTableName(Class<?> c) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Field> getPersistentFields(Class<?> c) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field findPersistentField(Class<?> c, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field getPrimaryKeyField(Class<?> c) throws ModelConfigurationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Field> getUniqueFields(Class<?> c) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFieldColumnName(Field f) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFieldPrimaryKey(Field f) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPrimaryKeyAutoIncrement(Field f) throws InfinitumRuntimeException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFieldNullable(Field f) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFieldUnique(Field f) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<ManyToManyRelationship> getManyToManyRelationships(Class<?> c) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCascading(Class<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPKNullOrZero(Object model) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRelationship(Field f) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ModelRelationship getRelationship(Field f) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field findRelationshipField(Class<?> c, ModelRelationship rel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLazy(Class<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getRestfulResource(Class<?> c) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getResourceFieldName(Field f) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

}
