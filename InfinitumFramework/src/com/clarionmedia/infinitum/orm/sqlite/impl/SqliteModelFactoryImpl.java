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

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import android.content.Context;
import android.database.Cursor;

import com.clarionmedia.infinitum.context.impl.ContextFactory;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.internal.PropertyLoader;
import com.clarionmedia.infinitum.logging.Logger;
import com.clarionmedia.infinitum.orm.LazilyLoadedObject;
import com.clarionmedia.infinitum.orm.OrmConstants;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.relationship.ForeignKeyRelationship;
import com.clarionmedia.infinitum.orm.relationship.ManyToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.ManyToOneRelationship;
import com.clarionmedia.infinitum.orm.relationship.ModelRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToOneRelationship;
import com.clarionmedia.infinitum.orm.sql.SqlBuilder;
import com.clarionmedia.infinitum.orm.sql.SqlExecutor;
import com.clarionmedia.infinitum.orm.sqlite.SqliteModelFactory;
import com.clarionmedia.infinitum.orm.sqlite.SqliteTypeAdapter;
import com.google.dexmaker.stock.ProxyBuilder;

/**
 * <p>
 * This is an implementation of {@link SqliteModelFactory}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/20/12
 */
public class SqliteModelFactoryImpl implements SqliteModelFactory {

	private static final String INSTANTIATION_ERROR = "Could not instantiate Object of type '%s'.";

	private SqlExecutor mExecutor;
	private SqlBuilder mSqlBuilder;
	private SqliteSession mSession;
	private SqliteMapper mMapper;
	private PersistencePolicy mPolicy;
	private Logger mLogger;
	private PropertyLoader mPropLoader;

	/**
	 * Constructs a {@code SqliteModelFactoryImpl} with the given
	 * {@link Context}.
	 * 
	 * @param context
	 *            the {@code Context} for this model factory
	 */
	public SqliteModelFactoryImpl(SqliteSession session, SqliteMapper mapper) {
		mExecutor = new SqliteExecutor(session.getDatabase());
		mSqlBuilder = new SqliteBuilder(mapper);
		mSession = session;
		mMapper = mapper;
		mPolicy = ContextFactory.getInstance().getPersistencePolicy();
		mLogger = Logger.getInstance(getClass().getSimpleName());
		mPropLoader = new PropertyLoader(ContextFactory.getInstance().getAndroidContext());
	}

	@Override
	public <T> T createFromResult(SqliteResult result, Class<T> modelClass) throws ModelConfigurationException,
			InfinitumRuntimeException {
		mSession.reconcileCache();
		return createFromCursorRec(result.getCursor(), modelClass);
	}

	@Override
	public <T> T createFromCursor(Cursor cursor, Class<T> modelClass) throws ModelConfigurationException,
			InfinitumRuntimeException {
		mSession.reconcileCache();
		return createFromCursorRec(cursor, modelClass);
	}

