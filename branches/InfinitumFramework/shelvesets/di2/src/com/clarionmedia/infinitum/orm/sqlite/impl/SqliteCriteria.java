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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.database.Cursor;

import com.clarionmedia.infinitum.context.ContextFactory;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.internal.Preconditions;
import com.clarionmedia.infinitum.internal.PropertyLoader;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.criteria.criterion.Criterion;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.sql.SqlBuilder;

/**
 * <p>
 * Implementation of {@link Criteria} for SQLite queries.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/17/12
 */
public class SqliteCriteria<T> implements Criteria<T> {

	private Class<T> mEntityClass;
	private SqliteSession mSession;
	private SqliteModelFactoryImpl mModelFactory;
	private List<Criterion> mCriterion;
	private int mLimit;
	private int mOffset;
	private SqlBuilder mSqlBuilder;
	private PersistencePolicy mPersistencePolicy;
	private PropertyLoader mPropLoader;

	/**
	 * Constructs a new {@code SqliteCriteria}.
	 * 
	 * @param session
	 *            the {@link SqliteSession} this {@code SqliteCriteria} is
	 *            attached to
	 * @param entityClass
	 *            the {@code Class} to create {@code SqliteCriteria} for
	 * @param sqlBuilder
	 *            {@link SqlBuilder} for generating SQL statements
	 * @param mapper
	 *            the {@link SqliteMapper} to use for {@link Object} mapping
	 * @throws InfinitumRuntimeException
	 *             if {@code entityClass} is transient
	 */
	public SqliteCriteria(SqliteSession session, Class<T> entityClass, SqlBuilder sqlBuilder, SqliteMapper mapper)
			throws InfinitumRuntimeException {
		Preconditions.checkPersistenceForLoading(entityClass);
		mSession = session;
		mEntityClass = entityClass;
		mModelFactory = new SqliteModelFactoryImpl(session, mapper);
		mCriterion = new ArrayList<Criterion>();
		mSqlBuilder = sqlBuilder;
		mPersistencePolicy = ContextFactory.getInstance().getPersistencePolicy();
		mPropLoader = new PropertyLoader(ContextFactory.getInstance().getAndroidContext());
	}

	@Override
	public String toSql() {
		return mSqlBuilder.createQuery(this);
	}

	@Override
	public Class<T> getEntityClass() {
		return mEntityClass;
	}

	@Override
	public List<Criterion> getCriterion() {
		return mCriterion;
	}

	@Override
	public int getLimit() {
		return mLimit;
	}

	@Override
	public int getOffset() {
		return mOffset;
	}

	@Override
	public Criteria<T> add(Criterion criterion) {
		mCriterion.add(criterion);
		return this;
	}

	@Override
	public Criteria<T> limit(int limit) {
		mLimit = limit;
		return this;
	}

	@Override
	public Criteria<T> offset(int offset) {
		mOffset = offset;
		return this;
	}

	@Override
	public List<T> list() {
		List<T> ret = new LinkedList<T>();
		Cursor result = mSession.executeForResult(toSql(), true);
		if (result.getCount() == 0) {
			result.close();
			return ret;
		}
		try {
			while (result.moveToNext()) {
				T entity = mModelFactory.createFromCursor(result, mEntityClass);
				ret.add(entity);
				// Cache results
				mSession.cache(mPersistencePolicy.computeModelHash(entity), entity);
			}
		} finally {
			result.close();
		}
		return ret;
	}

	@Override
	public T unique() throws InfinitumRuntimeException {
		Cursor result = mSession.executeForResult(toSql(), true);
		if (result.getCount() > 1)
			throw new InfinitumRuntimeException(String.format(mPropLoader.getErrorMessage("NON_UNIQUE_RESULT"),
					mEntityClass.getName(), result.getCount()));
		else if (result.getCount() == 0)
			return null;
		result.moveToFirst();
		T ret = null;
		try {
			ret = mModelFactory.createFromCursor(result, mEntityClass);
			// Cache result
			mSession.cache(mPersistencePolicy.computeModelHash(ret), ret);
		} finally {
			result.close();
		}
		return ret;
	}

	@Override
	public SqliteMapper getObjectMapper() {
		return mSession.getSqliteMapper();
	}

	@Override
	public long count() {
		Cursor result = mSession.executeForResult(mSqlBuilder.createCountQuery(this), true);
		result.moveToFirst();
		long ret = result.getLong(0);
		result.close();
		return ret;
	}

}
