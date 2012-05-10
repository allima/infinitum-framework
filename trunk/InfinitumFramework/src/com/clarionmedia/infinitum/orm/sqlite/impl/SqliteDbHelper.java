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
import android.database.sqlite.SQLiteOpenHelper;
import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.context.ContextFactory;
import com.clarionmedia.infinitum.logging.Logger;
import com.clarionmedia.infinitum.orm.OrmConstants;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.sql.SqlBuilder;

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
	private InfinitumContext mInfinitumContext;
	private SqlBuilder mSqlBuilder;
	private Logger mLogger;

	/**
	 * Constructs a new {@code SqliteDbHelper} with the given {@link Context}
	 * and {@link SqliteMapper}.
	 * 
	 * @param context
	 *            the {@code Context} of the {@code SqliteDbHelper}
	 * @param mapper
	 *            the {@code SqliteMapper} to use for {@link Object} mapping
	 */
	public SqliteDbHelper(Context context, SqliteMapper mapper) {
		super(context, ContextFactory.getInstance().getContext()
				.getSqliteDbName(), null, ContextFactory.getInstance()
				.getContext().getSqliteDbVersion());
		mLogger = Logger.getInstance(TAG);
		mInfinitumContext = ContextFactory.getInstance().getContext();
		mSqlBuilder = new SqliteBuilder(mapper);
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
	 * Returns the encapsulated {@link InfinitumContext}.
	 * 
	 * @return {@code InfinitumContext} instance
	 */
	public InfinitumContext getInfinitumContext() {
		return mInfinitumContext;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		mSqliteDb = db;
		if (!mInfinitumContext.isSchemaGenerated())
			return;
		if (mInfinitumContext.isDebug())
			mLogger.debug("Creating database tables");
		try {
			mSqlBuilder.createTables(this);
		} catch (ModelConfigurationException e) {
			mLogger.error(OrmConstants.CREATE_TABLES_ERROR, e);
		}
		if (mInfinitumContext.isDebug())
			mLogger.debug("Database tables created successfully");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (mInfinitumContext.isDebug())
			mLogger.debug("Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		// TODO: drop tables
		onCreate(db);
	}

}
