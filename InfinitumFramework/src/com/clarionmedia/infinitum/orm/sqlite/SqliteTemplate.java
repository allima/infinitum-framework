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
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.context.InfinitumContextFactory;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.internal.Pair;
import com.clarionmedia.infinitum.internal.Primitives;
import com.clarionmedia.infinitum.orm.OrmConstants;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.criteria.GenCriteria;
import com.clarionmedia.infinitum.orm.exception.SQLGrammarException;
import com.clarionmedia.infinitum.orm.persistence.PersistenceResolution;
import com.clarionmedia.infinitum.orm.persistence.TypeResolution;
import com.clarionmedia.infinitum.orm.relationship.ManyToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.ManyToOneRelationship;
import com.clarionmedia.infinitum.orm.relationship.ModelRelationship;
import com.clarionmedia.infinitum.orm.relationship.ModelRelationship.RelationType;
import com.clarionmedia.infinitum.orm.relationship.OneToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToOneRelationship;
import com.clarionmedia.infinitum.orm.sql.SqlBuilder;
import com.clarionmedia.infinitum.orm.sql.SqlUtil;
import com.clarionmedia.infinitum.reflection.ClassReflector;

/**
 * <p>
 * An implementation of {@link SqliteOperations}. This class is designed to
 * provide implementations of core CRUD operations for interacting with a SQLite
 * database and act as a factory for constructing {@link Criteria} and
 * {@link GenCriteria} queries.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/11/12
 * 
 */
public class SqliteTemplate implements SqliteOperations {

	private static final String TAG = "SqliteTemplate";

