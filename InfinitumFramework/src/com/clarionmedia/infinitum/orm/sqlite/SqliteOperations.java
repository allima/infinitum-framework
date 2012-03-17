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

import android.database.Cursor;
import android.database.SQLException;

import com.clarionmedia.infinitum.orm.DatastoreOperations;
import com.clarionmedia.infinitum.orm.exception.SQLGrammarException;

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
public interface SqliteOperations extends DatastoreOperations {

	/**
	 * Opens the database for transactions.
	 * 
	 * @throws SQLException
	 *             if the database cannot be opened
	 */
	void open() throws SQLException;

	/**
	 * Closes the database.
	 */
	void close();

	/**
	 * Indicates if the database is open.
	 * 
	 * @return {@code true} if the database is open, {@code false} if not
	 */
	boolean isOpen();

	/**
	 * Executes the given SQL query on the database for a result.
	 * 
	 * @param sql
	 *            the SQL query to execute
	 * @return {@link Cursor} containing the results of the query
	 * @throws SQLGrammarException
	 *             if the SQL was formatted incorrectly
	 */
	Cursor executeForResult(String sql) throws SQLGrammarException;

}