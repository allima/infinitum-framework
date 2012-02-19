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
import com.clarionmedia.infinitum.orm.criteria.criterion.Criterion;

/**
 * <p>
 * This interface represents a query for a particular persistent class. A
 * {@code CriteriaQuery} consists of {@link Criterion}, which act as
 * restrictions on a query. This interface is extended by a generic and
 * non-generic interface to allow for greater flexibility.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/18/12
 */
public interface CriteriaQuery {

	/**
	 * Returns the {@code Criteria} query in SQL form.
	 * 
	 * @return SQL {@link String} for this {@code Criteria}
	 */
	String toSql();

	/**
	 * Returns the {@link Class} associated with this {@code Criteria}.
	 * 
	 * @return {@code Criteria } entity {@code Class}
	 */
	Class<?> getEntityClass();

	/**
	 * Returns the {@link List} of {@link Criterion} for this {@code Criteria}.
	 * 
	 * @return {@code List} of {@code Criterion}
	 */
	List<Criterion> getCriterion();

	/**
	 * Returns the result set limit for this {@code Criteria}.
	 * 
	 * @return result set limit
	 */
	int getLimit();

}
