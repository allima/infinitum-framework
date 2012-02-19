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

package com.clarionmedia.infinitum.orm.criteria;

import java.util.List;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.criteria.criterion.Criterion;

/**
 * <p>
 * This generic interface represents a query for a particular persistent class.
 * {@code Criteria} queries consist of {@link Criterion}, which act as
 * restrictions on a query. For {@code GenCriteria's} non-generic counterpart,
 * see {@link Criteria}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/17/12
 */
public interface GenCriteria<T> extends CriteriaQuery {

	@Override
	String toSql();

	@Override
	Class<T> getEntityClass();

	@Override
	List<Criterion> getCriterion();

	@Override
	int getLimit();

	@Override
	int getOffset();

	/**
	 * Adds a {@link Criterion} to filter retrieved query results.
	 * 
	 * @param criterion
	 *            the {@code Criterion} to apply to the {@link GenCriteria}
	 *            query
	 * @return this {@code Criteria} to allow for method chaining
	 */
	GenCriteria<T> add(Criterion criterion);

	/**
	 * Limits the number of query results.
	 * 
	 * @param limit
	 *            max number of entities to retrieve
	 * @return this {@code Criteria} to allow for method chaining
	 */
	GenCriteria<T> limit(int limit);

	/**
	 * Offsets the result set by the given amount.
	 * 
	 * @param offset
	 *            amount to offset results
	 * @return this {@code Criteria} to allow for method chaining
	 */
	GenCriteria<T> offset(int offset);

	/**
	 * Retrieves the query results as a {@link List}.
	 * 
	 * @return query results in {@code List} form
	 */
	List<T> toList();

	/**
	 * Retrieves a unique query result for the {@code Criteria} query.
	 * 
	 * @return unique query result or {@code null} if no such result exists
	 * @throws InfinitumRuntimeException
	 *             if there was not a unique result for the query
	 */
	T unique() throws InfinitumRuntimeException;

}
