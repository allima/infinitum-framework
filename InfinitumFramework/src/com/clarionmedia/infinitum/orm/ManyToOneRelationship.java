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
import com.clarionmedia.infinitum.orm.annotation.ManyToOne;
import com.clarionmedia.infinitum.reflection.PackageReflector;

/**
 * <p>
 * This class encapsulates a many-to-one relationship between two models.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/03/12
 */
public class ManyToOneRelationship extends ModelRelationship {
	
	private String mColumn;

	public ManyToOneRelationship(Field f) {
		ManyToOne mto = f.getAnnotation(ManyToOne.class);
		mFirst = f.getDeclaringClass();
		mSecond = PackageReflector.getClass(mto.className());
		setColumn(mto.column());
		mRelationType = RelationType.ManyToOne;
	}

	public String getColumn() {
		return mColumn;
	}

	public void setColumn(String column) {
		mColumn = column;
	}

}
