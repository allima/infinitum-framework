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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.clarionmedia.infinitum.context.ContextFactory;
import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.context.impl.XmlContextFactory;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.logging.Logger;
import com.clarionmedia.infinitum.orm.Session;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.exception.SQLGrammarException;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.persistence.TypeAdapter;
import com.clarionmedia.infinitum.orm.sqlite.SqliteTypeAdapter;

/**
 * <p>
 * Implementation of {@link Session} for interacting with SQLite.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/15/12
 */
public class SqliteSession implements Session {

	private SqliteTemplate mSqlite;
	private Context mContext;
	private InfinitumContext mInfinitumContext;
	private Map<Integer, Object> mSessionCache;
	private int mCacheSize;
	private PersistencePolicy mPolicy;
	private Logger mLogger;

	/**
	 * Creates a new {@code SqliteSession} with the given {@link Context}.
	 * 
	 * @param context
	 *            the {@code Context} of the {@code Session}
	 */
	public SqliteSession(Context context) {
		mContext = context;
		mLogger = Logger.getInstance(getClass().getSimpleName());
		mInfinitumContext = XmlContextFactory.getInstance().getContext();
		mSqlite = new SqliteTemplate(this);
		mSessionCache = new HashMap<Integer, Object>();
		mCacheSize = DEFAULT_CACHE_SIZE;
		mPolicy = ContextFactory.getInstance().getPersistencePolicy();
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
		mInfinitumContext = ContextFactory.getInstance().getContext();
		mSqlite = new SqliteTemplate(this);
		mSessionCache = new HashMap<Integer, Object>();
		mCacheSize = cacheSize;
	}

	@Override
	public void open() throws SQLException {
		try {
			mSqlite.open();
			mLogger.debug("Session opened");
		} catch (SQLException e) {
			mLogger.error("Session not opened", e);
			throw e;
		}
	}

