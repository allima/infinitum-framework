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
import java.util.List;

import com.clarionmedia.infinitum.orm.Constants;
import com.clarionmedia.infinitum.orm.annotation.Column;
import com.clarionmedia.infinitum.orm.annotation.Table;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.PersistenceResolution;
import com.clarionmedia.infinitum.orm.persistence.TypeResolution;

/**
 * <p>
 * <code>SqlTableBuilder</code> is used to dynamically construct SQL strings for
 * table generation. It makes use of ORM annotations, such as {@link Table} and
 * {@link Column} to compose statements.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/13/12
 */
public class SqlTableBuilder {

	private static final String CREATE_TABLE = "CREATE TABLE";
	private static final String NOT_NULL = "NOT NULL";
	private static final String PRIMARY_KEY = "PRIMARY KEY";
	private static final String UNIQUE = "UNIQUE";

	/**
	 * Generates the create table SQL statement for the specified
	 * <code>Class</code>. If the <code>Class</code> does not contain any
	 * persistent <code>Fields</code>, a {@link ModelConfigurationException}
	 * will be thrown. If the <code>Class</code> itself is marked as transient,
	 * this method will return null.
	 * 
	 * @param c
	 *            the <code>Class</code> to generate the create table SQL
	 *            statement for
	 * @return create table SQL statement
	 * @throws ModelConfigurationException
	 */
	public static String createTableString(Class<?> c)
			throws ModelConfigurationException {
		if (!PersistenceResolution.isPersistent(c))
			return null;
		StringBuilder sb = new StringBuilder(CREATE_TABLE).append(" ")
				.append(PersistenceResolution.getModelTableName(c))
				.append(" (");
		appendColumns(c, sb);
		appendPrimaryKeys(c, sb);
		appendUniqueColumns(c, sb);
		sb.append(')');
		return sb.toString();
	}

	private static void appendColumns(Class<?> c, StringBuilder sb)
			throws ModelConfigurationException {
		List<Field> fields = PersistenceResolution.getPersistentFields(c);
		if (fields.size() == 0)
			throw new ModelConfigurationException(String.format(
					Constants.NO_PERSISTENT_FIELDS, c.getName()));
		String prefix = "";
		for (Field f : fields) {
			sb.append(prefix);
			prefix = ", ";
			sb.append(PersistenceResolution.getFieldColumnName(f)).append(" ")
					.append(TypeResolution.getSqliteDataType(f).toString());
			if (!PersistenceResolution.isFieldNullable(f))
				sb.append(" ").append(NOT_NULL);
		}
	}

	private static void appendPrimaryKeys(Class<?> c, StringBuilder sb) {
		List<Field> fields = PersistenceResolution.getPrimaryKeyFields(c);
		if (fields.size() > 0) {
			sb.append(", ").append(PRIMARY_KEY).append('(');
			String prefix = "";
			for (Field f : fields) {
				sb.append(prefix);
				prefix = ", ";
				sb.append(PersistenceResolution.getFieldColumnName(f));
			}
			sb.append(')');
		}
	}

	private static void appendUniqueColumns(Class<?> c, StringBuilder sb) {
		List<Field> fields = PersistenceResolution.getUniqueFields(c);
		if (fields.size() > 0) {
			sb.append(", ").append(UNIQUE).append('(');
			String prefix = "";
			for (Field f : fields) {
				sb.append(prefix);
				prefix = ", ";
				sb.append(PersistenceResolution.getFieldColumnName(f));
			}
			sb.append(')');
		}
	}
}
