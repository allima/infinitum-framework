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
import java.util.List;

import android.content.Context;
import android.database.Cursor;

import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.logging.Logger;
import com.clarionmedia.infinitum.orm.LazyLoadDexMakerProxy;
import com.clarionmedia.infinitum.orm.ModelFactory;
import com.clarionmedia.infinitum.orm.ResultSet;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.relationship.ForeignKeyRelationship;
import com.clarionmedia.infinitum.orm.relationship.ManyToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.ManyToOneRelationship;
import com.clarionmedia.infinitum.orm.relationship.ModelRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToOneRelationship;
import com.clarionmedia.infinitum.orm.sqlite.SqliteTypeAdapter;

/**
 * <p>
 * This is an implementation of {@link ModelFactory} for processing {@link SqliteResult}
 * queries.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/20/12
 * @since 1.0
 */
public class SqliteModelFactory implements ModelFactory {

	private static final String INSTANTIATION_ERROR = "Could not instantiate Object of type '%s'.";

	private SqliteBuilder mSqlBuilder;
	private SqliteSession mSession;
	private SqliteMapper mMapper;
	private PersistencePolicy mPersistencePolicy;
	private Logger mLogger;

	/**
	 * Constructs a {@code SqliteModelFactoryImpl} with the given
	 * {@link Context}.
	 * 
	 * @param context
	 *            the {@code Context} for this model factory
	 */
	public SqliteModelFactory(SqliteSession session, SqliteMapper mapper) {
		InfinitumContext context = session.getInfinitumContext();
		mSqlBuilder = new SqliteBuilder(context, mapper);
		mSession = session;
		mMapper = mapper;
		mPersistencePolicy = context.getPersistencePolicy();
		mLogger = Logger.getInstance(context, getClass().getSimpleName());
	}

	@Override
	public <T> T createFromResult(ResultSet result, Class<T> modelClass) {
		if (!(result instanceof SqliteResult))
			throw new IllegalArgumentException("SqliteModelFactory can only process SqliteResults.");
		return createFromCursorRec(((SqliteResult) result).getCursor(), modelClass);
	}
	
	/**
	 * Constructs a domain model instance and populates its {@link Field}'s from
	 * the given {@link Cursor}. The precondition for this method is that the
	 * {@code Cursor} is currently at the row to convert to an {@link Object}
	 * from the correct table.
	 * 
	 * @param cursor
	 *            the {@code Cursor} containing the row to convert to an
	 *            {@code Object}
	 * @param modelClass
	 *            the {@code Class} of the {@code Object} being instantiated
	 * @return a populated instance of the specified {@code Class}
	 * @throws ModelConfigurationException
	 *             if the specified model {@code Class} does not contain an
	 *             empty constructor
	 * @throws InfinitumRuntimeException
	 *             if the model could not be instantiated
	 */
	public <T> T createFromCursor(Cursor cursor, Class<T> modelClass) throws ModelConfigurationException, InfinitumRuntimeException {
		return createFromCursorRec(cursor, modelClass);
	}

	@SuppressWarnings("unchecked")
	private <T> T createFromCursorRec(Cursor cursor, Class<T> modelClass) throws ModelConfigurationException, InfinitumRuntimeException {
		T ret = null;
		SqliteResult result = new SqliteResult(cursor);
		try {
			ret = modelClass.newInstance();
		} catch (InstantiationException e) {
			throw new InfinitumRuntimeException(String.format(INSTANTIATION_ERROR, modelClass.getName()));
		} catch (IllegalAccessException e) {
			throw new InfinitumRuntimeException(String.format(INSTANTIATION_ERROR, modelClass.getName()));
		}
		List<Field> fields = mPersistencePolicy.getPersistentFields(modelClass);
		for (Field f : fields) {
			f.setAccessible(true);
			if (!mPersistencePolicy.isRelationship(f)) {
				SqliteTypeAdapter<?> resolver = mMapper.resolveType(f.getType());
				int index = result.getColumnIndex(mPersistencePolicy.getFieldColumnName(f));
				try {
					resolver.mapToObject(result, index, f, ret);
				} catch (IllegalArgumentException e) {
					throw new InfinitumRuntimeException("Could not map '" + f.getType().getName() + "'");
				} catch (IllegalAccessException e) {
					throw new InfinitumRuntimeException("Could not map '" + f.getType().getName() + "'");
				}
			}
		}
		int objHash = mPersistencePolicy.computeModelHash(ret);
		if (mSession.checkCache(objHash))
			return (T) mSession.searchCache(objHash);
		mSession.cache(objHash, ret);
		loadRelationships(ret);
		return ret;
	}