	@SuppressWarnings("unchecked")
	private <T> T createFromCursorRec(Cursor cursor, Class<T> modelClass) throws ModelConfigurationException,
			InfinitumRuntimeException {
		T ret = null;
		SqliteResult result = new SqliteResult(cursor);
		try {
			Constructor<T> ctor = modelClass.getConstructor();
			ctor.setAccessible(true);
			ret = ctor.newInstance();
		} catch (SecurityException e) {
			throw new InfinitumRuntimeException(String.format(INSTANTIATION_ERROR, modelClass.getName()));
		} catch (NoSuchMethodException e) {
			throw new ModelConfigurationException(String.format(mPropLoader.getErrorMessage("NO_EMPTY_CONSTRUCTOR"),
					modelClass.getName()));
		} catch (IllegalArgumentException e) {
			throw new InfinitumRuntimeException(String.format(INSTANTIATION_ERROR, modelClass.getName()));
		} catch (InstantiationException e) {
			throw new InfinitumRuntimeException(String.format(INSTANTIATION_ERROR, modelClass.getName()));
		} catch (IllegalAccessException e) {
			throw new InfinitumRuntimeException(String.format(INSTANTIATION_ERROR, modelClass.getName()));
		} catch (InvocationTargetException e) {
			throw new InfinitumRuntimeException(String.format(INSTANTIATION_ERROR, modelClass.getName()));
		}
		List<Field> fields = mPolicy.getPersistentFields(modelClass);
		for (Field f : fields) {
			f.setAccessible(true);
			if (!mPolicy.isRelationship(f)) {
				SqliteTypeAdapter<?> resolver = mMapper.resolveType(f.getType());
				int index = result.getColumnIndex(mPolicy.getFieldColumnName(f));
				try {
					resolver.mapToObject(result, index, f, ret);
				} catch (IllegalArgumentException e) {
					throw new InfinitumRuntimeException("Could not map '" + f.getType().getName() + "'");
				} catch (IllegalAccessException e) {
					throw new InfinitumRuntimeException("Could not map '" + f.getType().getName() + "'");
				}
			}
		}
		int objHash = mPolicy.computeModelHash(ret);
		if (mSession.checkCache(objHash))
			return (T) mSession.searchCache(objHash);
		mSession.cache(objHash, ret);
		loadRelationships(ret);
		return ret;
	}

	private <T> void loadRelationships(T model) throws ModelConfigurationException, InfinitumRuntimeException {
		for (Field f : mPolicy.getPersistentFields(model.getClass())) {
			f.setAccessible(true);
			if (!mPolicy.isRelationship(f))
				continue;
			ModelRelationship rel = mPolicy.getRelationship(f);
			switch (rel.getRelationType()) {
			case ManyToMany:
				loadManyToMany((ManyToManyRelationship) rel, f, model);
				break;
			case ManyToOne:
				if (mPolicy.isLazy(model.getClass()))
					lazilyLoadManyToOne((ManyToOneRelationship) rel, f, model);
				else
					loadManyToOne((ManyToOneRelationship) rel, f, model);
				break;
			case OneToMany:
				if (mPolicy.isLazy(model.getClass()))
					lazilyLoadOneToMany((OneToManyRelationship) rel, f, model);
				else
					loadOneToMany((OneToManyRelationship) rel, f, model);
				break;
			case OneToOne:
				if (mPolicy.isLazy(model.getClass()))
					lazilyLoadOneToOne((OneToOneRelationship) rel, f, model);
				else
					loadOneToOne((OneToOneRelationship) rel, f, model);
				break;
			}
		}
	}

	private <T> void lazilyLoadOneToOne(final OneToOneRelationship rel, Field f, T model) {
		final String sql = getEntityQuery(model, rel.getSecondType(), f, rel);
		Object related = null;
		if (mExecutor.count(sql.replace("*", "count(*)")) > 0) {
			try {
				related = ProxyBuilder.forClass(rel.getSecondType()).handler(new LazilyLoadedObject() {
					@Override
					protected Object loadObject() {
						Object ret = null;
						SqliteResult result = (SqliteResult) mExecutor.execute(sql);
						while (result.getCursor().moveToNext())
							ret = createFromResult(result, rel.getSecondType());
						result.close();
						return ret;
					}
				}).dexCache(mSession.getContext().getDir(OrmConstants.DEX_CACHE, Context.MODE_PRIVATE)).build();
			} catch (IOException e1) {
				throw new InfinitumRuntimeException("Could not build entity proxy");
			}
		}
		try {
			f.set(model, related);
		} catch (IllegalArgumentException e) {
			mLogger.error("Unable to set relationship field for object of type '" + model.getClass().getName() + "'", e);
		} catch (IllegalAccessException e) {
			mLogger.error("Unable to set relationship field for object of type '" + model.getClass().getName() + "'", e);
		}
	}

