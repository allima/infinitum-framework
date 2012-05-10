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
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.context.ContextFactory;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.internal.Pair;
import com.clarionmedia.infinitum.internal.Preconditions;
import com.clarionmedia.infinitum.internal.Primitives;
import com.clarionmedia.infinitum.logging.Logger;
import com.clarionmedia.infinitum.orm.OrmConstants;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.exception.SQLGrammarException;
import com.clarionmedia.infinitum.orm.persistence.PersistenceResolution;
import com.clarionmedia.infinitum.orm.persistence.TypeResolution;
import com.clarionmedia.infinitum.orm.relationship.ManyToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.ManyToOneRelationship;
import com.clarionmedia.infinitum.orm.relationship.ModelRelationship.RelationType;
import com.clarionmedia.infinitum.orm.relationship.OneToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToOneRelationship;
import com.clarionmedia.infinitum.orm.sql.SqlBuilder;
import com.clarionmedia.infinitum.orm.sqlite.SqliteOperations;
import com.clarionmedia.infinitum.orm.sqlite.SqliteTypeAdapter;
import com.clarionmedia.infinitum.orm.sqlite.SqliteUtil;
import com.clarionmedia.infinitum.reflection.ClassReflector;

/**
 * <p>
 * An implementation of {@link SqliteOperations}. This class is designed to
 * provide implementations of core CRUD operations for interacting with a SQLite
 * database and act as a factory for constructing {@link Criteria} and
 * {@link Criteria} queries.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/11/12
 * 
 */
public class SqliteTemplate implements SqliteOperations {

	private static final String TAG = "SqliteTemplate";

	protected InfinitumContext mInfinitumContext;
	protected SqliteSession mSession;
	protected SqliteDbHelper mDbHelper;
	protected SQLiteDatabase mSqliteDb;
	protected SqliteMapper mMapper;
	protected SqliteModelFactoryImpl mModelFactory;
	protected SqlBuilder mSqlBuilder;
	protected boolean mIsOpen;
	protected Stack<Boolean> mTransactionStack;
	protected boolean mIsAutocommit;
	private Logger mLogger;

	/**
	 * Constructs a new {@code SqliteTemplate} attached to the given
	 * {@link SqliteSession}
	 * 
	 * @param session
	 *            the {@code SqliteSession} this {@code SqliteTemplate} is
	 *            attached to
	 */
	public SqliteTemplate(SqliteSession session) {
		mSession = session;
		mLogger = Logger.getInstance(TAG);
		mInfinitumContext = ContextFactory.getInstance().getContext();
		mIsAutocommit = mInfinitumContext.isAutocommit();
		mMapper = new SqliteMapper();
		mSqlBuilder = new SqliteBuilder(mMapper);
		mTransactionStack = new Stack<Boolean>();
	}

	@Override
	public <T> Criteria<T> createCriteria(Class<T> entityClass) {
		return new SqliteCriteria<T>(mSession, entityClass, mSqlBuilder, mMapper);
	}

	@Override
	public void open() throws SQLException {
		mDbHelper = new SqliteDbHelper(mSession.getContext(), mMapper);
		mSqliteDb = mDbHelper.getWritableDatabase();
		mModelFactory = new SqliteModelFactoryImpl(mSession, mMapper);
		mIsOpen = true;
	}

	@Override
	public void close() {
		mDbHelper.close();
		mIsOpen = false;
	}

	@Override
	public boolean isOpen() {
		return mIsOpen;
	}
	
	@Override
	public void beginTransaction() {
		if (mIsAutocommit)
			return;
		mSqliteDb.beginTransaction();
		mTransactionStack.push(true);
		if (mInfinitumContext.isDebug())
			mLogger.debug("Transaction started");
	}

	@Override
	public void commit() {
		if (!isTransactionOpen())
			return;
		mSqliteDb.setTransactionSuccessful();
		mSqliteDb.endTransaction();
		mTransactionStack.pop();
		if (mInfinitumContext.isDebug())
			mLogger.debug("Transaction committed");
	}

	@Override
	public void rollback() {
		if (!isTransactionOpen())
			return;
		mSqliteDb.endTransaction();
		mTransactionStack.pop();
		if (mInfinitumContext.isDebug())
			mLogger.debug("Transaction rolled back");
	}
	