	private <T> void loadRelationships(T model)
			throws ModelConfigurationException, InfinitumRuntimeException {
		for (Field f : mPersistencePolicy.getPersistentFields(model.getClass())) {
			f.setAccessible(true);
			if (!mPersistencePolicy.isRelationship(f))
				continue;
			ModelRelationship rel = mPersistencePolicy.getRelationship(f);
			switch (rel.getRelationType()) {
			case ManyToMany:
				if (mPersistencePolicy.isLazy(model.getClass()))
					lazilyLoadManyToMany((ManyToManyRelationship) rel, f, model);
				else
					loadManyToMany((ManyToManyRelationship) rel, f, model);
				break;
			case ManyToOne:
				if (mPersistencePolicy.isLazy(model.getClass()))
					lazilyLoadManyToOne((ManyToOneRelationship) rel, f, model);
				else
					loadManyToOne((ManyToOneRelationship) rel, f, model);
				break;
			case OneToMany:
				if (mPersistencePolicy.isLazy(model.getClass()))
					lazilyLoadOneToMany((OneToManyRelationship) rel, f, model);
				else
					loadOneToMany((OneToManyRelationship) rel, f, model);
				break;
			case OneToOne:
				if (mPersistencePolicy.isLazy(model.getClass()))
					lazilyLoadOneToOne((OneToOneRelationship) rel, f, model);
				else
					loadOneToOne((OneToOneRelationship) rel, f, model);
				break;
			}
		}
	}

	private <T> void lazilyLoadOneToOne(final OneToOneRelationship rel,
			Field f, T model) {
		final String sql = getOneToOneEntityQuery(model, rel.getSecondType(), f, rel);
		Object related = null;
		if (mSession.count(sql.replace("*", "count(*)")) > 0) {
			related = new LazyLoadDexMakerProxy(mSession.getContext(),
					rel.getSecondType()) {
				@Override
				protected Object loadObject() {
					Object ret = null;
					Cursor result = mSession.executeForResult(sql, true);
					while (result.moveToNext())
						ret = createFromCursor(result, rel.getSecondType());
					result.close();
					return ret;
				}
			}.getProxy();
		}
		try {
			f.set(model, related);
		} catch (IllegalArgumentException e) {
			mLogger.error(
					"Unable to set relationship field for object of type '"
							+ model.getClass().getName() + "'", e);
		} catch (IllegalAccessException e) {
			mLogger.error(
					"Unable to set relationship field for object of type '"
							+ model.getClass().getName() + "'", e);
		}
	}

	private <T> void loadOneToOne(OneToOneRelationship rel, Field f, T model) {
		String sql = getOneToOneEntityQuery(model, rel.getSecondType(), f, rel);
		Cursor result = mSession.executeForResult(sql, true);
		while (result.moveToNext())
			try {
				f.set(model, createFromCursor(result, rel.getSecondType()));
			} catch (IllegalArgumentException e) {
				mLogger.error(
						"Unable to set relationship field for object of type '"
								+ model.getClass().getName() + "'", e);
			} catch (IllegalAccessException e) {
				mLogger.error(
						"Unable to set relationship field for object of type '"
								+ model.getClass().getName() + "'", e);
			}
		result.close();
	}

	@SuppressWarnings("unchecked")
	private <T> void lazilyLoadOneToMany(final OneToManyRelationship rel,
			Field f, T model) {
		final StringBuilder sql = new StringBuilder("SELECT * FROM ")
				.append(mPersistencePolicy.getModelTableName(rel.getManyType()))
				.append(" WHERE ").append(rel.getColumn()).append(" = ");
		Serializable pk = mPersistencePolicy.getPrimaryKey(model);
		switch (mMapper.getSqliteDataType(mPersistencePolicy.getPrimaryKeyField(model
				.getClass()))) {
		case TEXT:
			sql.append("'").append(pk).append("'");
			break;
		default:
			sql.append(pk);
		}
		try {
			final Collection<Object> collection = (Collection<Object>) f
					.get(model);
			Collection<Object> related = (Collection<Object>) new LazyLoadDexMakerProxy(mSession.getContext(), collection.getClass()) {
				@Override
				protected Object loadObject() {
					Cursor result = mSession.executeForResult(sql.toString(), true);
					while (result.moveToNext())
						collection.add(createFromCursor(result, rel.getManyType()));
					result.close();
					return collection;
				}
			}.getProxy();
			f.set(model, related);
		} catch (IllegalArgumentException e) {
			mLogger.error(
					"Unable to set relationship field for object of type '"
							+ model.getClass().getName() + "'", e);
		} catch (IllegalAccessException e) {
			mLogger.error(
					"Unable to set relationship field for object of type '"
							+ model.getClass().getName() + "'", e);
		}
	}

