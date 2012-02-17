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

import java.io.Serializable;
import java.util.Collection;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.clarionmedia.infinitum.context.ApplicationContext;
import com.clarionmedia.infinitum.context.ApplicationContextFactory;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.Constants;
import com.clarionmedia.infinitum.orm.persistence.ObjectMapper;
import com.clarionmedia.infinitum.orm.persistence.PersistenceResolution;
import com.clarionmedia.infinitum.orm.persistence.TypeResolution;
import com.clarionmedia.infinitum.orm.sql.SqlUtil;
import com.clarionmedia.infinitum.reflection.ModelFactory;

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

	private static final String TAG = "SqliteTemplate";

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
		if (!PersistenceResolution.isPersistent(model.getClass()))
			throw new InfinitumRuntimeException(String.format(Constants.CANNOT_SAVE_TRANSIENT, model.getClass()
					.getName()));
		ContentValues values = mObjectMapper.mapModel(model);
		String tableName = PersistenceResolution.getModelTableName(model.getClass());
		long ret = mSqliteDb.insert(tableName, null, values);
		if (mAppContext.isDebug())
			Log.d(TAG, model.getClass().getSimpleName() + " model saved");
		return ret;
	}

	@Override
	public boolean update(Object model) throws InfinitumRuntimeException {
		if (!PersistenceResolution.isPersistent(model.getClass()))
			throw new InfinitumRuntimeException(String.format(Constants.CANNOT_UPDATE_TRANSIENT, model.getClass()
					.getName()));
		ContentValues values = mObjectMapper.mapModel(model);
		String tableName = PersistenceResolution.getModelTableName(model.getClass());
		String whereClause = SqlUtil.getWhereClause(model);
		long ret = mSqliteDb.update(tableName, values, whereClause, null);
		if (mAppContext.isDebug()) {
			if (ret > 0)
				Log.d(TAG, model.getClass().getSimpleName() + " model updated");
			else
				Log.d(TAG, model.getClass().getSimpleName() + " model was not updated");
		}
		return ret > 0;
	}

	@Override
	public boolean delete(Object model) throws InfinitumRuntimeException {
		if (!PersistenceResolution.isPersistent(model.getClass()))
			throw new InfinitumRuntimeException(String.format(Constants.CANNOT_UPDATE_TRANSIENT, model.getClass()
					.getName()));
		String tableName = PersistenceResolution.getModelTableName(model.getClass());
		String whereClause = SqlUtil.getWhereClause(model);
		int result = mSqliteDb.delete(tableName, whereClause, null);
		if (mAppContext.isDebug()) {
			if (result == 1)
				Log.d(TAG, model.getClass().getSimpleName() + " model deleted");
			else
				Log.d(TAG, model.getClass().getSimpleName() + " model was not deleted");
		}
		return result == 1;
	}

	@Override
	public long saveOrUpdate(Object model) throws InfinitumRuntimeException {
		if (!update(model))
			return save(model);
		else
			return 0;
	}

	@Override
	public void saveOrUpdateAll(Collection<? extends Object> models) throws InfinitumRuntimeException {
		if (mAppContext.isDebug())
			Log.d(TAG, "Saving or updating " + models.size() + " models");
		for (Object o : models)
			saveOrUpdate(o);
		if (mAppContext.isDebug())
			Log.d(TAG, "Models saved or updated");
	}

	@Override
	public int saveAll(Collection<? extends Object> models) throws InfinitumRuntimeException {
		int count = 0;
		if (mAppContext.isDebug())
			Log.d(TAG, "Saving " + models.size() + " models");
		for (Object o : models) {
			if (save(o) > 0)
				count++;
		}
		if (mAppContext.isDebug())
			Log.d(TAG, count + " models saved");
		return count;
	}

	@Override
	public int deleteAll(Collection<? extends Object> models) throws InfinitumRuntimeException {
		int count = 0;
		if (mAppContext.isDebug())
			Log.d(TAG, "Deleting " + models.size() + " models");
		for (Object o : models) {
			if (delete(o))
				count++;
		}
		if (mAppContext.isDebug())
			Log.d(TAG, count + " models deleted");
		return count;
	}

	@Override
	public <T> T load(Class<T> c, Serializable id) throws InfinitumRuntimeException, IllegalArgumentException {
		if (!PersistenceResolution.isPersistent(c))
			throw new InfinitumRuntimeException(String.format(Constants.CANNOT_LOAD_TRANSIENT, c.getName()));
		if (!TypeResolution.isValidPrimaryKey(PersistenceResolution.getPrimaryKeyField(c), id))
			throw new IllegalArgumentException(String.format(Constants.INVALID_PK, id.getClass().getSimpleName(),
					c.getName()));
		Cursor cursor = mSqliteDb.query(PersistenceResolution.getModelTableName(c), null,
				SqlUtil.getWhereClause(c, id), null, null, null, null, "1");
		if (cursor.getCount() == 0) {
			cursor.close();
			return null;
		}
		cursor.moveToFirst();
		T ret = null;
		try {
			ret = ModelFactory.createFromCursor(cursor, c);
		} catch (InfinitumRuntimeException e) {
			throw e;
		} finally {
			cursor.close();
		}
		return ret;
	}

	@Override
	public void execute(String sql) {
		if (mAppContext.isDebug())
			Log.d(TAG, "Executing SQL: " + sql);
		mSqliteDb.execSQL(sql);
	}

}
