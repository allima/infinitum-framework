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

import com.clarionmedia.infinitum.context.ContextFactory;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.exception.InvalidCriteriaException;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.sql.SqlConstants;

/**
 * <p>
 * Represents a condition restraining a {@link Field} value to between two
 * values.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/18/12
 */
public class BetweenExpression extends Criterion {

	private static final long serialVersionUID = 1282172886230328002L;

	private Object mLow;
	private Object mHigh;

	/**
	 * Constructs a new {@code BetweenExpression} with the given {@link Field}
	 * name and value range.
	 * 
	 * @param fieldName
	 *            the name of the field to check value for
	 * @param low
	 *            the lower bound
	 * @param high
	 *            the upper bound
	 */
	public BetweenExpression(String fieldName, Object low, Object high) {
		super(fieldName);
		mLow = low;
		mHigh = high;
	}

	@Override
	public String toSql(Criteria<?> criteria) throws InvalidCriteriaException {
		StringBuilder query = new StringBuilder();
		Class<?> c = criteria.getEntityClass();
		Field f = null;
		PersistencePolicy policy = ContextFactory.getInstance().getPersistencePolicy();
		try {
			f = policy.findPersistentField(c, mFieldName);
			if (f == null)
				throw new InvalidCriteriaException(String.format(mPropLoader.getErrorMessage("INVALID_CRITERIA"),
						c.getName()));
			f.setAccessible(true);
		} catch (SecurityException e) {
			throw new InvalidCriteriaException(String.format(mPropLoader.getErrorMessage("INVALID_CRITERIA"),
					c.getName()));
		}
		String colName = policy.getFieldColumnName(f);
		query.append(colName).append(' ').append(SqlConstants.OP_BETWEEN).append(' ');
		if (criteria.getObjectMapper().isTextColumn(f))
			query.append("'").append(mLow.toString()).append("'");
		else
			query.append(mLow.toString());
		query.append(SqlConstants.AND);
		if (criteria.getObjectMapper().isTextColumn(f))
			query.append("'").append(mHigh.toString()).append("'");
		else
			query.append(mHigh.toString());
		return query.toString();
	}

}