	private <T> void loadOneToMany(OneToManyRelationship rel, Field f, T model) {
		StringBuilder sql = new StringBuilder("SELECT * FROM ")
				.append(mPersistencePolicy.getModelTableName(rel.getManyType()))
				.append(" WHERE ").append(rel.getColumn()).append(" = ");
		Serializable pk = mPersistencePolicy.getPrimaryKey(model);
		switch (mMapper.getSqliteDataType(mPersistencePolicy.getPrimaryKeyField(model
				.getClass()))) {
		case TEXT:
			sql.append("'").append(pk).append("'");
			break;
		default:
			sql.append(pk);
		}
		Cursor result = mSession.executeForResult(sql.toString(), true);
		try {
			@SuppressWarnings("unchecked")
			Collection<Object> related = (Collection<Object>) f.get(model);
			while (result.moveToNext())
				related.add(createFromCursor(result, rel.getManyType()));
			f.set(model, related);
		} catch (IllegalArgumentException e) {
			mLogger.error(
					"Unable to set relationship field for object of type '"
							+ model.getClass().getName() + "'", e);
		} catch (IllegalAccessException e) {
			mLogger.error(
					"Unable to set relationship field for object of type '"
							+ model.getClass().getName() + "'", e);
		}
		result.close();
	}

	private <T> void lazilyLoadManyToOne(ManyToOneRelationship rel, Field f,
			T model) {
		final Class<?> direction = model.getClass() == rel.getFirstType() ? rel
				.getSecondType() : rel.getFirstType();
		final String sql = getEntityQuery(model, direction, f, rel);
		Object related = null;
		if (mSession.count(sql.replace("*", "count(*)")) > 0) {
			related = new LazyLoadDexMakerProxy(mSession.getContext(), rel.getSecondType()) {
				@Override
				protected Object loadObject() {
					Object ret = null;
					Cursor result = mSession.executeForResult(sql, true);
					while (result.moveToNext())
						ret = createFromCursor(result, direction);
					result.close();
					return ret;
				}
			}.getProxy();
		}
		try {
			f.set(model, related);
		} catch (IllegalArgumentException e) {
			mLogger.error(
					"Unable to set relationship field for object of type '"
							+ model.getClass().getName() + "'", e);
		} catch (IllegalAccessException e) {
			mLogger.error(
					"Unable to set relationship field for object of type '"
							+ model.getClass().getName() + "'", e);
		}
	}

	private <T> void loadManyToOne(ManyToOneRelationship rel, Field f, T model) {
		Class<?> direction = model.getClass() == rel.getFirstType() ? rel
				.getSecondType() : rel.getFirstType();
		String sql = getEntityQuery(model, direction, f, rel);
		Cursor result = mSession.executeForResult(sql, true);
		while (result.moveToNext())
			try {
				f.set(model, createFromCursor(result, direction));
			} catch (IllegalArgumentException e) {
				mLogger.error(
						"Unable to set relationship field for object of type '"
								+ model.getClass().getName() + "'", e);
			} catch (IllegalAccessException e) {
				mLogger.error(
						"Unable to set relationship field for object of type '"
								+ model.getClass().getName() + "'", e);
			}
		result.close();
	}

	@SuppressWarnings("unchecked")
	private <T> void lazilyLoadManyToMany(final ManyToManyRelationship rel,
			Field f, T model) {
		// TODO Add reflexive M:M support
		final Class<?> direction = model.getClass() == rel.getFirstType() ? rel
				.getSecondType() : rel.getFirstType();
		Serializable pk = mPersistencePolicy.getPrimaryKey(model);
		final String sql = mSqlBuilder.createManyToManyJoinQuery(rel, pk,
				direction);
		try {
			final Collection<Object> collection = (Collection<Object>) f
					.get(model);
			Collection<Object> related = (Collection<Object>) new LazyLoadDexMakerProxy(mSession.getContext(), collection.getClass()) {
				@Override
				protected Object loadObject() {
					Cursor result = mSession.executeForResult(sql, true);
					while (result.moveToNext())
						collection.add(createFromCursor(result, direction));
					result.close();
					return collection;
				}
			}.getProxy();
			f.set(model, related);
		} catch (IllegalArgumentException e) {
			mLogger.error(
					"Unable to set relationship field for object of type '"
							+ model.getClass().getName() + "'", e);
		} catch (IllegalAccessException e) {
			mLogger.error(
					"Unable to set relationship field for object of type '"
							+ model.getClass().getName() + "'", e);
		}
	}

