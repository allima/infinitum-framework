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

import com.clarionmedia.infinitum.orm.annotation.ManyToMany;
import com.clarionmedia.infinitum.orm.persistence.PersistenceResolution;
import com.clarionmedia.infinitum.reflection.PackageReflector;

/**
 * <p>
 * This class encapsulates a many-to-many relationship between two models.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/19/12
 */
public class ManyToManyRelationship extends ModelRelationship {

	private String mTableName;
	private String mFirstFieldName;
	private String mSecondFieldName;

	public ManyToManyRelationship(Field f) {
		ManyToMany mtm = f.getAnnotation(ManyToMany.class);
		mTableName = mtm.tableName();
		mFirst = f.getDeclaringClass();
		mSecond = PackageReflector.getClass(mtm.className());
		mFirstFieldName = mtm.keyField();
		mSecondFieldName = mtm.foreignField();
		mRelationType = RelationType.ManyToMany;
	}

	public String getTableName() {
		return mTableName;
	}

	public void setTableName(String tableName) {
		mTableName = tableName;
	}

	public String getFirstFieldName() {
		return mFirstFieldName;
	}

	public Field getFirstField() {
		return PersistenceResolution.findPersistentField(mFirst, mFirstFieldName);
	}

	public void setFirstFieldName(String firstField) {
		mFirstFieldName = firstField;
	}

	public String getSecondFieldName() {
		return mSecondFieldName;
	}

	public Field getSecondField() {
		return PersistenceResolution.findPersistentField(mSecond, mSecondFieldName);
	}

	public void setSecondFieldName(String secondField) {
		mSecondFieldName = secondField;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ManyToManyRelationship))
			return false;
		ManyToManyRelationship o = (ManyToManyRelationship) other;
		return mTableName.equalsIgnoreCase(o.mTableName)
				&& ((mFirst == o.mFirst && mSecond == o.mSecond && mFirstFieldName.equalsIgnoreCase(o.mFirstFieldName) && mSecondFieldName
						.equalsIgnoreCase(o.mSecondFieldName)) || mFirst == o.mSecond && mSecond == o.mFirst
						&& mFirstFieldName.equalsIgnoreCase(o.mSecondFieldName)
						&& mSecondFieldName.equalsIgnoreCase(o.mFirstFieldName));
	}

	@Override
	public int hashCode() {
		int hash = 1;
		final int PRIME = 31;
		hash *= PRIME;
		hash *= PRIME + mTableName.hashCode();
		hash *= PRIME + mFirst.hashCode();
		hash *= PRIME + mSecond.hashCode();
		hash *= PRIME + mFirstFieldName.hashCode();
		hash *= PRIME + mSecondFieldName.hashCode();
		return hash;
	}

}
