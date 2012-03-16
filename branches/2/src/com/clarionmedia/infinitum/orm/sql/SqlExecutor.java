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

package com.clarionmedia.infinitum.orm.sql;

import com.clarionmedia.infinitum.orm.ResultSet;

/**
 * Provides an API for executing SQL statements against a database.
 * 
 * @author Tyler Treat
 * @version 1.0 02/24/12
 */
public interface SqlExecutor {

	/**
	 * Opens the database to begin a transaction. If the database does not
	 * exist, it will be created.
	 */
	void open();

	/**
	 * Closes the database connection.
	 */
	void close();

	/**
	 * Executes the given SQL query and returns a {@link ResultSet} for it.
	 * 
	 * @param sql
	 *            the SQL to execute
	 * @return the result of the query
	 */
	ResultSet execute(String sql);

}
