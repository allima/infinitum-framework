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

package com.clarionmedia.infinitum.orm.criteria;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.database.Cursor;

import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.persistence.PersistenceResolution;
import com.clarionmedia.infinitum.orm.sql.SqlQueryBuilder;
import com.clarionmedia.infinitum.orm.sqlite.SqliteOperations;
import com.clarionmedia.infinitum.reflection.ModelFactory;

/**
 * Implementation of {@link Criteria}.
 * 
 * @author Tyler Treat
 * @version 1.0 02/17/12
 */
public class CriteriaImpl<T> implements Criteria<T> {

	private Class<T> mEntityClass;
	private SqliteOperations mSqliteOps;
	private List<Criterion> mCriterion;
	private int mLimit;

	/**
	 * Constructs a new {@code CriteriaImpl} with the given entity {@link Class}
	 * .
	 * 
	 * @param entityClass
	 *            Class<?> to create {@code CriteriaImpl} for
	 * @param sqliteOps
	 *            {@link SqliteOperations} for which this {@code CriteriaImpl}
	 *            is being created for
	 * @throws InfinitumRuntimeException if {@code entityClass} is transient
	 */
	public CriteriaImpl(Class<T> entityClass, SqliteOperations sqliteOps) throws InfinitumRuntimeException {
		if (!PersistenceResolution.isPersistent(entityClass))
			throw new InfinitumRuntimeException(String.format(Constants.TRANSIENT_CRITERIA, entityClass.getName()));
		mEntityClass = entityClass;
		mSqliteOps = sqliteOps;
		mCriterion = new ArrayList<Criterion>();
	}
	
	@Override
	public String toSql() {
		return SqlQueryBuilder.createQuery(this);
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
	public List<T> toList() {
		List<T> ret = new LinkedList<T>();
		Cursor result = mSqliteOps.executeForResult(SqlQueryBuilder
				.createQuery(this));
		if (result.getCount() == 0) {
			result.close();
			return ret;
		}
		try {
			while (result.moveToNext())
				ret.add(ModelFactory.createFromCursor(result, mEntityClass));
		} catch (InfinitumRuntimeException e) {
			throw e;
		} finally {
			result.close();
		}

		return ret;
	}

	@Override
	public T unique() throws InfinitumRuntimeException {
		Cursor result = mSqliteOps.executeForResult(SqlQueryBuilder
				.createQuery(this));
		if (result.getCount() > 1)
			throw new InfinitumRuntimeException(String.format(
					Constants.NON_UNIQUE_RESULT, mEntityClass.getName(),
					result.getCount()));
		else if (result.getCount() == 0)
			return null;
		result.moveToFirst();
		return ModelFactory.createFromCursor(result, mEntityClass);
	}

}
