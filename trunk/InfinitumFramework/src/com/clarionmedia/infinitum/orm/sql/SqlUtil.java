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

package com.clarionmedia.infinitum.orm.sql;

import java.lang.reflect.Field;

import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.Constants;
import com.clarionmedia.infinitum.orm.persistence.PersistenceResolution;
import com.clarionmedia.infinitum.orm.persistence.TypeResolution;
import com.clarionmedia.infinitum.orm.persistence.TypeResolution.SqliteDataType;

/**
 * This class contains utility methods for generating SQL strings.
 * 
 * @author Tyler Treat
 * @version 1.0 02/15/12
 */
public class SqlUtil {

	/**
	 * Generates a "where clause" {@link String} used for updating or deleting
	 * the given persistent {@link Object} in the database. Where conditions are
	 * indicated by primary key {@link Field}'s. Note that the actual
	 * {@code String} "where" is not included with the resulting output.
	 * 
	 * <p>
	 * For example, passing an object of class {@code Foobar} which has a
	 * primary key {@code foo} with a value of {@code 42} will result in the
	 * where clause {@code foo = 42}.
	 * </p>
	 * 
	 * @param model
	 *            the model to generate the where clause for
	 * @return where clause {@code String} for specified {@code Object}
	 * @throws InfinitumRuntimeException
	 *             if there is an error generating the SQL
	 */
	public static String getUpdateWhereClause(Object model) throws InfinitumRuntimeException {
		Field pk = PersistenceResolution.getPrimaryKeyField(model.getClass());
		StringBuilder sb = new StringBuilder();
		pk.setAccessible(true);
		sb.append(PersistenceResolution.getFieldColumnName(pk)).append(" = ");
		SqliteDataType t = TypeResolution.getSqliteDataType(pk);
		try {
			if (t == SqliteDataType.TEXT)
				sb.append("'").append(pk.get(model)).append("'");
			else
				sb.append(pk.get(model));
		} catch (IllegalAccessException e) {
			throw new InfinitumRuntimeException(
					String.format(Constants.UNABLE_TO_GEN_QUERY, model.getClass().getName()));
		}
		return sb.toString();
	}

}