	@Override
	public boolean isTransactionOpen() {
		return mTransactionStack.size() > 0;
	}
	
	@Override
	public void setAutocommit(boolean autocommit) {
		mIsAutocommit = autocommit;
	}
	
	@Override
	public boolean isAutocommit() {
		return mIsAutocommit;
	}

	@Override
	public long save(Object model) throws InfinitumRuntimeException {
		Preconditions.checkForTransaction(mIsAutocommit, isTransactionOpen());
		Preconditions.checkPersistenceForModify(model);
		Map<Integer, Object> objectMap = new Hashtable<Integer, Object>();
		return saveRec(model, objectMap);
	}

	@Override
	public boolean update(Object model) throws InfinitumRuntimeException {
		Preconditions.checkForTransaction(mIsAutocommit, isTransactionOpen());
		Preconditions.checkPersistenceForModify(model);
		Map<Integer, Object> objectMap = new Hashtable<Integer, Object>();
		return updateRec(model, objectMap);
	}

	@Override
	public boolean delete(Object model) throws InfinitumRuntimeException {
		Preconditions.checkForTransaction(mIsAutocommit, isTransactionOpen());
		Preconditions.checkPersistenceForModify(model);
		String tableName = PersistenceResolution.getModelTableName(model.getClass());
		String whereClause = SqliteUtil.getWhereClause(model, mMapper);
		int result = mSqliteDb.delete(tableName, whereClause, null);
		if (result == 1)
			deleteRelationships(model);
		if (mInfinitumContext.isDebug()) {
			if (result == 1)
				mLogger.debug(model.getClass().getSimpleName() + " model deleted");
			else
				mLogger.debug(model.getClass().getSimpleName() + " model was not deleted");
		}
		return result == 1;
	}

	@Override
	public long saveOrUpdate(Object model) throws InfinitumRuntimeException {
		Preconditions.checkForTransaction(mIsAutocommit, isTransactionOpen());
		Preconditions.checkPersistenceForModify(model);
		Map<Integer, Object> objectMap = new Hashtable<Integer, Object>();
		return saveOrUpdateRec(model, objectMap);
	}

	@Override
	public void saveOrUpdateAll(Collection<? extends Object> models) throws InfinitumRuntimeException {
		Preconditions.checkForTransaction(mIsAutocommit, isTransactionOpen());
		if (mInfinitumContext.isDebug())
			mLogger.debug("Saving or updating " + models.size() + " models");
		for (Object o : models) {
			saveOrUpdate(o);
		}
		if (mInfinitumContext.isDebug())
			mLogger.debug("Models saved or updated");
	}

	@Override
	public int saveAll(Collection<? extends Object> models) throws InfinitumRuntimeException {
		Preconditions.checkForTransaction(mIsAutocommit, isTransactionOpen());
		int count = 0;
		if (mInfinitumContext.isDebug())
			mLogger.debug("Saving " + models.size() + " models");
		for (Object o : models) {
			if (save(o) > 0)
				count++;
		}
		if (mInfinitumContext.isDebug())
			mLogger.debug(count + " models saved");
		return count;
	}

	@Override
	public int deleteAll(Collection<? extends Object> models) throws InfinitumRuntimeException {
		Preconditions.checkForTransaction(mIsAutocommit, isTransactionOpen());
		int count = 0;
		if (mInfinitumContext.isDebug())
			mLogger.debug("Deleting " + models.size() + " models");
		for (Object o : models) {
			if (delete(o))
				count++;
		}
		if (mInfinitumContext.isDebug())
			mLogger.debug(count + " models deleted");
		return count;
	}

