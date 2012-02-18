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

package com.clarionmedia.infinitum.orm.criteria.criterion;

import java.lang.reflect.Field;

import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.criteria.CriteriaConstants;
import com.clarionmedia.infinitum.orm.exception.InvalidCriteriaException;
import com.clarionmedia.infinitum.orm.persistence.PersistenceResolution;
import com.clarionmedia.infinitum.orm.persistence.TypeResolution;
import com.clarionmedia.infinitum.orm.persistence.TypeResolution.SqliteDataType;
import com.clarionmedia.infinitum.orm.sql.SqlConstants;

/**
 * <p>
 * Represents a binary logical expression {@link Criterion}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/17/12
 */
public class BinaryExpression extends Criterion {

	private static final long serialVersionUID = 1282172886230328002L;

	private Object mValue;
	private String mOperator;
	private boolean mIgnoreCase;

	/**
	 * Constructs a new {@code BinaryExpression} with the given field name,
	 * value, and binary operator.
	 * 
	 * @param fieldName
	 *            the name of the field to check value for
	 * @param value
	 *            the value to check for
	 * @param operator
	 *            the binary operator
	 */
	public BinaryExpression(String fieldName, Object value, String operator) {
		super(fieldName);
		mValue = value;
		mOperator = operator;
	}

	/**
	 * Constructs a new {@code BinaryExpression} with the given {@link Field}
	 * name, value, and binary operator, and ignore case {@code boolean}.
	 * 
	 * @param fieldName
	 *            the name of the field to check value for
	 * @param value
	 *            the value to check for
	 * @param operator
	 *            the binary operator
	 * @param ignoreCase
	 *            indicates if case should be ignored for {@link String} values
	 */
	public BinaryExpression(String fieldName, Object value, String operator, boolean ignoreCase) {
		super(fieldName);
		mFieldName = fieldName;
		mValue = value;
		mOperator = operator;
		mIgnoreCase = ignoreCase;
	}

	@Override
	public String toSql(Criteria<?> criteria) throws InvalidCriteriaException {
		StringBuilder query = new StringBuilder();
		Class<?> c = criteria.getEntityClass();
		Field f = null;
		try {
			f = PersistenceResolution.findPersistentField(c, mFieldName);
			if (f == null)
				throw new InvalidCriteriaException(String.format(CriteriaConstants.INVALID_CRITERIA, c.getName()));
			f.setAccessible(true);
		} catch (SecurityException e) {
			throw new InvalidCriteriaException(String.format(CriteriaConstants.INVALID_CRITERIA, c.getName()));
		}
		String colName = PersistenceResolution.getFieldColumnName(f);
		SqliteDataType sqlType = TypeResolution.getSqliteDataType(f);
		boolean lowerCase = mIgnoreCase && sqlType == SqliteDataType.TEXT;
		if (lowerCase)
			query.append(SqlConstants.LOWER).append('(');
		query.append(colName);
		if (lowerCase)
			query.append(')');
		query.append(' ').append(mOperator).append(' ');
		if (sqlType == SqliteDataType.TEXT)
			query.append("'").append(mValue.toString()).append("'");
		else
			query.append(mValue.toString());
		return query.toString();
	}

}
