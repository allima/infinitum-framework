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

package com.clarionmedia.infinitum.context;

/**
 * <p>
 * Acts as a container for application-wide context information. This should not
 * be instantiated directly but rather obtained through the
 * {@link ApplicationContextFactory}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/11/12
 */
public class ApplicationContext {

	private String mSqliteDbName;
	private int mSqliteDbVersion;

	/**
	 * Returns the name of the SQLite database for this ApplicationContext or
	 * null if there is none. The SQLite database name can be specified in
	 * infinitum.cfg.xml. It's the name used to construct the database and
	 * subsequently connect to it.
	 * 
	 * @return the name of the SQLite database for this ApplicationContext
	 */
	public String getSqliteDbName() {
		return mSqliteDbName;
	}

	/**
	 * Sets the value of the SQLite database name for this ApplicationContext.
	 * Null can be used to specify there is no database.
	 * 
	 * @param dbName
	 *            the name of the SQLite database
	 */
	public void setSqliteDbName(String dbName) {
		mSqliteDbName = dbName;
	}

	/**
	 * Returns the version number of the SQLite database for this
	 * ApplicationContext. The SQLite database version can be specified in
	 * infinitum.cfg.xml.
	 * 
	 * @return the SQLite database version number
	 */
	public int getSqliteDbVersion() {
		return mSqliteDbVersion;
	}

	/**
	 * Sets the version for the SQLite database for this ApplicationContext.
	 * 
	 * @param version
	 *            the version to set for the SQLite database
	 */
	public void setSqliteDbVersion(int version) {
		mSqliteDbVersion = version;
	}

}
