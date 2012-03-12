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

import com.clarionmedia.infinitum.orm.annotation.OneToMany;
import com.clarionmedia.infinitum.reflection.PackageReflector;

/**
 * <p>
 * This class encapsulates a one-to-many relationship between two models.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/03/12
 */
public class OneToManyRelationship extends ModelRelationship {
	
	private String mColumn;
	private Class<?> mOneType;
	private Class<?> mManyType;

	public OneToManyRelationship(Field f) {
		OneToMany otm = f.getAnnotation(OneToMany.class);
		mFirst = f.getDeclaringClass();
		setOneType(mFirst);
		mSecond = PackageReflector.getClass(otm.className());
		setManyType(mSecond);
		mRelationType = RelationType.OneToMany;
		mName = otm.name();
		mColumn = otm.column();
	}

	public void setColumn(String mColumn) {
		this.mColumn = mColumn;
	}

	public String getColumn() {
		return mColumn;
	}

	public void setOneType(Class<?> mOneType) {
		this.mOneType = mOneType;
	}

	public Class<?> getOneType() {
		return mOneType;
	}

	public void setManyType(Class<?> mManyType) {
		this.mManyType = mManyType;
	}

	public Class<?> getManyType() {
		return mManyType;
	}

}
