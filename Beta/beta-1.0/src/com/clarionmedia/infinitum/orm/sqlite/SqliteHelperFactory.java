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

package com.clarionmedia.infinitum.orm.sqlite;

import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.ModelFactory;
import com.clarionmedia.infinitum.orm.sql.SqlBuilder;
import com.clarionmedia.infinitum.orm.sqlite.impl.SqliteCriteria;
import com.clarionmedia.infinitum.orm.sqlite.impl.SqliteDbHelper;
import com.clarionmedia.infinitum.orm.sqlite.impl.SqliteMapper;
import com.clarionmedia.infinitum.orm.sqlite.impl.SqliteSession;

/**
 * <p>
 * Defines factory methods for SQLite component dependencies. This interface and
 * its implementations are intended to be used internally by the framework.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 07/25/12
 * @since 1.0
 */
public interface SqliteHelperFactory {

	/**
	 * Constructs a new {@code SqliteCriteria}.
	 * 
	 * @param session
	 *            the {@link SqliteSession} this {@code SqliteCriteria} is
	 *            attached to
	 * @param entityClass
	 *            the {@code Class} to create {@code SqliteCriteria} for
	 * @param sqlBuilder
	 *            {@link SqlBuilder} for generating SQL statements
	 * @param mapper
	 *            the {@link SqliteMapper} to use for {@link Object} mapping
	 * @throws InfinitumRuntimeException
	 *             if {@code entityClass} is transient
	 */
	<T> SqliteCriteria<T> createCriteria(SqliteSession session,
			Class<T> entityClass, SqlBuilder sqlBuilder, SqliteMapper mapper)
			throws InfinitumRuntimeException;

	/**
	 * Creates a new {@link SqliteDbHelper}.
	 * 
	 * @param context
	 *            the {@link InfinitumContext} to create the
	 *            {@code SqliteDbHelper} with
	 * @param mapper
	 *            the {@link SqliteMapper} to create the {@code SqliteDbHelper}
	 *            with
	 * @return {@code SqliteDbHelper}
	 */
	SqliteDbHelper createSqliteDbHelper(InfinitumContext context,
			SqliteMapper mapper);

	/**
	 * Creates a new {@link ModelFactory}.
	 * 
	 * @param session
	 *            the {@link SqliteSession} this {@code SqliteCriteria} is
	 *            attached to
	 * @param mapper
	 *            the {@link SqliteMapper} to use for {@link Object} mapping
	 * @return {@code SqliteModelFactory}
	 */
	ModelFactory createSqliteModelFactory(SqliteSession session,
			SqliteMapper mapper);

}
