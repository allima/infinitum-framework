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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.clarionmedia.infinitum.orm.sql.SqlExecutor;

/**
 * <p>
 * Executes SQL statements against the registered SQLite database for this
 * application.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/24/12
 */
public class SqliteExecutor implements SqlExecutor {

	private SQLiteDatabase mDb;

	/**
	 * Constructs a new {@code SqliteExecutor} with the given {@link Context}
	 * and {@link SqliteMapper}.
	 * 
	 * @param context
	 *            the {@code Context} of the {@code SqliteExecutor}
	 * @param mapper
	 *            the {@code SqliteMapper} to use for {@link Object} mapping
	 */
	public SqliteExecutor(SQLiteDatabase db) {
		mDb = db;
	}

	@Override
	public SqliteResult execute(String sql) {
		return new SqliteResult(mDb.rawQuery(sql, null));
	}

}
