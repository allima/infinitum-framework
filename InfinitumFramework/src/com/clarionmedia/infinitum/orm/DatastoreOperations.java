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

package com.clarionmedia.infinitum.orm;

import java.io.Serializable;
import java.util.Collection;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;

/**
 * <p>
 * This interface specifies methods for interacting with a datastore. This is
 * not typically used directly but allows for greater testability.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/15/12
 */
public interface DatastoreOperations {

	/**
	 * Persists the given {@link Object} to the database. This method is
	 * idempotent, meaning if the record already exists, it will not be
	 * effected.
	 * 
	 * @param model
	 *            {@code Object} to persist to the database
	 * @return the row ID of the newly inserted record, or -1 if the insert
	 *         failed
	 * @throws InfinitumRuntimeException
	 *             if the model is marked transient
	 */
	long save(Object model) throws InfinitumRuntimeException;

	/**
	 * Updates the given {@link Object} in the database.
	 * 
	 * @param model
	 *            {@code Object} to update in the database
	 * @return {@code true} if the updated succeeded, {@code false} if it failed
	 * @throws InfinitumRuntimeException
	 *             if the model is marked transient
	 */
	boolean update(Object model) throws InfinitumRuntimeException;

	/**
	 * Deletes the given <code>Object</code> from the database if it exists.
	 * 
	 * @param model
	 *            <code>Object</code> to delete from the database
	 * @return true if the record was deleted, false otherwise
	 * @throws InfinitumRuntimeException
	 *             if the model is marked transient
	 */
	boolean delete(Object model) throws InfinitumRuntimeException;

	/**
	 * Persists the given <code>Object</code> to the database, or, if it already
	 * exists, updates the record.
	 * 
	 * @param model
	 *            <code>Object</code> to save or update in the database
	 * @return the row ID of the newly inserted row or 0 if the row was updated
	 * @throws InfinitumRuntimeException
	 *             if the model is marked transient
	 */
	long saveOrUpdate(Object model) throws InfinitumRuntimeException;

	/**
	 * Persists or updates the entire collection of <code>Objects</code> in the
	 * database.
	 * 
	 * @param models
	 *            <code>Objects</code> to save or update in the database
	 * @throws InfinitumRuntimeException
	 *             if one or more of the models is marked transient
	 */
	void saveOrUpdateAll(Collection<? extends Object> models) throws InfinitumRuntimeException;

	/**
	 * Persists the entire collection of <code>Objects</code> to the database.
	 * 
	 * @param models
	 *            <code>Objects</code> to persist to the database
	 * @return the number of records saved
	 * @throws InfinitumRuntimeException
	 *             if one or more of the models is marked transient
	 */
	int saveAll(Collection<? extends Object> models) throws InfinitumRuntimeException;

	/**
	 * Deletes the entire collection of <code>Objects</code> from the database
	 * if they exist.
	 * 
	 * @param models
	 *            <code>Objects</code> to delete from the database
	 * @return the number of records deleted
	 * @throws InfinitumRuntimeException
	 *             if one or more of the models is marked transient
	 */
	int deleteAll(Collection<? extends Object> models) throws InfinitumRuntimeException;

	/**
	 * Returns an instance of the given persistent model {@link Class} as
	 * identified by the specified primary key(s) or {@code null} if no such
	 * entity exists.
	 * 
	 * @param c
	 *            the {@code Class} of the persistent instance to load
	 * @param id
	 *            the primary key value of the persistent instance to load
	 * @return the persistent instance
	 * @throws InfinitumRuntimeException
	 *             if the specified {@code Class} is marked transient
	 * @throws IllegalArgumentException
	 *             if an incorrect number of primary keys is provided
	 */
	<T extends Object> T load(Class<T> c, Serializable id) throws InfinitumRuntimeException,
			IllegalArgumentException;

	/**
	 * Executes the given SQL query on the database.
	 * 
	 * @param sql
	 *            the SQL query to execute
	 */
	void execute(String sql);

}