	protected InfinitumContext mAppContext;
	protected SqliteSession mSession;
	protected SqliteDbHelper mDbHelper;
	protected SQLiteDatabase mSqliteDb;
	protected SqliteMapper mObjectMapper;
	protected SqliteModelFactoryImpl mModelFactory;
	protected SqlBuilder mSqlBuilder;
	protected boolean mIsOpen;

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
		mAppContext = InfinitumContextFactory.getInfinitumContext();
		mObjectMapper = new SqliteMapper();
		mModelFactory = new SqliteModelFactoryImpl(mSession);
		mSqlBuilder = new SqliteBuilder();
	}

	@Override
	public <T> GenCriteria<T> createGenericCriteria(Class<T> entityClass) {
		return new SqliteGenCriteria<T>(mSession, entityClass, mSqlBuilder);
	}

	@Override
	public Criteria createCriteria(Class<?> entityClass) {
		return new SqliteCriteria(mSession, entityClass, mSqlBuilder);
	}

	@Override
	public void open() throws SQLException {
		mDbHelper = new SqliteDbHelper(mSession.getContext());
		mSqliteDb = mDbHelper.getWritableDatabase();
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
	public long save(Object model) throws InfinitumRuntimeException {
		if (!PersistenceResolution.isPersistent(model.getClass()))
			throw new InfinitumRuntimeException(String.format(
					OrmConstants.CANNOT_SAVE_TRANSIENT, model.getClass()
							.getName()));
		Map<Integer, Object> objectMap = new Hashtable<Integer, Object>();
		return saveRec(model, objectMap);
	}

	@Override
	public boolean update(Object model) throws InfinitumRuntimeException {
		if (!PersistenceResolution.isPersistent(model.getClass()))
			throw new InfinitumRuntimeException(String.format(
					OrmConstants.CANNOT_UPDATE_TRANSIENT, model.getClass()
							.getName()));
		Map<Integer, Object> objectMap = new Hashtable<Integer, Object>();
		return updateRec(model, objectMap);
	}

	@Override
	public boolean delete(Object model) throws InfinitumRuntimeException {
		if (!PersistenceResolution.isPersistent(model.getClass()))
			throw new InfinitumRuntimeException(String.format(
					OrmConstants.CANNOT_UPDATE_TRANSIENT, model.getClass()
							.getName()));
		String tableName = PersistenceResolution.getModelTableName(model
				.getClass());
		String whereClause = SqlUtil.getWhereClause(model);
		int result = mSqliteDb.delete(tableName, whereClause, null);
		if (result == 1)
			deleteRelationships(model);
		if (mAppContext.isDebug()) {
			if (result == 1)
				Log.d(TAG, model.getClass().getSimpleName() + " model deleted");
			else
				Log.d(TAG, model.getClass().getSimpleName()
						+ " model was not deleted");
		}
		return result == 1;
	}

	@Override
	public long saveOrUpdate(Object model) throws InfinitumRuntimeException {
		Map<Integer, Object> objectMap = new Hashtable<Integer, Object>();
		return saveOrUpdateRec(model, objectMap);
	}

	@Override
	public void saveOrUpdateAll(Collection<? extends Object> models)
			throws InfinitumRuntimeException {
		if (mAppContext.isDebug())
			Log.d(TAG, "Saving or updating " + models.size() + " models");
		for (Object o : models)
			saveOrUpdate(o);
		if (mAppContext.isDebug())
			Log.d(TAG, "Models saved or updated");
	}

	@Override
	public int saveAll(Collection<? extends Object> models)
			throws InfinitumRuntimeException {
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
	public int deleteAll(Collection<? extends Object> models)
			throws InfinitumRuntimeException {
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
	public <T> T load(Class<T> c, Serializable id)
			throws InfinitumRuntimeException, IllegalArgumentException {
		if (!PersistenceResolution.isPersistent(c))
			throw new InfinitumRuntimeException(String.format(
					OrmConstants.CANNOT_LOAD_TRANSIENT, c.getName()));
		if (!TypeResolution.isValidPrimaryKey(
				PersistenceResolution.getPrimaryKeyField(c), id))
			throw new IllegalArgumentException(String.format(
					OrmConstants.INVALID_PK, id.getClass().getSimpleName(),
					c.getName()));
		Cursor cursor = mSqliteDb.query(
				PersistenceResolution.getModelTableName(c), null,
				SqlUtil.getWhereClause(c, id), null, null, null, null, "1");
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
		if (mAppContext.isDebug())
			Log.d(TAG, c.getSimpleName() + " model loaded");
		return ret;
	}

	@Override
	public void execute(String sql) throws SQLGrammarException {
		if (mAppContext.isDebug())
			Log.d(TAG, "Executing SQL: " + sql);
		try {
			mSqliteDb.execSQL(sql);
		} catch (SQLiteException e) {
			throw new SQLGrammarException(String.format(OrmConstants.BAD_SQL,
					sql));
		}
	}

	@Override
	public Cursor executeForResult(String sql) throws SQLGrammarException {
		if (mAppContext.isDebug())
			Log.d(TAG, "Executing SQL: " + sql);
		Cursor ret = null;
		try {
			ret = mSqliteDb.rawQuery(sql, null);
		} catch (SQLiteException e) {
			throw new SQLGrammarException(String.format(OrmConstants.BAD_SQL,
					sql));
		}
		return ret;
	}

	private long saveRec(Object model, Map<Integer, Object> objectMap) {
		SqliteModelMap map = mObjectMapper.mapModel(model);
		ContentValues values = map.getContentValues();
		String tableName = PersistenceResolution.getModelTableName(model
				.getClass());
		int objHash = PersistenceResolution.computeModelHash(model);
		if (objectMap.containsKey(objHash)
				&& !PersistenceResolution.isPKNullOrZero(model))
			return 0;
		objectMap.put(objHash, model);
		processOneToOneRelationships(model, map, objectMap, values);
		long ret = mSqliteDb.insert(tableName, null, values);
		if (ret <= 0) {
			if (mAppContext.isDebug())
				Log.d(TAG, model.getClass().getSimpleName()
						+ " model was not saved");
			return ret;
		}
		if (ret > 0 && PersistenceResolution.isCascading(model.getClass())) {
			processRelationships(map, objectMap, model, ret);
			if (mAppContext.isDebug())
				Log.d(TAG, model.getClass().getSimpleName() + " model saved");
		}
		return ret;
	}

	private boolean updateRec(Object model, Map<Integer, Object> objectMap) {
		SqliteModelMap map = mObjectMapper.mapModel(model);
		ContentValues values = map.getContentValues();
		String tableName = PersistenceResolution.getModelTableName(model
				.getClass());
		String whereClause = SqlUtil.getWhereClause(model);
		int objHash = PersistenceResolution.computeModelHash(model);
		if (objectMap.containsKey(objHash)
				&& !PersistenceResolution.isPKNullOrZero(model))
			return true;
		objectMap.put(objHash, model);
		if (values.size() == 0)
			return false;
		processOneToOneRelationships(model, map, objectMap, values);
		long ret = mSqliteDb.update(tableName, values, whereClause, null);
		if (ret <= 0) {
			if (mAppContext.isDebug())
				Log.d(TAG, model.getClass().getSimpleName()
						+ " model was not updated");
			return false;
		}
		if (PersistenceResolution.isCascading(model.getClass()))
			processRelationships(map, objectMap, model);
		if (mAppContext.isDebug())
			Log.d(TAG, model.getClass().getSimpleName() + " model updated");
		return true;
	}

	private long saveOrUpdateRec(Object model, Map<Integer, Object> objectMap) {
		if (!updateRec(model, objectMap)) {
			objectMap.clear();
			return saveRec(model, objectMap);
		} else
			return 0;
	}

	private void processRelationships(SqliteModelMap map,
			Map<Integer, Object> objectMap, Object model, long pk) {
		Field f = PersistenceResolution.getPrimaryKeyField(model.getClass());
		f.setAccessible(true);
		try {
			f.set(model, pk);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Process aggregate relationships
		for (Pair<ModelRelationship, Iterable<Object>> p : map
				.getAggregateRelationships()) {
			ModelRelationship rel = p.getFirst();
			StringBuilder staleQuery = null;
			if (rel.getRelationType() == RelationType.ManyToMany)
				staleQuery = mSqlBuilder.createInitialStaleRelationshipQuery(
						(ManyToManyRelationship) rel, model);
			String prefix = "";
			for (Object o : p.getSecond()) {
				int oHash = PersistenceResolution.computeModelHash(o);
				if (objectMap.containsKey(oHash)
						&& !PersistenceResolution.isPKNullOrZero(o))
					continue;
				long id = saveOrUpdateRec(o, objectMap);
				if (id >= 0) {
					if (id > 0) {
						f = PersistenceResolution.getPrimaryKeyField(o
								.getClass());
						f.setAccessible(true);
						try {
							f.set(o, id);
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (rel.getRelationType() == RelationType.ManyToMany)
						insertManyToManyRelationship(model, o,
								(ManyToManyRelationship) rel);
				}
				if (rel.getRelationType() == RelationType.ManyToMany) {
					mSqlBuilder.addPrimaryKeyToQuery(o, staleQuery, prefix);
					prefix = ", ";
				}
			}
			staleQuery.append(')');
			mSqliteDb.rawQuery(staleQuery.toString(), null);
		}
		processManyToOneRelationships(model, map, objectMap);
		processOneToManyRelationships(model, map, objectMap);
	}

	private void processRelationships(SqliteModelMap map,
			Map<Integer, Object> objectMap, Object model) {
		for (Pair<ModelRelationship, Iterable<Object>> p : map.getAggregateRelationships()) {
			ModelRelationship rel = p.getFirst();
			StringBuilder staleQuery = mSqlBuilder.createInitialStaleRelationshipQuery((ManyToManyRelationship) rel, model);
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
					if (rel.getRelationType() == RelationType.ManyToMany)
						insertManyToManyRelationship(model, o, (ManyToManyRelationship) rel);
				}
				mSqlBuilder.addPrimaryKeyToQuery(o, staleQuery, prefix);
				prefix = ", ";
			}
			staleQuery.append(')');
			mSqliteDb.execSQL(staleQuery.toString());
		}
		processManyToOneRelationships(model, map, objectMap);
		processOneToManyRelationships(model, map, objectMap);
	}

	private void processOneToOneRelationships(Object model, SqliteModelMap map,
			Map<Integer, Object> objectMap, ContentValues values) {
		for (Pair<OneToOneRelationship, Object> p : map
				.getOneToOneRelationships()) {
			Object o = p.getSecond();
			if (ClassReflector.isNull(o))
				continue;
			long id = saveOrUpdateRec(o, objectMap);
			if (id > 0) {
				values.put(PersistenceResolution
						.getFieldColumnName(PersistenceResolution
								.findRelationshipField(model.getClass(),
										p.getFirst())), id);
			} else if (id == 0) {
				Object pk = PersistenceResolution.getPrimaryKey(p.getSecond());
				values.put(PersistenceResolution
						.getFieldColumnName(PersistenceResolution
								.findRelationshipField(model.getClass(),
										p.getFirst())), (Long) pk);
			}
		}
	}

	private void processOneToManyRelationships(Object model,
			SqliteModelMap map, Map<Integer, Object> objectMap) {
		for (Pair<OneToManyRelationship, Iterable<Object>> p : map
				.getOneToManyRelationships()) {
			StringBuilder updateQuery = mSqlBuilder
					.createInitialUpdateForeignKeyQuery(p.getFirst(), model);
			String prefix = "";
			for (Object o : p.getSecond()) {
				int oHash = PersistenceResolution.computeModelHash(o);
				if (objectMap.containsKey(oHash)
						&& !PersistenceResolution.isPKNullOrZero(o))
					continue;
				saveOrUpdateRec(o, objectMap);
				mSqlBuilder.addPrimaryKeyToQuery(o, updateQuery, prefix);
				prefix = ", ";
			}
			updateQuery.append(')');
			mSqliteDb.execSQL(updateQuery.toString());
		}
	}

	private void processManyToOneRelationships(Object model,
			SqliteModelMap map, Map<Integer, Object> objectMap) {
		for (Pair<ManyToOneRelationship, Object> p : map.getManyToOneRelationships()) {
			Object o = p.getSecond();
			if (ClassReflector.isNull(o))
				continue;
			saveOrUpdateRec(o, objectMap);
			String update = mSqlBuilder.createUpdateQuery(model, o, p.getFirst().getColumn());
			mSqliteDb.execSQL(update);
		}
	}

	private void insertManyToManyRelationship(Object model, Object related,
			ManyToManyRelationship mtm) {
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
				f = PersistenceResolution.findPersistentField(
						mtm.getFirstType(), mtm.getFirstFieldName());
				s = PersistenceResolution.findPersistentField(
						mtm.getSecondType(), mtm.getSecondFieldName());
				fPk = f.get(model);
				sPk = s.get(related);
			} else if (model.getClass() == second) {
				s = PersistenceResolution.findPersistentField(
						mtm.getFirstType(), mtm.getFirstFieldName());
				f = PersistenceResolution.findPersistentField(
						mtm.getSecondType(), mtm.getSecondFieldName());
				fPk = f.get(related);
				sPk = s.get(model);
			} else {
				// TODO
				throw new InfinitumRuntimeException("");
			}
			String fCol = PersistenceResolution.getModelTableName(first) + '_'
					+ PersistenceResolution.getFieldColumnName(f);
			String sCol = PersistenceResolution.getModelTableName(second) + '_'
					+ PersistenceResolution.getFieldColumnName(s);
			switch (TypeResolution.getSqliteDataType(f)) {
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
			switch (TypeResolution.getSqliteDataType(s)) {
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
			    result = mSqliteDb.insertOrThrow(mtm.getTableName(), null, relData) > 0;
			} catch (SQLException e) {
				return;
			}
			if (mAppContext.isDebug()) {
				if (result)
					Log.d(TAG, first.getSimpleName() + "-" + second.getSimpleName() + " relationship saved");
				else
					Log.e(TAG, first.getSimpleName() + "-" + second.getSimpleName() + " relationship was not saved");
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
		SqliteModelMap map = mObjectMapper.mapModel(model);
		for (Pair<ModelRelationship, Iterable<Object>> p : map
				.getAggregateRelationships()) {
			ModelRelationship rel = p.getFirst();
			if (rel.getRelationType() == RelationType.ManyToMany)
				mSqliteDb.rawQuery(mSqlBuilder.createManyToManyDeleteQuery(model,
						(ManyToManyRelationship) rel), null);
		}
		// TODO Update non M:M relationships
	}

}