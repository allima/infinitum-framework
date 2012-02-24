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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.clarionmedia.infinitum.orm.sql.SqlExecutor;

public class SqliteExecutor implements SqlExecutor {

	private SQLiteDatabase mDb;

	public SqliteExecutor(Context context) {
		SqliteDbHelper helper = new SqliteDbHelper(context);
		mDb = helper.getWritableDatabase();
	}

	@Override
	public SqliteResult execute(String sql) {
		return new SqliteResult(mDb.rawQuery(sql, null));
	}

}