	@Override
	public void close() {
		mSqlite.close();
		recycleCache();
		mLogger.debug("Session closed");
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
	public void setCacheSize(int cacheSize) {
		mCacheSize = cacheSize;
	}

	@Override
	public int getCacheSize() {
		return mCacheSize;
	}

	@Override
	public <T> Criteria<T> createCriteria(Class<T> entityClass) {
		return mSqlite.createCriteria(entityClass);
	}

	@Override
	public long save(Object model) throws InfinitumRuntimeException {
		reconcileCache();
		int hash = mPolicy.computeModelHash(model);
		// Update session cache
		if (mSessionCache.containsKey(hash))
			mSessionCache.put(hash, model);
		return mSqlite.save(model);
	}

	@Override
	public boolean update(Object model) throws InfinitumRuntimeException {
		int hash = mPolicy.computeModelHash(model);
		// Update session cache
		if (mSessionCache.containsKey(hash))
			mSessionCache.put(hash, model);
		return mSqlite.update(model);
	}

	@Override
	public boolean delete(Object model) throws InfinitumRuntimeException {
		int hash = mPolicy.computeModelHash(model);
		// Remove from session cache
		mSessionCache.remove(hash);
		return mSqlite.delete(model);
	}

	@Override
	public long saveOrUpdate(Object model) throws InfinitumRuntimeException {
		reconcileCache();
		int hash = mPolicy.computeModelHash(model);
		// Update session cache
		if (mSessionCache.containsKey(hash))
			mSessionCache.put(hash, model);
		return mSqlite.saveOrUpdate(model);
	}

	@Override
	public void saveOrUpdateAll(Collection<? extends Object> models) throws InfinitumRuntimeException {
		reconcileCache();
		for (Object model : models) {
			int hash = mPolicy.computeModelHash(model);
			// Update session cache
			if (mSessionCache.containsKey(hash))
				mSessionCache.put(hash, model);
		}
		mSqlite.saveOrUpdateAll(models);
	}

	@Override
	public int saveAll(Collection<? extends Object> models) throws InfinitumRuntimeException {
		reconcileCache();
		for (Object model : models) {
			int hash = mPolicy.computeModelHash(model);
			// Update session cache
			if (mSessionCache.containsKey(hash))
				mSessionCache.put(hash, model);
		}
		return mSqlite.saveAll(models);
	}

	@Override
	public int deleteAll(Collection<? extends Object> models) throws InfinitumRuntimeException {
		for (Object model : models) {
			int hash = mPolicy.computeModelHash(model);
			// Remove from session cache
			if (mSessionCache.containsKey(hash))
				mSessionCache.remove(hash);
		}
		return mSqlite.deleteAll(models);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T load(Class<T> c, Serializable id) throws InfinitumRuntimeException, IllegalArgumentException {
		int hash = mPolicy.computeModelHash(c, id);
		if (mSessionCache.containsKey(hash))
			return (T) mSessionCache.get(hash);
		return mSqlite.load(c, id);
	}

	@Override
	public void execute(String sql) throws SQLGrammarException {
		mSqlite.execute(sql);
	}

	@Override
	public <T> void registerTypeAdapter(Class<T> type, TypeAdapter<T> adapter) {
		if (adapter instanceof SqliteTypeAdapter)
			mSqlite.registerTypeAdapter(type, (SqliteTypeAdapter<T>) adapter);
	}

	@Override
	public Map<Class<?>, ? extends TypeAdapter<?>> getRegisteredTypeAdapters() {
		return mSqlite.getRegisteredTypeAdapters();
	}

	@Override
	public void beginTransaction() {
		mSqlite.beginTransaction();
	}

	@Override
	public void commit() {
		mSqlite.commit();
	}

	@Override
	public void rollback() {
		mSqlite.rollback();
	}

	@Override
	public boolean isTransactionOpen() {
		return mSqlite.isTransactionOpen();
	}

	@Override
	public void setAutocommit(boolean autocommit) {
		mSqlite.setAutocommit(autocommit);
	}

	@Override
	public boolean isAutocommit() {
		return mSqlite.isAutocommit();
	}

	/**
	 * Executes the given SQL query on the database for a result.
	 * 
	 * @param sql
	 *            the SQL query to execute
	 * @param force
	 *            indicates if the query should be executed regardless of
	 *            transaction state
	 * @return {@link Cursor} containing the results of the query
	 * @throws SQLGrammarException
	 *             if the SQL was formatted incorrectly
	 */
	public Cursor executeForResult(String sql, boolean force) throws SQLGrammarException {
		return mSqlite.executeForResult(sql, force);
	}

	/**
	 * Caches the given model identified by the given hash code.
	 * 
	 * @param hash
	 *            the hash code which maps to the model
	 * @param model
	 *            the {@link Object} to cache
	 * @return {@code true} if the model was cached, {@code false} if not
	 */
	public boolean cache(int hash, Object model) {
		if (mSessionCache.size() >= mCacheSize)
			return false;
		mSessionCache.put(hash, model);
		return true;
	}

	/**
	 * Indicates if the session cache contains the given hash code.
	 * 
	 * @param hash
	 *            the hash code to check for
	 * @return {@code true} if the cache contains the hash code, {@code false}
	 *         if not
	 */
	public boolean checkCache(int hash) {
		return mSessionCache.containsKey(hash);
	}

	/**
	 * Returns the model with the given hash code from the session cache.
	 * 
	 * @param hash
	 *            the hash code of the model to retrieve
	 * @return the model {@link Object} identified by the given hash code or
	 *         {@code null} if no such entity exists in the cache
	 */
	public Object searchCache(int hash) {
		return mSessionCache.get(hash);
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
	 * how Infinitum is configured. The cache will be reclaimed if its current
	 * size equals or exceeds the maximum cache size.
	 */
	public void reconcileCache() {
		if (!mInfinitumContext.isCacheRecyclable())
			return;
		if (mSessionCache.size() >= mCacheSize)
			recycleCache();
	}

	/**
	 * Returns the {@link SqliteMapper} associated with this
	 * {@code SqliteSession}.
	 * 
	 * @return {@code SqliteMapper}
	 */
	public SqliteMapper getSqliteMapper() {
		return mSqlite.getSqliteMapper();
	}

	/**
	 * Returns the {@link SQLiteDatabase} associated with this
	 * {@code SqliteSession}.
	 * 
	 * @return {@code SQLiteDatabase}
	 */
	public SQLiteDatabase getDatabase() {
		return mSqlite.getDatabase();
	}

}
