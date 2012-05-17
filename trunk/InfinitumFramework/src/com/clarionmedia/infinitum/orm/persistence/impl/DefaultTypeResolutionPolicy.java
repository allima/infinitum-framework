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

import java.io.Serializable;
import java.lang.reflect.Field;

import com.clarionmedia.infinitum.context.ContextFactory;
import com.clarionmedia.infinitum.internal.Primitives;
import com.clarionmedia.infinitum.orm.persistence.TypeResolutionPolicy;

/**
 * <p>
 * This class provides runtime resolution of data types for the purpose of
 * persistence in the ORM.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/14/12
 */
public class DefaultTypeResolutionPolicy implements TypeResolutionPolicy {

	@Override
	public boolean isValidPrimaryKey(Field pkField, Serializable id) {
		if (id == null)
			return false;
		Class<?> pkUnwrapped = Primitives.unwrap(pkField.getType());
		Class<?> idUnwrapped = Primitives.unwrap(id.getClass());
		return pkUnwrapped == idUnwrapped;
	}

	@Override
	public boolean isDomainModel(Class<?> c) {
		for (String s : ContextFactory.getInstance().getContext().getDomainModels()) {
			if (c.getName().equalsIgnoreCase(s))
				return true;
		}
		return isDomainProxy(c);
	}

	@Override
	public boolean isDomainProxy(Class<?> c) {
		for (String s : ContextFactory.getInstance().getContext().getDomainModels()) {
			String name = s;
			if (name.contains("."))
				name = name.substring(name.lastIndexOf('.') + 1);
			if (c.getName().equalsIgnoreCase(name + "_Proxy"))
				return true;
		}
		return false;
	}

}
