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
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import com.clarionmedia.infinitum.aop.AopProxy;
import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.internal.Pair;
import com.clarionmedia.infinitum.internal.Preconditions;
import com.clarionmedia.infinitum.internal.Primitives;
import com.clarionmedia.infinitum.internal.PropertyLoader;
import com.clarionmedia.infinitum.logging.Logger;
import com.clarionmedia.infinitum.orm.ModelFactory;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.exception.SQLGrammarException;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy.Cascade;
import com.clarionmedia.infinitum.orm.persistence.TypeResolutionPolicy;
import com.clarionmedia.infinitum.orm.persistence.impl.DefaultTypeResolutionPolicy;
import com.clarionmedia.infinitum.orm.relationship.ManyToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.ManyToOneRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToOneRelationship;
import com.clarionmedia.infinitum.orm.sql.SqlBuilder;
import com.clarionmedia.infinitum.orm.sqlite.SqliteHelperFactory;
import com.clarionmedia.infinitum.orm.sqlite.SqliteOperations;
import com.clarionmedia.infinitum.orm.sqlite.SqliteTypeAdapter;
import com.clarionmedia.infinitum.orm.sqlite.SqliteUtil;
import com.clarionmedia.infinitum.reflection.ClassReflector;
import com.clarionmedia.infinitum.reflection.impl.DefaultClassReflector;

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
 * @since 1.0
 */
public class SqliteTemplate implements SqliteOperations {

	protected InfinitumContext mInfinitumContext;
	protected SqliteSession mSession;
	protected SqliteDbHelper mDbHelper;
	protected SQLiteDatabase mSqliteDb;
	protected SqliteMapper mMapper;
	protected ModelFactory mModelFactory;
	protected SqlBuilder mSqlBuilder;
	protected boolean mIsOpen;
	protected Stack<Boolean> mTransactionStack;
	protected boolean mIsAutocommit;
	protected PersistencePolicy mPersistencePolicy;
	protected TypeResolutionPolicy mTypePolicy;
	protected SqliteUtil mSqliteUtil;
	protected ClassReflector mClassReflector;
	protected Logger mLogger;
	protected PropertyLoader mPropLoader;
	protected SqliteHelperFactory mHelperFactory;
	
	@Deprecated
	public SqliteTemplate() {
	}

	/**
	 * Constructs a new {@code SqliteTemplate} attached to the given
	 * {@link SqliteSession}
	 * 
	 * @param session
	 *            the {@code SqliteSession} this {@code SqliteTemplate} is
	 *            attached to
	 */
	public SqliteTemplate(SqliteSession session) {
		mInfinitumContext = session.getInfinitumContext();
		mSqliteUtil = new SqliteUtil(mInfinitumContext);
		mClassReflector = new DefaultClassReflector();
		mPersistencePolicy = mInfinitumContext.getPersistencePolicy();
		mTypePolicy = new DefaultTypeResolutionPolicy(mInfinitumContext);
		mSession = session;
		mLogger = Logger.getInstance(mInfinitumContext, getClass().getSimpleName());
		mIsAutocommit = mInfinitumContext.isAutocommit();
		mMapper = new SqliteMapper(mInfinitumContext);
		mSqlBuilder = new SqliteBuilder(mInfinitumContext, mMapper);
		mTransactionStack = new Stack<Boolean>();
		mPropLoader = new PropertyLoader(mInfinitumContext.getAndroidContext());
		mHelperFactory = new SqliteHelperFactoryImpl();
	}

	@Override
	public <T> Criteria<T> createCriteria(Class<T> entityClass) {
		return mHelperFactory.createCriteria(mSession, entityClass, mSqlBuilder, mMapper);
	}

	@Override
	public void open() throws SQLException {
		mDbHelper = mHelperFactory.createSqliteDbHelper(mInfinitumContext, mMapper);
		mSqliteDb = mDbHelper.getWritableDatabase();
		mModelFactory = mHelperFactory.createSqliteModelFactory(mSession, mMapper);
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
		mLogger.debug("Transaction started");
	}

	@Override
	public void commit() {
		if (!isTransactionOpen())
			return;
		mSqliteDb.setTransactionSuccessful();
		mSqliteDb.endTransaction();
		mTransactionStack.pop();
		mLogger.debug("Transaction committed");
	}

