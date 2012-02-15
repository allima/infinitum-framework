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
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.clarionmedia.infinitum.context.ApplicationContext;
import com.clarionmedia.infinitum.orm.Constants;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.sql.SqlTableBuilder;

/**
 * <p>
 * A helper class to manage database creation and version management. This is an
 * extension of <code>SQLiteOpenHelper</code> that will take care of opening a
 * database, creating it if it does not exist, and upgrading it if necessary.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/12/12
 * 
 */
public class SqliteDbHelper extends SQLiteOpenHelper {

	private static final String TAG = "SqliteDbHelper";

	private SQLiteDatabase mSqliteDb;
	private ApplicationContext mAppContext;

	public SqliteDbHelper(Context context, ApplicationContext appContext) {
		super(context, appContext.getSqliteDbName(), null, appContext.getSqliteDbVersion());
		mAppContext = appContext;
	}

	/**
	 * Returns an instance of the <code>SQLiteDatabase</code>.
	 * 
	 * @return the <code>SQLiteDatabase</code> for this application
	 */
	public SQLiteDatabase getDatabase() {
		return mSqliteDb;
	}

	/**
	 * Returns the encapsulated <code>ApplicationContext</code>.
	 * 
	 * @return <code>ApplicationContext</code> instance
	 */
	public ApplicationContext getApplicationContext() {
		return mAppContext;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		mSqliteDb = db;
		if (mAppContext.isDebug())
			Log.d(TAG, "Creating database tables");
		try {
			SqlTableBuilder.createTables(this);
		} catch (ModelConfigurationException e) {
			Log.e(TAG, Constants.CREATE_TABLES_ERROR, e);
		}
		if (mAppContext.isDebug())
			Log.d(TAG, "Database tables created successfully");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (mAppContext.isDebug())
			Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
					+ ", which will destroy all old data");
		// TODO: drop tables
		onCreate(db);
	}

}
