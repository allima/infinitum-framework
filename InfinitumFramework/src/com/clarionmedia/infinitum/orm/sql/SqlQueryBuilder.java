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

import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.criteria.Criterion;
import com.clarionmedia.infinitum.orm.persistence.PersistenceResolution;

/**
 * <p>
 * {@code SqlQueryBuilder} is used to dynamically construct SQL entity queries.
 * SQL queries are constructed using {@link Criteria} queries, which themselves
 * are composed of {@link Criterion} restrictions.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/17/12
 */
public class SqlQueryBuilder {

	private static final String SELECT_ALL_FROM = "SELECT * FROM ";
	private static final String WHERE = "WHERE";
	private static final String AND = " AND ";

	/**
	 * Generates a SQL query {@link String} from the given {@link Criteria}.
	 * 
	 * @param criteria
	 *            the {@code Criteria} to build the SQL query from
	 * @return SQL query
	 */
	public static String createQuery(Criteria<?> criteria) {
		Class<?> c = criteria.getEntityClass();
		StringBuilder query = new StringBuilder(SELECT_ALL_FROM)
				.append(PersistenceResolution.getModelTableName(c)).append(' ')
				.append(WHERE);
		String prefix = "";
		for (Criterion criterion : criteria.getCriterion()) {
			query.append(prefix);
			prefix = AND;
			query.append(criterion.toSql(criteria));
		}
		return query.toString();
	}

}