	private <T> void loadOneToOne(OneToOneRelationship rel, Field f, T model) {
		String sql = getEntityQuery(model, rel.getSecondType(), f, rel);
		SqliteResult result = (SqliteResult) mExecutor.execute(sql);
		while (result.getCursor().moveToNext())
			try {
				f.set(model, createFromResult(result, rel.getSecondType()));
			} catch (IllegalArgumentException e) {
				mLogger.error("Unable to set relationship field for object of type '" + model.getClass().getName()
						+ "'", e);
			} catch (IllegalAccessException e) {
				mLogger.error("Unable to set relationship field for object of type '" + model.getClass().getName()
						+ "'", e);
			}
		result.close();
	}

	@SuppressWarnings("unchecked")
	private <T> void lazilyLoadOneToMany(final OneToManyRelationship rel, Field f, T model) {
		final StringBuilder sql = new StringBuilder("SELECT * FROM ")
				.append(mPolicy.getModelTableName(rel.getManyType())).append(" WHERE ").append(rel.getColumn())
				.append(" = ");
		Object pk = mPolicy.getPrimaryKey(model);
		switch (mMapper.getSqliteDataType(mPolicy.getPrimaryKeyField(model.getClass()))) {
		case TEXT:
			sql.append("'").append(pk).append("'");
			break;
		default:
			sql.append(pk);
		}
		try {
			final Collection<Object> collection = (Collection<Object>) f.get(model);
			Collection<Object> related = ProxyBuilder.forClass(collection.getClass()).handler(new LazilyLoadedObject() {
				@Override
				protected Object loadObject() {
					SqliteResult result = (SqliteResult) mExecutor.execute(sql.toString());
					while (result.getCursor().moveToNext())
						collection.add(createFromResult(result, rel.getManyType()));
					result.close();
					return collection;
				}
			}).dexCache(mSession.getContext().getDir(OrmConstants.DEX_CACHE, Context.MODE_PRIVATE)).build();
			f.set(model, related);
		} catch (IllegalArgumentException e) {
			mLogger.error("Unable to set relationship field for object of type '" + model.getClass().getName() + "'", e);
		} catch (IllegalAccessException e) {
			mLogger.error("Unable to set relationship field for object of type '" + model.getClass().getName() + "'", e);
		} catch (IOException e) {
			throw new InfinitumRuntimeException("Could not build entity proxy");
		}
	}

	private <T> void loadOneToMany(OneToManyRelationship rel, Field f, T model) {
		StringBuilder sql = new StringBuilder("SELECT * FROM ").append(mPolicy.getModelTableName(rel.getManyType()))
				.append(" WHERE ").append(rel.getColumn()).append(" = ");
		Object pk = mPolicy.getPrimaryKey(model);
		switch (mMapper.getSqliteDataType(mPolicy.getPrimaryKeyField(model.getClass()))) {
		case TEXT:
			sql.append("'").append(pk).append("'");
			break;
		default:
			sql.append(pk);
		}
		SqliteResult result = (SqliteResult) mExecutor.execute(sql.toString());
		try {
			@SuppressWarnings("unchecked")
			Collection<Object> related = (Collection<Object>) f.get(model);
			while (result.getCursor().moveToNext())
				related.add(createFromResult(result, rel.getManyType()));
			f.set(model, related);
		} catch (IllegalArgumentException e) {
			mLogger.error("Unable to set relationship field for object of type '" + model.getClass().getName() + "'", e);
		} catch (IllegalAccessException e) {
			mLogger.error("Unable to set relationship field for object of type '" + model.getClass().getName() + "'", e);
		}
		result.close();
	}

	private <T> void lazilyLoadManyToOne(ManyToOneRelationship rel, Field f, T model) {
		final Class<?> direction = model.getClass() == rel.getFirstType() ? rel.getSecondType() : rel.getFirstType();
		final String sql = getEntityQuery(model, direction, f, rel);
		Object related = null;
		if (mExecutor.count(sql.replace("*", "count(*)")) > 0) {
			try {
				related = ProxyBuilder.forClass(rel.getSecondType()).handler(new LazilyLoadedObject() {
					@Override
					protected Object loadObject() {
						Object ret = null;
						SqliteResult result = (SqliteResult) mExecutor.execute(sql);
						while (result.getCursor().moveToNext())
							ret = createFromResult(result, direction);
						result.close();
						return ret;
					}
				}).dexCache(mSession.getContext().getDir(OrmConstants.DEX_CACHE, Context.MODE_PRIVATE)).build();
			} catch (IOException e1) {
				throw new InfinitumRuntimeException("Could not build entity proxy");
			}
		}
		try {
			f.set(model, related);
		} catch (IllegalArgumentException e) {
			mLogger.error("Unable to set relationship field for object of type '" + model.getClass().getName() + "'", e);
		} catch (IllegalAccessException e) {
			mLogger.error("Unable to set relationship field for object of type '" + model.getClass().getName() + "'", e);
		}
	}

