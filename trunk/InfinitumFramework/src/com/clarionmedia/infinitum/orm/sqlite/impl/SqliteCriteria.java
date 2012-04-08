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

import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.criteria.CriteriaConstants;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.criteria.criterion.Criterion;
import com.clarionmedia.infinitum.orm.persistence.PersistenceResolution;
import com.clarionmedia.infinitum.orm.sql.SqlBuilder;

/**
 * <p>
 * Implementation of {@link Criteria}.
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

	/**
	 * Constructs a new {@code SqliteGenCriteria}.
	 * 
	 * @param session
	 *            the {@link SqliteSession} this {@code SqliteGenCriteria} is
	 *            attached to
	 * @param entityClass
	 *            the {@code Class} to create {@code SqliteGenCriteria} for
	 * @param sqlBuilder
	 *            {@link SqlBuilder} for generating SQL statements
	 * @param mapper
	 *            the {@link SqliteMapper} to use for {@link Object} mapping
	 * @throws InfinitumRuntimeException
	 *             if {@code entityClass} is transient
	 */
	public SqliteCriteria(SqliteSession session, Class<T> entityClass, SqlBuilder sqlBuilder, SqliteMapper mapper)
	        throws InfinitumRuntimeException {
		if (!PersistenceResolution.isPersistent(entityClass))
			throw new InfinitumRuntimeException(String.format(CriteriaConstants.TRANSIENT_CRITERIA, entityClass.getName()));
		mSession = session;
		mEntityClass = entityClass;
		mModelFactory = new SqliteModelFactoryImpl(session, mapper);
		mCriterion = new ArrayList<Criterion>();
		mSqlBuilder = sqlBuilder;
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
		Cursor result = mSession.executeForResult(mSqlBuilder.createQuery(this), true);
		if (result.getCount() == 0) {
			result.close();
			return ret;
		}
		try {
			while (result.moveToNext())
				ret.add(mModelFactory.createFromCursor(result, mEntityClass));
		} catch (InfinitumRuntimeException e) {
			throw e;
		} finally {
			result.close();
		}

		return ret;
	}

	@Override
	public T unique() throws InfinitumRuntimeException {
		Cursor result = mSession.executeForResult(mSqlBuilder.createQuery(this), true);
		if (result.getCount() > 1)
			throw new InfinitumRuntimeException(String.format(CriteriaConstants.NON_UNIQUE_RESULT, mEntityClass.getName(), result.getCount()));
		else if (result.getCount() == 0)
			return null;
		result.moveToFirst();
		return mModelFactory.createFromCursor(result, mEntityClass);
	}

	@Override
	public SqliteMapper getObjectMapper() {
		return mSession.getSqliteMapper();
	}

}
