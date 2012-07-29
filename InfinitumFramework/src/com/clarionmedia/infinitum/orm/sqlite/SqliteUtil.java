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

import com.clarionmedia.infinitum.aop.AopProxy;
import com.clarionmedia.infinitum.context.ContextFactory;
import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.internal.PropertyLoader;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.persistence.TypeResolutionPolicy;
import com.clarionmedia.infinitum.orm.persistence.TypeResolutionPolicy.SqliteDataType;
import com.clarionmedia.infinitum.orm.persistence.impl.DefaultTypeResolutionPolicy;
import com.clarionmedia.infinitum.orm.sqlite.impl.SqliteMapper;

/**
 * <p>
 * This class contains utility methods for generating SQL strings for a SQLite
 * database.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/15/12
 * @since 1.0
 */
public class SqliteUtil {

	private TypeResolutionPolicy mTypePolicy;
	private PropertyLoader mPropLoader;

	/**
	 * Constructs a new {@code SqliteUtil}.
	 * 
	 * @param context
	 *            the {@link InfinitumContext} to use for this
	 *            {@code SqliteUtil}
	 */
	public SqliteUtil(InfinitumContext context) {
		mTypePolicy = new DefaultTypeResolutionPolicy(context);
		mPropLoader = new PropertyLoader(context.getAndroidContext());
	}

	/**
	 * Generates a "where clause" {@link String} used for updating or deleting
	 * the given persistent {@link Object} in the database. Where conditions are
	 * indicated by primary key {@link Field}'s. Note that the actual
	 * {@code String} "where" is not included with the resulting output.
	 * 
	 * <p>
	 * For example, passing an object of class {@code Foobar} which has a
	 * primary key {@code foo} with a value of {@code 42} will result in the
	 * where clause {@code foo = 42}.
	 * </p>
	 * 
	 * @param model
	 *            the model to generate the where clause for
	 * @return where clause {@code String} for specified {@code Object}
	 * @throws InfinitumRuntimeException
	 *             if there is an error generating the SQL
	 */
	public String getWhereClause(Object model, SqliteMapper mapper)
			throws InfinitumRuntimeException {
		if (AopProxy.isAopProxy(model)) {
			model = AopProxy.getProxy(model).getTarget();
		}
		PersistencePolicy policy = ContextFactory.newInstance()
				.getPersistencePolicy();
		Field pk = policy.getPrimaryKeyField(model.getClass());
		StringBuilder sb = new StringBuilder();
		pk.setAccessible(true);
		sb.append(policy.getFieldColumnName(pk)).append(" = ");
		SqliteDataType t = mapper.getSqliteDataType(pk);
		Serializable pkVal = null;
		try {
			pkVal = (Serializable) pk.get(model);
		} catch (IllegalArgumentException e) {
			throw new InfinitumRuntimeException(String.format(
					mPropLoader.getErrorMessage("UNABLE_TO_GEN_QUERY"),
					model.getClass().getName()));
		} catch (IllegalAccessException e) {
			throw new InfinitumRuntimeException(String.format(
					mPropLoader.getErrorMessage("UNABLE_TO_GEN_QUERY"),
					model.getClass().getName()));
		} catch (ClassCastException e) {
			throw new ModelConfigurationException(
					"Invalid primary key specified for type '"
							+ model.getClass().getName() + "'.");
		}
		if (t == SqliteDataType.TEXT)
			sb.append("'").append(pkVal).append("'");
		else
			sb.append(pkVal);
		return sb.toString();
	}

	/**
	 * Generates a "where clause" {@link String} used for the given persistent
	 * {@link Class} in the database with the given primary key. Where
	 * conditions are indicated by primary key {@link Field}'s. Note that the
	 * actual {@code String} "where" is not included with the resulting output.
	 * 
	 * <p>
	 * For example, passing a {@code Class} {@code Foobar} which has a primary
	 * key {@code foo} with a value of {@code 42} will result in the where
	 * clause {@code foo = 42}.
	 * </p>
	 * 
	 * @param c
	 *            the {@code Class} of the model
	 * @param id
	 *            the primary key for the model
	 * @return where clause {@code String} for specified {@code Class} and
	 *         primary key
	 * @throws IllegalArgumentException
	 *             if there is a mismatch between the {@code Class}'s primary
	 *             key type and the type of the given primary key
	 */
	public String getWhereClause(Class<?> c, Serializable id,
			SqliteMapper mapper) throws IllegalArgumentException {
		PersistencePolicy policy = ContextFactory.newInstance()
				.getPersistencePolicy();
		Field pk = policy.getPrimaryKeyField(c);
		if (!mTypePolicy.isValidPrimaryKey(pk, id))
			throw new IllegalArgumentException(String.format(mPropLoader
					.getErrorMessage("INVALID_PK"), id.getClass()
					.getSimpleName(), c.getName()));
		StringBuilder sb = new StringBuilder();
		sb.append(policy.getFieldColumnName(pk)).append(" = ");
		SqliteDataType t = mapper.getSqliteDataType(pk);
		if (t == SqliteDataType.TEXT)
			sb.append("'").append(id).append("'");
		else
			sb.append(id);
		return sb.toString();
	}

}