	@Override
	public void rollback() {
		if (!isTransactionOpen())
			return;
		mSqliteDb.endTransaction();
		mTransactionStack.pop();
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

	@SuppressLint("UseSparseArrays")
	@Override
	public long save(Object model) throws InfinitumRuntimeException {
		Preconditions.checkForTransaction(mIsAutocommit, isTransactionOpen());
		Preconditions.checkPersistenceForModify(model, mPersistencePolicy);
		Map<Integer, Object> objectMap = new HashMap<Integer, Object>();
		long result = saveRec(model, objectMap);
		if (result > 0)
			mLogger.debug(model.getClass().getSimpleName() + " model saved");
		else
			mLogger.debug(model.getClass().getSimpleName() + " model was not saved");
		return result;
	}

	@SuppressLint("UseSparseArrays")
	@Override
	public boolean update(Object model) throws InfinitumRuntimeException {
		Preconditions.checkForTransaction(mIsAutocommit, isTransactionOpen());
		Preconditions.checkPersistenceForModify(model, mPersistencePolicy);
		Map<Integer, Object> objectMap = new HashMap<Integer, Object>();
		boolean result = updateRec(model, objectMap);
		if (result)
			mLogger.debug(model.getClass().getSimpleName() + " model updated");
		else
			mLogger.debug(model.getClass().getSimpleName() + " model was not updated");
		return result;
	}

	@Override
	public boolean delete(Object model) throws InfinitumRuntimeException {
		model = AopProxy.getTarget(model);
		Preconditions.checkForTransaction(mIsAutocommit, isTransactionOpen());
		Preconditions.checkPersistenceForModify(model, mPersistencePolicy);
		String tableName = mPersistencePolicy.getModelTableName(model.getClass());
		String whereClause = mSqliteUtil.getWhereClause(model, mMapper);
		int result = mSqliteDb.delete(tableName, whereClause, null);
		if (result == 1) {
			deleteRelationships(model);
			mLogger.debug(model.getClass().getSimpleName() + " model deleted");
		} else {
			mLogger.debug(model.getClass().getSimpleName() + " model was not deleted");
		}
		return result == 1;
	}

	@SuppressLint("UseSparseArrays")
	@Override
	public long saveOrUpdate(Object model) throws InfinitumRuntimeException {
		Preconditions.checkForTransaction(mIsAutocommit, isTransactionOpen());
		Preconditions.checkPersistenceForModify(model, mPersistencePolicy);
		Map<Integer, Object> objectMap = new HashMap<Integer, Object>();
		return saveOrUpdateRec(model, objectMap);
	}

	@Override
	public <T> T load(Class<T> c, Serializable id) throws InfinitumRuntimeException, IllegalArgumentException {
		Preconditions.checkPersistenceForLoading(c, mPersistencePolicy);
		if (!mTypePolicy.isValidPrimaryKey(mPersistencePolicy.getPrimaryKeyField(c), id))
			throw new IllegalArgumentException(String.format(mPropLoader.getErrorMessage("INVALID_PK"), id.getClass()
					.getSimpleName(), c.getName()));
		Cursor cursor = mSqliteDb.query(mPersistencePolicy.getModelTableName(c), null,
				mSqliteUtil.getWhereClause(c, id, mMapper), null, null, null, null, "1");
		if (cursor.getCount() == 0) {
			cursor.close();
			return null;
		}
		cursor.moveToFirst();
		SqliteResult result = new SqliteResult(cursor);
		T ret = null;
		try {
			ret = mModelFactory.createFromResult(result, c);
		} catch (InfinitumRuntimeException e) {
			throw e;
		} finally {
			result.close();
		}
		mLogger.debug(c.getSimpleName() + " model loaded");
		return ret;
	}

	@Override
	public void execute(String sql) throws SQLGrammarException {
		Preconditions.checkForTransaction(mIsAutocommit, isTransactionOpen());
		mLogger.debug("Executing SQL: " + sql);
		try {
			mSqliteDb.execSQL(sql);
		} catch (SQLiteException e) {
			throw new SQLGrammarException(String.format(mPropLoader.getErrorMessage("BAD_SQL"), sql));
		}
	}

	@Override
	public Cursor executeForResult(String sql, boolean force) throws SQLGrammarException {
		if (!force)
			Preconditions.checkForTransaction(mIsAutocommit, isTransactionOpen());
		mLogger.debug("Executing SQL: " + sql);
		Cursor ret = null;
		try {
			ret = mSqliteDb.rawQuery(sql, null);
		} catch (SQLiteException e) {
			throw new SQLGrammarException(String.format(mPropLoader.getErrorMessage("BAD_SQL"), sql));
		}
		return ret;
	}

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

	private long saveOrUpdateRec(Object model, Map<Integer, Object> objectMap) {
		// First try to update the entity, then try to save it if needed
		return updateRec(model, objectMap) ? 0 : saveRec(model, objectMap);
	}

	private long saveRec(Object model, Map<Integer, Object> objectMap) {
		model = AopProxy.getTarget(model);
		// Check if the entity has already been persisted
		int objHash = mPersistencePolicy.computeModelHash(model);
		if (objectMap.containsKey(objHash) && !mPersistencePolicy.isPKNullOrZero(model))
			return 0;
		// Persist it
		SqliteModelMap map = mMapper.mapModel(model);
		ContentValues values = map.getContentValues();
		String tableName = mPersistencePolicy.getModelTableName(model.getClass());
		long rowId = mSqliteDb.insert(tableName, null, values);
		if (rowId <= 0) {
			// Persist failed
			return rowId;
		}
		// Persist succeeded
		setPrimaryKey(model, rowId);
		objHash = mPersistencePolicy.computeModelHash(model);
		objectMap.put(objHash, model);
		processRelationships(map, objectMap, model, mPersistencePolicy.getCascadeMode(model.getClass()));
		return rowId;
	}

	private boolean updateRec(Object model, Map<Integer, Object> objectMap) {
		model = AopProxy.getTarget(model);
		int objHash = mPersistencePolicy.computeModelHash(model);
		if (objectMap.containsKey(objHash) && !mPersistencePolicy.isPKNullOrZero(model))
			return true;
		SqliteModelMap map = mMapper.mapModel(model);
		ContentValues values = map.getContentValues();
		String tableName = mPersistencePolicy.getModelTableName(model.getClass());
		String whereClause = mSqliteUtil.getWhereClause(model, mMapper);
		if (values.size() == 0)
			return false;
		long ret = mSqliteDb.update(tableName, values, whereClause, null);
		if (ret <= 0) {
			return false;
		}
		objectMap.put(objHash, model);
		processRelationships(map, objectMap, model, mPersistencePolicy.getCascadeMode(model.getClass()));
		return true;
	}

	private void processRelationships(SqliteModelMap map, Map<Integer, Object> objectMap, Object model, Cascade cascade) {
		if (cascade == Cascade.None)
			return;
		processManyToManyRelationships(model, map, objectMap, cascade);
		processManyToOneRelationships(model, map, objectMap, cascade);
		processOneToManyRelationships(model, map, objectMap, cascade);
		processOneToOneRelationships(model, map, objectMap, cascade);
	}

	private void processManyToManyRelationships(Object model, SqliteModelMap map, Map<Integer, Object> objectMap, Cascade cascade) {
		for (Pair<ManyToManyRelationship, Iterable<Object>> relationshipPair : map.getManyToManyRelationships()) {
			ManyToManyRelationship relationship = relationshipPair.getFirst();
			StringBuilder staleQuery = mSqlBuilder.createInitialStaleRelationshipQuery(relationship, model);
			String prefix = "";
			for (Object relatedEntity : relationshipPair.getSecond()) {
				if (relatedEntity == null) {
					// Related entity is null, nothing to do here...
					continue;
				}
				int relatedHash = mPersistencePolicy.computeModelHash(relatedEntity);
				if (objectMap.containsKey(relatedHash) && !mPersistencePolicy.isPKNullOrZero(relatedEntity)) {
					mSqlBuilder.addPrimaryKeyToQuery(relatedEntity, staleQuery, prefix);
					prefix = ", ";
					continue;
				}
				// Cascade.All means we persist/update related entities
				if (cascade == Cascade.All) {
				    // Save or update the related entity
				    if (saveOrUpdateRec(relatedEntity, objectMap) >= 0) {
					    // Persist relationship to many-to-many table
					    insertManyToManyRelationship(model, relatedEntity, relationship);
					    mSqlBuilder.addPrimaryKeyToQuery(relatedEntity, staleQuery, prefix);
					    prefix = ", ";
				    }
				// Cascade.Keys means we persist/update foreign keys
				} else if (cascade == Cascade.Keys && !mPersistencePolicy.isPKNullOrZero(relatedEntity)) {
					// Persist relationship to many-to-many table
				    insertManyToManyRelationship(model, relatedEntity, relationship);
				    mSqlBuilder.addPrimaryKeyToQuery(relatedEntity, staleQuery, prefix);
				    prefix = ", ";
				}
			}
			staleQuery.append(')');
			// Delete stale relationships
			if (!staleQuery.toString().contains("NOT IN ()"))
				mSqliteDb.execSQL(staleQuery.toString());
		}
	}

	private void processOneToOneRelationships(Object model, SqliteModelMap map, Map<Integer, Object> objectMap, Cascade cascade) {
		for (Pair<OneToOneRelationship, Object> relationshipPair : map.getOneToOneRelationships()) {
			OneToOneRelationship relationship = relationshipPair.getFirst();
			Object relatedEntity = relationshipPair.getSecond();
			if (mClassReflector.isNull(relatedEntity)) {
				// Related entity is null, nothing to do here...
				continue;
			}
			// Cascade.All means we persist/update related entities
			if (cascade == Cascade.All) {
			    // Save or update the related entity
			    if (saveOrUpdateRec(relatedEntity, objectMap) >= 0 && relationship.getOwner() == model.getClass()) {
				    // Update the relationship owner's foreign key
				    String sql = mSqlBuilder.createUpdateOneToOneForeignKeyQuery(relationship, model, relatedEntity);
				    mSqliteDb.execSQL(sql);
			    }
			// Cascade.Keys means we persist/update foreign keys
			} else if (cascade == Cascade.Keys && !mPersistencePolicy.isPKNullOrZero(relatedEntity)) {
				// Update the relationship owner's foreign key
			    String sql = mSqlBuilder.createUpdateOneToOneForeignKeyQuery(relationship, model, relatedEntity);
			    mSqliteDb.execSQL(sql);
			}
		}
	}

	private void processOneToManyRelationships(Object model, SqliteModelMap map, Map<Integer, Object> objectMap, Cascade cascade) {
		for (Pair<OneToManyRelationship, Iterable<Object>> relationshipPair : map.getOneToManyRelationships()) {
			StringBuilder updateQuery = mSqlBuilder.createInitialUpdateForeignKeyQuery(relationshipPair.getFirst(), model);
			String prefix = "";
			for (Object relatedEntity : relationshipPair.getSecond()) {
				if (relatedEntity == null) {
					// Related entity is null, nothing to do here...
					continue;
				}
				int relatedHash = mPersistencePolicy.computeModelHash(relatedEntity);
				if (objectMap.containsKey(relatedHash) && !mPersistencePolicy.isPKNullOrZero(relatedEntity))
					continue;
				// Cascade.All means we persist/update related entities
				if (cascade == Cascade.All) {
				    // Save or update the related entity
				    if (saveOrUpdateRec(relatedEntity, objectMap) >= 0) {
					    // Include its foreign key to be updated
				        mSqlBuilder.addPrimaryKeyToQuery(relatedEntity, updateQuery, prefix);
				        prefix = ", ";
				    }
			    // Cascade.Keys means we persist/update foreign keys
				} else if (cascade == Cascade.Keys && !mPersistencePolicy.isPKNullOrZero(relatedEntity)) {
					// Include its foreign key to be updated
			        mSqlBuilder.addPrimaryKeyToQuery(relatedEntity, updateQuery, prefix);
			        prefix = ", ";
				}
			}
			updateQuery.append(')');
			// Update the foreign keys
			mSqliteDb.execSQL(updateQuery.toString());
		}
	}

	private void processManyToOneRelationships(Object model, SqliteModelMap map, Map<Integer, Object> objectMap, Cascade cascade) {
		for (Pair<ManyToOneRelationship, Object> relationshipPair : map.getManyToOneRelationships()) {
			Object relatedEntity = relationshipPair.getSecond();
			if (mClassReflector.isNull(relatedEntity)) {
				// Related entity is null, nothing to do here...
				continue;
			}
			// Cascade.All means we persist/update related entities
			if (cascade == Cascade.All) {
			    // Save or update the related entity
			    if (saveOrUpdateRec(relatedEntity, objectMap) >= 0) {
				    // Update the foreign key
			        String update = mSqlBuilder.createUpdateQuery(model, relatedEntity, relationshipPair.getFirst().getColumn());
			        mSqliteDb.execSQL(update);
			    }
			// Cascade.Keys means we persist/update foreign keys
			} else if (cascade == Cascade.Keys && !mPersistencePolicy.isPKNullOrZero(relatedEntity)) {
				// Update the foreign key
		        String update = mSqlBuilder.createUpdateQuery(model, relatedEntity, relationshipPair.getFirst().getColumn());
		        mSqliteDb.execSQL(update);
			}
		}
	}

	private void insertManyToManyRelationship(Object model, Object related, ManyToManyRelationship mtm) {
		ContentValues relationshipData = new ContentValues();
		Class<?> firstType = mtm.getFirstType();
		Class<?> secondType = mtm.getSecondType();
		// TODO Doesn't support reflexive relationships
		try {
			Field firstField;
			Field secondField;
			Serializable firstPk;
			Serializable secondPk;
			if (model.getClass() == firstType) {
				firstField = mPersistencePolicy.findPersistentField(firstType, mtm.getFirstFieldName());
				secondField = mPersistencePolicy.findPersistentField(secondType, mtm.getSecondFieldName());
				firstPk = (Serializable) mClassReflector.getFieldValue(model, firstField);
				secondPk = (Serializable) mClassReflector.getFieldValue(related, secondField);
			} else if (model.getClass() == secondType) {
				secondField = mPersistencePolicy.findPersistentField(firstType, mtm.getFirstFieldName());
				firstField = mPersistencePolicy.findPersistentField(secondType, mtm.getSecondFieldName());
				firstPk = (Serializable) mClassReflector.getFieldValue(related, firstField);
				secondPk = (Serializable) mClassReflector.getFieldValue(model, secondField);
			} else {
				throw new InfinitumRuntimeException("Invalid many-to-many relationship");
			}
			String firstCol = mPersistencePolicy.getModelTableName(firstType) + '_' + mPersistencePolicy.getFieldColumnName(firstField);
			String secondCol = mPersistencePolicy.getModelTableName(secondType) + '_' + mPersistencePolicy.getFieldColumnName(secondField);
			putRelationalKey(relationshipData, firstCol, firstField, firstPk);
			putRelationalKey(relationshipData, secondCol, secondField, secondPk);
			boolean result = false;
			try {
				result = mSqliteDb.insertOrThrow(mtm.getTableName(), null, relationshipData) > 0;
			} catch (SQLException e) {
				return;
			}
			if (result)
				mLogger.debug(firstType.getSimpleName() + "-" + secondType.getSimpleName() + " relationship saved");
			else
				mLogger.error(firstType.getSimpleName() + "-" + secondType.getSimpleName() + " relationship was not saved");
		} catch (ClassCastException e) {
			throw new ModelConfigurationException("Invalid primary key.", e);
		}
	}

	private void deleteRelationships(Object model) {
		SqliteModelMap map = mMapper.mapModel(model);
		for (Pair<ManyToManyRelationship, Iterable<Object>> relationshipPair : map.getManyToManyRelationships()) {
			ManyToManyRelationship relationship = relationshipPair.getFirst();
			mSqliteDb.execSQL(mSqlBuilder.createManyToManyDeleteQuery(model, relationship), null);
		}
		// TODO Update non M:M relationships?
	}

	private void setPrimaryKey(Object model, long rowId) {
		Field pkField = mPersistencePolicy.getPrimaryKeyField(model.getClass());
		Class<?> pkType = Primitives.unwrap(pkField.getType());
		// The row ID is not a PK if the PK type is not int or long
		if (pkType != int.class && pkType != long.class)
			return;
		mClassReflector.setFieldValue(model, pkField, rowId);
	}
	
	private void putRelationalKey(ContentValues relationshipData, String column, Field field, Serializable value) {
		switch (mMapper.getSqliteDataType(field)) {
			case INTEGER:
				if (Primitives.unwrap(field.getType()) == int.class)
					relationshipData.put(column, (Integer) value);
				else
					relationshipData.put(column, (Long) value);
				break;
			case TEXT:
				relationshipData.put(column, (String) value);
				break;
			case REAL:
				if (Primitives.unwrap(field.getType()) == float.class)
					relationshipData.put(column, (Float) value);
				else
					relationshipData.put(column, (Double) value);
				break;
			case BLOB:
				relationshipData.put(column, (byte[]) value);
				break;
			default:
				throw new InfinitumRuntimeException("Invalid relational key type");
	    }
	}

}