	private <T> void loadManyToOne(ManyToOneRelationship rel, Field f, T model) {
		Class<?> direction = model.getClass() == rel.getFirstType() ? rel.getSecondType() : rel.getFirstType();
		String sql = getEntityQuery(model, direction, f, rel);
		SqliteResult result = (SqliteResult) mExecutor.execute(sql);
		while (result.getCursor().moveToNext())
			try {
				f.set(model, createFromResult(result, direction));
			} catch (IllegalArgumentException e) {
				mLogger.error("Unable to set relationship field for object of type '" + model.getClass().getName()
						+ "'", e);
			} catch (IllegalAccessException e) {
				mLogger.error("Unable to set relationship field for object of type '" + model.getClass().getName()
						+ "'", e);
			}
		result.close();
	}

	private <T> void loadManyToMany(ManyToManyRelationship rel, Field f, T model) throws ModelConfigurationException,
			InfinitumRuntimeException {
		try {
			// TODO Add reflexive M:M support
			Class<?> direction = model.getClass() == rel.getFirstType() ? rel.getSecondType() : rel.getFirstType();
			Field pk = mPolicy.getPrimaryKeyField(model.getClass());
			String sql = mSqlBuilder.createManyToManyJoinQuery(rel, (Serializable) pk.get(model), direction);
			SqliteResult result = (SqliteResult) mExecutor.execute(sql);
			@SuppressWarnings("unchecked")
			Collection<Object> related = (Collection<Object>) f.get(model);
			while (result.getCursor().moveToNext())
				related.add(createFromResult(result, direction));
			result.close();
			f.set(model, related);
		} catch (IllegalArgumentException e) {
			mLogger.error("Unable to set relationship field for object of type '" + model.getClass().getName() + "'", e);
		} catch (IllegalAccessException e) {
			mLogger.error("Unable to set relationship field for object of type '" + model.getClass().getName() + "'", e);
		}
	}

	private String getEntityQuery(Object model, Class<?> c, Field f, ForeignKeyRelationship rel) {
		StringBuilder sql = new StringBuilder("SELECT * FROM ").append(mPolicy.getModelTableName(c)).append(" WHERE ")
				.append(mPolicy.getFieldColumnName(mPolicy.getPrimaryKeyField(c))).append(" = ");
		switch (mMapper.getSqliteDataType(f)) {
		case TEXT:
			sql.append("'").append(getForeignKey(model, f, rel)).append("'");
			break;
		default:
			sql.append(getForeignKey(model, f, rel));
		}
		return sql.append(" LIMIT 1").toString();
	}

	private long getForeignKey(Object model, Field f, ForeignKeyRelationship rel) {
		StringBuilder q = new StringBuilder("SELECT ").append(rel.getColumn()).append(" FROM ")
				.append(mPolicy.getModelTableName(model.getClass())).append(" WHERE ")
				.append(mPolicy.getFieldColumnName(mPolicy.getPrimaryKeyField(model.getClass()))).append(" = ");
		Object pk = mPolicy.getPrimaryKey(model);
		switch (mMapper.getSqliteDataType(f)) {
		case TEXT:
			q.append("'").append(pk).append("'");
			break;
		default:
			q.append(pk);
		}
		SqliteResult res = (SqliteResult) mExecutor.execute(q.toString());
		res.getCursor().moveToFirst();
		long id = res.getLong(0);
		res.close();
		return id;
	}

}