	private <T> void loadManyToMany(ManyToManyRelationship rel, Field f, T model)
			throws ModelConfigurationException, InfinitumRuntimeException {
		try {
			// TODO Add reflexive M:M support
			Class<?> direction = model.getClass() == rel.getFirstType() ? rel
					.getSecondType() : rel.getFirstType();
			Serializable pk = mPersistencePolicy.getPrimaryKey(model);
			String sql = mSqlBuilder.createManyToManyJoinQuery(rel, pk,
					direction);
			Cursor result = mSession.executeForResult(sql, true);
			@SuppressWarnings("unchecked")
			Collection<Object> related = (Collection<Object>) f.get(model);
			while (result.moveToNext())
				related.add(createFromCursor(result, direction));
			result.close();
			f.set(model, related);
		} catch (IllegalArgumentException e) {
			mLogger.error(
					"Unable to set relationship field for object of type '"
							+ model.getClass().getName() + "'", e);
		} catch (IllegalAccessException e) {
			mLogger.error(
					"Unable to set relationship field for object of type '"
							+ model.getClass().getName() + "'", e);
		}
	}

	private String getEntityQuery(Object model, Class<?> c, Field f,
			ForeignKeyRelationship rel) {
		StringBuilder sql = new StringBuilder("SELECT * FROM ")
				.append(mPersistencePolicy.getModelTableName(c))
				.append(" WHERE ")
				.append(mPersistencePolicy.getFieldColumnName(mPersistencePolicy
						.getPrimaryKeyField(c))).append(" = ");
		switch (mMapper.getSqliteDataType(f)) {
		case TEXT:
			sql.append("'").append(getForeignKey(model, rel)).append("'");
			break;
		default:
			sql.append(getForeignKey(model, rel));
		}
		return sql.append(" LIMIT 1").toString();
	}
	
	private String getOneToOneEntityQuery(Object model, Class<?> relatedClass, Field f, OneToOneRelationship rel) {
		boolean isOwner = rel.getOwner() == model.getClass();
		StringBuilder sql = new StringBuilder("SELECT * FROM ")
				.append(mPersistencePolicy.getModelTableName(relatedClass))
				.append(" WHERE ");
		if (isOwner) {
		    sql.append(mPersistencePolicy.getFieldColumnName(mPersistencePolicy.getPrimaryKeyField(relatedClass)));
		} else {
			sql.append(rel.getColumn());
		}
		sql.append(" = ");
		switch (mMapper.getSqliteDataType(f)) {
		case TEXT:
			sql.append("'").append(getOneToOneKey(model, isOwner, rel)).append("'");
			break;
		default:
			sql.append(getOneToOneKey(model, isOwner, rel));
		}
		return sql.append(" LIMIT 1").toString();
	}
	
	private Serializable getOneToOneKey(Object model, boolean isOwner, OneToOneRelationship rel) {
		if (isOwner) {
			return getForeignKey(model, rel);
		} else {
			return mPersistencePolicy.getPrimaryKey(model);
		}
	}

	private Serializable getForeignKey(Object model, ForeignKeyRelationship rel) {
		StringBuilder q = new StringBuilder("SELECT ")
				.append(rel.getColumn())
				.append(" FROM ")
				.append(mPersistencePolicy.getModelTableName(model.getClass()))
				.append(" WHERE ")
				.append(mPersistencePolicy.getFieldColumnName(mPersistencePolicy
						.getPrimaryKeyField(model.getClass()))).append(" = ");
		Serializable pk = mPersistencePolicy.getPrimaryKey(model);
		switch (mMapper.getSqliteDataType(pk)) {
		case TEXT:
			q.append("'").append(pk).append("'");
			break;
		default:
			q.append(pk);
		}
		Cursor result = mSession.executeForResult(q.toString(), true);
		result.moveToFirst();
		Serializable id;
		try {
			id = result.getString(0);
		} catch (ClassCastException e) {
			throw new ModelConfigurationException(
					"Invalid primary key specified for model.");
		} finally {
			result.close();
		}
		return id;
	}

}
