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

import android.database.SQLException;

/**
 * <p>
 * This provides abstracted methods for interacting with a SQLite database at an
 * object level.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/11/12
 */
public interface SqliteDao {

	/**
	 * Opens the database to begin a transaction.
	 * 
	 * @return an instance of itself to enable chaining
	 * @throws SQLException
	 *             thrown if the database cannot be opened for writing
	 */
	SqliteDao open() throws SQLException;

	/**
	 * Closes the database connection, effectively ending the transaction.
	 */
	void close();

	/**
	 * Persists the given {@link AbstractModel} to the database. This method is
	 * idempotent, meaning if the record already exists, it will not be
	 * effected.
	 * 
	 * @param model
	 *            AbstractModel to persist to the database
	 * @return the row ID of the newly inserted record, or -1 if the insert
	 *         failed
	 */
	long save(AbstractModel model);

	/**
	 * Updates the given (@link AbstractModel} in the database.
	 * 
	 * @param model
	 *            AbstractModel to update in the database
	 * @return the row ID of the effected record, 0 if the record did not exist,
	 *         or -1 if the update failed
	 */
	long update(AbstractModel model);

	/**
	 * Deletes the given {@link AbstractModel} from the database if it exists.
	 * 
	 * @param model
	 *            AbstractModel to delete from the database
	 * @return true if the record was deleted, false otherwise
	 */
	boolean delete(AbstractModel model);

	/**
	 * Persists the given {@link AbstractModel} to the database, or, if it
	 * already exists, updates the record.
	 * 
	 * @param model
	 *            AbstractModel to save or update in the database
	 * @return the row ID of the newly inserted row, 0 if the row was updated,
	 *         or -1 if the query failed
	 */
	long saveOrUpdate(AbstractModel model);

	/**
	 * Persists or updates the entire collection of (@link AbstractModel}
	 * entities in the database.
	 * 
	 * @param models
	 *            AbstractModels to save or update in the database
	 */
	void saveOrUpdateAll(Iterable<? extends AbstractModel> models);

	/**
	 * Persists the entire collection of {@link AbstractModel} entities to the
	 * database.
	 * 
	 * @param models
	 *            AbstractModels to persist to the database
	 */
	void saveAll(Iterable<? extends AbstractModel> models);

	/**
	 * Deletes the entire collection of {@link AbstractModel} entities from the
	 * database if they exist.
	 * 
	 * @param models
	 *            AbstractModels to delete from the database
	 * @return the number of records deleted
	 */
	int deleteAll(Iterable<? extends AbstractModel> models);

}
