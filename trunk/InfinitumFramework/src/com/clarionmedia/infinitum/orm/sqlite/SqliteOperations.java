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

import android.database.SQLException;

/**
 * <p>
 * This interface specifies methods for basic SQLite operations. This is
 * typically not used directly, rather the implementation {@link SqliteTemplate}
 * is used.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/11/12
 */
public interface SqliteOperations {

	/**
	 * Opens the database to begin a transaction. If the database does not
	 * exist, it will be created.
	 * 
	 * @return an instance of itself to enable chaining
	 * @throws SQLException
	 *             thrown if the database cannot be opened for writing
	 */
	SqliteOperations open() throws SQLException;

	/**
	 * Closes the database connection, effectively ending the transaction.
	 */
	void close();

	/**
	 * Persists the given <code>Object</code> to the database. This method is
	 * idempotent, meaning if the record already exists, it will not be
	 * effected.
	 * 
	 * @param model
	 *            <code>Object</code> to persist to the database
	 * @return the row ID of the newly inserted record, or -1 if the insert
	 *         failed
	 */
	long save(Object model);

	/**
	 * Updates the given <code>Object</code> in the database.
	 * 
	 * @param model
	 *            <code>Object</code> to update in the database
	 * @return the row ID of the effected record, 0 if the record did not exist,
	 *         or -1 if the update failed
	 */
	long update(Object model);

	/**
	 * Deletes the given <code>Object</code> from the database if it exists.
	 * 
	 * @param model
	 *            <code>Object</code> to delete from the database
	 * @return true if the record was deleted, false otherwise
	 */
	boolean delete(Object model);

	/**
	 * Persists the given <code>Object</code> to the database, or, if it already
	 * exists, updates the record.
	 * 
	 * @param model
	 *            <code>Object</code> to save or update in the database
	 * @return the row ID of the newly inserted row, 0 if the row was updated,
	 *         or -1 if the query failed
	 */
	long saveOrUpdate(Object model);

	/**
	 * Persists or updates the entire collection of <code>Objects</code> in the
	 * database.
	 * 
	 * @param models
	 *            <code>Objects</code> to save or update in the database
	 */
	void saveOrUpdateAll(Iterable<? extends Object> models);

	/**
	 * Persists the entire collection of <code>Objects</code> to the database.
	 * 
	 * @param models
	 *            <code>Objects</code> to persist to the database
	 */
	void saveAll(Iterable<? extends Object> models);

	/**
	 * Deletes the entire collection of <code>Objects</code> from the database
	 * if they exist.
	 * 
	 * @param models
	 *            <code>Objects</code> to delete from the database
	 * @return the number of records deleted
	 */
	int deleteAll(Iterable<? extends Object> models);

	/**
	 * Executes the given SQL query on the database.
	 * 
	 * @param sql
	 *            the SQL query to execute
	 */
	void execute(String sql);

}