	@Override
	public <T> T load(Class<T> c, Serializable id) throws InfinitumRuntimeException, IllegalArgumentException {
		Preconditions.checkPersistenceForLoading(c);
		if (!TypeResolution.isValidPrimaryKey(PersistenceResolution.getPrimaryKeyField(c), id))
			throw new IllegalArgumentException(String.format(OrmConstants.INVALID_PK, id.getClass().getSimpleName(), c.getName()));
		Cursor cursor = mSqliteDb.query(
				PersistenceResolution.getModelTableName(c), null,
				SqliteUtil.getWhereClause(c, id, mMapper), null, null, null,
				null, "1");
		if (cursor.getCount() == 0) {
			cursor.close();
			return null;
		}
		cursor.moveToFirst();
		T ret = null;
		try {
			ret = mModelFactory.createFromCursor(cursor, c);
		} catch (InfinitumRuntimeException e) {
			throw e;
		} finally {
			cursor.close();
		}
		if (mInfinitumContext.isDebug())
			mLogger.debug(c.getSimpleName() + " model loaded");
		return ret;
	}

	@Override
	public void execute(String sql) throws SQLGrammarException {
		Preconditions.checkForTransaction(mIsAutocommit, isTransactionOpen());
		if (mInfinitumContext.isDebug())
			mLogger.debug("Executing SQL: " + sql);
		try {
			mSqliteDb.execSQL(sql);
		} catch (SQLiteException e) {
			throw new SQLGrammarException(String.format(OrmConstants.BAD_SQL, sql));
		}
	}

	@Override
	public Cursor executeForResult(String sql, boolean force) throws SQLGrammarException {
		if (!force)
		    Preconditions.checkForTransaction(mIsAutocommit, isTransactionOpen());
		if (mInfinitumContext.isDebug())
			mLogger.debug("Executing SQL: " + sql);
		Cursor ret = null;
		try {
			ret = mSqliteDb.rawQuery(sql, null);
		} catch (SQLiteException e) {
			throw new SQLGrammarException(String.format(OrmConstants.BAD_SQL, sql));
		}
		return ret;
	}
	
	//private Cursor executeQueryForResult(String sql) {
		
	//}

	@Override
	public <T> void registerTypeAdapter(Class<T> type, SqliteTypeAdapter<T> adapter) {
		mMapper.registerTypeAdapter(type, (SqliteTypeAdapter<T>) adapter);
	}
	
	@Override
	public Map<Class<?>, SqliteTypeAdapter<?>> getRegisteredTypeAdapters() {
		return mMapper.getRegisteredTypeAdapters();
	}

	/**
	 * Returns the {@link SqliteMapper} associated with this
	 * {@code SqliteTemplate}.
	 * 
	 * @return {@code SqliteMapper}
	 */
	public SqliteMapper getSqliteMapper() {
		return mMapper;
	}
	
	/**
	 * Returns the {@link SQLiteDatabase} instance attached to
	 * this {@code SqliteTemplate}.
	 * 
	 * @return {@code SQLiteDatabase} instance
	 */
	public SQLiteDatabase getDatabase() {
		return mSqliteDb;
	}

	private long saveOrUpdateRec(Object model, Map<Integer, Object> objectMap) {
		if (!updateRec(model, objectMap)) {
			objectMap.clear();
			return saveRec(model, objectMap);
		} else
			return 0;
	}

