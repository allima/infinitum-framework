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

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.clarionmedia.infinitum.context.ApplicationContext;
import com.clarionmedia.infinitum.context.ApplicationContextFactory;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.Constants;
import com.clarionmedia.infinitum.orm.persistence.ObjectMapper;
import com.clarionmedia.infinitum.orm.persistence.PersistenceResolution;

/**
 * <p>
 * An implementation of {@link SqliteOperations}. This class is designed to
 * provide implementations of core CRUD operations for interacting with a SQLite
 * database.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/11/12
 * 
 */
public class SqliteTemplate implements SqliteOperations {

	protected Context mContext;
	protected ApplicationContext mAppContext;
	protected SqliteDbHelper mDbHelper;
	protected SQLiteDatabase mSqliteDb;
	protected ObjectMapper mObjectMapper;

	/**
	 * Constructs a new <code>AbstractSqliteDao</code> using the given
	 * <code>Context</code>.
	 * 
	 * @param context
	 *            the calling <code>Context</code>
	 */
	public SqliteTemplate(Context context) {
		mContext = context;
		mAppContext = ApplicationContextFactory.getApplicationContext();
		mObjectMapper = new ObjectMapper();
	}

	@Override
	public SqliteOperations open() throws SQLException {
		mDbHelper = new SqliteDbHelper(mContext, mAppContext);
		mSqliteDb = mDbHelper.getWritableDatabase();
		return this;
	}

	@Override
	public void close() {
		mDbHelper.close();
	}

	@Override
	public long save(Object model) throws InfinitumRuntimeException {
		ContentValues values = mObjectMapper.mapModel(model);
		String tableName;
		try {
			tableName = PersistenceResolution.getModelTableName(model.getClass());
		} catch (IllegalArgumentException e) {
			throw new InfinitumRuntimeException(String.format(Constants.CANNOT_SAVE_TRANSIENT, model.getClass()
					.getName()));
		}
		return mSqliteDb.insert(tableName, null, values);
	}

	@Override
	public long update(Object model) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean delete(Object model) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long saveOrUpdate(Object model) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void saveOrUpdateAll(Iterable<? extends Object> models) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveAll(Iterable<? extends Object> models) {
		// TODO Auto-generated method stub

	}

	@Override
	public int deleteAll(Iterable<? extends Object> models) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void execute(String sql) {
		// TODO Auto-generated method stub

	}

}
