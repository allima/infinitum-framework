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
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.context.InfinitumContextFactory;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.Session;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.criteria.GenCriteria;
import com.clarionmedia.infinitum.orm.exception.SQLGrammarException;
import com.clarionmedia.infinitum.orm.persistence.PersistenceResolution;

/**
 * <p>
 * Implementation of {@link Session} for interacting with SQLite.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/15/12
 */
public class SqliteSession implements Session {

	private static final String TAG = "SqliteSession";
	private static final int DEFAULT_CACHE_SIZE = 500;

	private SqliteTemplate mSqlite;
	private Context mContext;
	private InfinitumContext mInfinitumContext;
	private Map<Integer, Object> mSessionCache;
	private int mCacheSize;

	/**
	 * Creates a new {@code SqliteSession} with the given {@link Context}.
	 * 
	 * @param context
	 *            the {@code Context} of the {@code Session}
	 */
	public SqliteSession(Context context) {
		mContext = context;
		mInfinitumContext = InfinitumContextFactory.getInfinitumContext();
		mSqlite = new SqliteTemplate(this);
		mSessionCache = new HashMap<Integer, Object>();
		mCacheSize = DEFAULT_CACHE_SIZE;
	}

	/**
	 * Creates a new {@code SqliteSession} with the given {@link Context} and
	 * cache size.
	 * 
	 * @param context
	 *            the {@code Context} of the {@code Session}
	 * @param cacheSize
	 *            the maximum number of {@code Objects} the {@code Session}
	 *            cache can store
	 */
	public SqliteSession(Context context, int cacheSize) {
		mContext = context;
		mInfinitumContext = InfinitumContextFactory.getInfinitumContext();
		mSqlite = new SqliteTemplate(this);
		mSessionCache = new HashMap<Integer, Object>();
		mCacheSize = cacheSize;
	}

	@Override
	public void open() throws SQLException {
		try {
			mSqlite.open();
			if (mInfinitumContext.isDebug())
				Log.d(TAG, "Session opened");
		} catch (SQLException e) {
			if (mInfinitumContext.isDebug())
				Log.e(TAG, "Session not opened", e);
			throw e;
		}
	}

	@Override
	public void close() {
		mSqlite.close();
		mSessionCache.clear();
		if (mInfinitumContext.isDebug())
			Log.d(TAG, "Session closed");
	}

	@Override
	public boolean isOpen() {
		return mSqlite.isOpen();
	}

	@Override
	public void recycleCache() {
		mSessionCache.clear();
	}

	@Override
	public void setCacheSize(int mCacheSize) {
		this.mCacheSize = mCacheSize;
	}

	@Override
	public int getCacheSize() {
		return mCacheSize;
	}

	@Override
	public <T> GenCriteria<T> createGenericCriteria(Class<T> entityClass) {
		return mSqlite.createGenericCriteria(entityClass);
	}

	@Override
	public Criteria createCriteria(Class<?> entityClass) {
		return mSqlite.createCriteria(entityClass);
	}

	@Override
	public long save(Object model) throws InfinitumRuntimeException {
		reconcileCache();
		int hash = PersistenceResolution.computeModelHash(model);
		// Update session cache
		if (mSessionCache.containsKey(hash))
			mSessionCache.put(hash, model);
		return mSqlite.save(model);
	}

	@Override
	public boolean update(Object model) throws InfinitumRuntimeException {
		int hash = PersistenceResolution.computeModelHash(model);
		// Update session cache
		if (mSessionCache.containsKey(hash))
			mSessionCache.put(hash, model);
		return mSqlite.update(model);
	}

	@Override
	public boolean delete(Object model) throws InfinitumRuntimeException {
		int hash = PersistenceResolution.computeModelHash(model);
		// Remove from session cache
		if (mSessionCache.containsKey(hash))
			mSessionCache.remove(hash);
		return mSqlite.delete(model);
	}

	@Override
	public long saveOrUpdate(Object model) throws InfinitumRuntimeException {
		reconcileCache();
		int hash = PersistenceResolution.computeModelHash(model);
		// Update session cache
		if (mSessionCache.containsKey(hash))
			mSessionCache.put(hash, model);
		return mSqlite.saveOrUpdate(model);
	}

	@Override
	public void saveOrUpdateAll(Collection<? extends Object> models)
			throws InfinitumRuntimeException {
		reconcileCache();
		for (Object model : models) {
			int hash = PersistenceResolution.computeModelHash(model);
			// Update session cache
			if (mSessionCache.containsKey(hash))
				mSessionCache.put(hash, model);
		}
		mSqlite.saveOrUpdateAll(models);
	}

	@Override
	public int saveAll(Collection<? extends Object> models)
			throws InfinitumRuntimeException {
		reconcileCache();
		for (Object model : models) {
			int hash = PersistenceResolution.computeModelHash(model);
			// Update session cache
			if (mSessionCache.containsKey(hash))
				mSessionCache.put(hash, model);
		}
		return mSqlite.saveAll(models);
	}

	@Override
	public int deleteAll(Collection<? extends Object> models)
			throws InfinitumRuntimeException {
		for (Object model : models) {
			int hash = PersistenceResolution.computeModelHash(model);
			// Remove from session cache
			if (mSessionCache.containsKey(hash))
				mSessionCache.remove(hash);
		}
		return mSqlite.deleteAll(models);
	}

	@Override
	public <T> T load(Class<T> c, Serializable id)
			throws InfinitumRuntimeException, IllegalArgumentException {
		return mSqlite.load(c, id);
	}

	@Override
	public void execute(String sql) throws SQLGrammarException {
		mSqlite.execute(sql);
	}

	/**
	 * Executes the given SQL query on the database for a result.
	 * 
	 * @param sql
	 *            the SQL query to execute
	 * @return {@link Cursor} containing the results of the query
	 * @throws SQLGrammarException
	 *             if the SQL was formatted incorrectly
	 */
	public Cursor executeForResult(String sql) throws SQLGrammarException {
		return mSqlite.executeForResult(sql);
	}

	/**
	 * Returns the {@link Session} cache for this {@code SqliteSession}.
	 * 
	 * @return {@code Session} cache
	 */
	public Map<Integer, Object> getSessionCache() {
		return mSessionCache;
	}

	/**
	 * Returns the {@link Context} for this {@code SqliteSession}.
	 * 
	 * @return {@code Context}
	 */
	public Context getContext() {
		return mContext;
	}

	/**
	 * Recycles the {@code Session} cache if {@code infinitum.cfg.xml} has
	 * {@code recycleCache} set to {@code true}, otherwise has no effect. This
	 * allows the {@code Session} cache to be recycled automatically when
	 * needed. The cache can be manually recycled by calling
	 * {@link SqliteSession#recycleCache()}, which will reclaim it regardless of
	 * how Infinitum is configured.
	 */
	public void reconcileCache() {
		if (!mInfinitumContext.isCacheRecyclable())
			return;
		if (mSessionCache.size() >= mCacheSize)
			recycleCache();
	}

}