	private long saveRec(Object model, Map<Integer, Object> objectMap) {
		SqliteModelMap map = mMapper.mapModel(model);
		ContentValues values = map.getContentValues();
		String tableName = PersistenceResolution.getModelTableName(model.getClass());
		int objHash = PersistenceResolution.computeModelHash(model);
		if (objectMap.containsKey(objHash) && !PersistenceResolution.isPKNullOrZero(model))
			return 0;
		objectMap.put(objHash, model);
		processOneToOneRelationships(model, map, objectMap, values);
		long ret = mSqliteDb.insert(tableName, null, values);
		if (ret <= 0) {
			if (mInfinitumContext.isDebug())
				mLogger.debug(model.getClass().getSimpleName() + " model was not saved");
			return ret;
		}
		Field f = PersistenceResolution.getPrimaryKeyField(model.getClass());
		f.setAccessible(true);
		try {
			f.set(model, ret);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (ret > 0 && PersistenceResolution.isCascading(model.getClass())) {
			processRelationships(map, objectMap, model);
			if (mInfinitumContext.isDebug())
				mLogger.debug(model.getClass().getSimpleName() + " model saved");
		}
		return ret;
	}

	private boolean updateRec(Object model, Map<Integer, Object> objectMap) {
		SqliteModelMap map = mMapper.mapModel(model);
		ContentValues values = map.getContentValues();
		String tableName = PersistenceResolution.getModelTableName(model.getClass());
		String whereClause = SqliteUtil.getWhereClause(model, mMapper);
		int objHash = PersistenceResolution.computeModelHash(model);
		if (objectMap.containsKey(objHash) && !PersistenceResolution.isPKNullOrZero(model))
			return true;
		objectMap.put(objHash, model);
		if (values.size() == 0)
			return false;
		processOneToOneRelationships(model, map, objectMap, values);
		long ret = mSqliteDb.update(tableName, values, whereClause, null);
		if (ret <= 0) {
			if (mInfinitumContext.isDebug())
				mLogger.debug(model.getClass().getSimpleName() + " model was not updated");
			return false;
		}
		if (PersistenceResolution.isCascading(model.getClass()))
			processRelationships(map, objectMap, model);
		if (mInfinitumContext.isDebug())
			mLogger.debug(model.getClass().getSimpleName() + " model updated");
		return true;
	}

	private void processRelationships(SqliteModelMap map, Map<Integer, Object> objectMap, Object model) {
		processManyToManyRelationships(model, map, objectMap);
		processManyToOneRelationships(model, map, objectMap);
		processOneToManyRelationships(model, map, objectMap);
	}

	private void processManyToManyRelationships(Object model, SqliteModelMap map, Map<Integer, Object> objectMap) {
		for (Pair<ManyToManyRelationship, Iterable<Object>> p : map.getManyToManyRelationships()) {
			ManyToManyRelationship rel = p.getFirst();
			StringBuilder staleQuery = mSqlBuilder.createInitialStaleRelationshipQuery(rel, model);
			String prefix = "";
			for (Object o : p.getSecond()) {
				boolean success;
				int oHash = PersistenceResolution.computeModelHash(o);
				if (objectMap.containsKey(oHash) && !PersistenceResolution.isPKNullOrZero(o)) {
					mSqlBuilder.addPrimaryKeyToQuery(o, staleQuery, prefix);
					prefix = ", ";
					continue;
				}
				success = saveOrUpdateRec(o, objectMap) >= 0;
				if (success) {
					insertManyToManyRelationship(model, o, rel);
					mSqlBuilder.addPrimaryKeyToQuery(o, staleQuery, prefix);
					prefix = ", ";
				}
			}
			staleQuery.append(')');
			mSqliteDb.execSQL(staleQuery.toString());
		}
	}

	private void processOneToOneRelationships(Object model, SqliteModelMap map, Map<Integer, Object> objectMap, ContentValues values) {
		for (Pair<OneToOneRelationship, Object> p : map.getOneToOneRelationships()) {
			Object o = p.getSecond();
			if (ClassReflector.isNull(o))
				continue;
			long id = saveOrUpdateRec(o, objectMap);
			if (id > 0) {
				values.put(PersistenceResolution.getFieldColumnName(
						PersistenceResolution.findRelationshipField(model.getClass(), p.getFirst())), id);
			} else if (id == 0) {
				Object pk = PersistenceResolution.getPrimaryKey(p.getSecond());
				values.put(PersistenceResolution.getFieldColumnName(
						PersistenceResolution.findRelationshipField(model.getClass(), p.getFirst())), (Long) pk);
			}
		}
	}

	private void processOneToManyRelationships(Object model, SqliteModelMap map, Map<Integer, Object> objectMap) {
		for (Pair<OneToManyRelationship, Iterable<Object>> p : map.getOneToManyRelationships()) {
			StringBuilder updateQuery = mSqlBuilder.createInitialUpdateForeignKeyQuery(p.getFirst(), model);
			String prefix = "";
			for (Object o : p.getSecond()) {
				int oHash = PersistenceResolution.computeModelHash(o);
				if (objectMap.containsKey(oHash) && !PersistenceResolution.isPKNullOrZero(o))
					continue;
				saveOrUpdateRec(o, objectMap);
				mSqlBuilder.addPrimaryKeyToQuery(o, updateQuery, prefix);
				prefix = ", ";
			}
			updateQuery.append(')');
			mSqliteDb.execSQL(updateQuery.toString());
		}
	}

	private void processManyToOneRelationships(Object model, SqliteModelMap map, Map<Integer, Object> objectMap) {
		for (Pair<ManyToOneRelationship, Object> p : map.getManyToOneRelationships()) {
			Object o = p.getSecond();
			if (ClassReflector.isNull(o))
				continue;
			saveOrUpdateRec(o, objectMap);
			String update = mSqlBuilder.createUpdateQuery(model, o, p.getFirst().getColumn());
			mSqliteDb.execSQL(update);
		}
	}

	private void insertManyToManyRelationship(Object model, Object related, ManyToManyRelationship mtm) {
		// TODO Revisit this method
		ContentValues relData = new ContentValues();
		Class<?> first = mtm.getFirstType();
		Class<?> second = mtm.getSecondType();
		// TODO Doesn't support reflexive relationships
		try {
			Field f;
			Field s;
			Object fPk;
			Object sPk;
			if (model.getClass() == first) {
				f = PersistenceResolution.findPersistentField(mtm.getFirstType(), mtm.getFirstFieldName());
				s = PersistenceResolution.findPersistentField(mtm.getSecondType(), mtm.getSecondFieldName());
				fPk = f.get(model);
				sPk = s.get(related);
			} else if (model.getClass() == second) {
				s = PersistenceResolution.findPersistentField(mtm.getFirstType(), mtm.getFirstFieldName());
				f = PersistenceResolution.findPersistentField(mtm.getSecondType(), mtm.getSecondFieldName());
				fPk = f.get(related);
				sPk = s.get(model);
			} else {
				// TODO
				throw new InfinitumRuntimeException("");
			}
			String fCol = PersistenceResolution.getModelTableName(first) + '_' + PersistenceResolution.getFieldColumnName(f);
			String sCol = PersistenceResolution.getModelTableName(second) + '_' + PersistenceResolution.getFieldColumnName(s);
			switch (mMapper.getSqliteDataType(f)) {
			case INTEGER:
				if (Primitives.unwrap(f.getType()) == int.class)
					relData.put(fCol, (Integer) fPk);
				else
					relData.put(fCol, (Long) fPk);
				break;
			case TEXT:
				relData.put(fCol, (String) fPk);
				break;
			case REAL:
				if (Primitives.unwrap(f.getType()) == float.class)
					relData.put(fCol, (Float) fPk);
				else
					relData.put(fCol, (Double) fPk);
				break;
			case BLOB:
				relData.put(fCol, (byte[]) fPk);
				break;
			default:
				// TODO
				throw new InfinitumRuntimeException("");
			}
			switch (mMapper.getSqliteDataType(s)) {
			case INTEGER:
				if (Primitives.unwrap(s.getType()) == int.class)
					relData.put(sCol, (Integer) sPk);
				else
					relData.put(sCol, (Long) sPk);
				break;
			case TEXT:
				relData.put(sCol, (String) sPk);
				break;
			case REAL:
				if (Primitives.unwrap(s.getType()) == float.class)
					relData.put(sCol, (Float) sPk);
				else
					relData.put(sCol, (Double) sPk);
				break;
			case BLOB:
				relData.put(sCol, (byte[]) sPk);
				break;
			default:
				// TODO
				throw new InfinitumRuntimeException("");
			}
			boolean result = false;
			try {
				result = mSqliteDb.insertOrThrow(mtm.getTableName(), null,
						relData) > 0;
			} catch (SQLException e) {
				return;
			}
			if (mInfinitumContext.isDebug()) {
				if (result)
					mLogger.debug(first.getSimpleName() + "-" + second.getSimpleName() + " relationship saved");
				else
					mLogger.error(first.getSimpleName() + "-" + second.getSimpleName() + " relationship was not saved");
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void deleteRelationships(Object model) {
		SqliteModelMap map = mMapper.mapModel(model);
		for (Pair<ManyToManyRelationship, Iterable<Object>> p : map.getManyToManyRelationships()) {
			ManyToManyRelationship rel = p.getFirst();
			if (rel.getRelationType() == RelationType.ManyToMany)
				mSqliteDb.rawQuery(mSqlBuilder.createManyToManyDeleteQuery(model, rel), null);
		}
		// TODO Update non M:M relationships
	}

}
