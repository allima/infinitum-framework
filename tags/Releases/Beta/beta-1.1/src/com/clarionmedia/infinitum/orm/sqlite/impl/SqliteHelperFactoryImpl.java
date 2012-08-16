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

package com.clarionmedia.infinitum.orm.sqlite.impl;

import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.ModelFactory;
import com.clarionmedia.infinitum.orm.sql.SqlBuilder;
import com.clarionmedia.infinitum.orm.sqlite.SqliteHelperFactory;

/**
 * <p>
 * Implementation of {@link SqliteHelperFactory}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 07/25/12
 * @since 1.0
 */
public class SqliteHelperFactoryImpl implements SqliteHelperFactory {

	@Override
	public <T> SqliteCriteria<T> createCriteria(SqliteSession session,
			Class<T> entityClass, SqlBuilder sqlBuilder, SqliteMapper mapper)
			throws InfinitumRuntimeException {
		return new SqliteCriteria<T>(session, entityClass, sqlBuilder, mapper);
	}

	@Override
	public SqliteDbHelper createSqliteDbHelper(InfinitumContext context, SqliteMapper mapper) {
		return new SqliteDbHelper(context, mapper);
	}

	@Override
	public ModelFactory createSqliteModelFactory(SqliteSession session,
			SqliteMapper mapper) {
		return new SqliteModelFactory(session, mapper);
	}

}
