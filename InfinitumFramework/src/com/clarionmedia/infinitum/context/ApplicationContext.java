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

	private boolean mIsDebug;
	private boolean mHasSqliteDb;
	private String mSqliteDbName;
	private int mSqliteDbVersion;

	/**
	 * Indicates if debug is enabled or not. If it is enabled, Infinitum will
	 * produce log statements in <code>Logcat</code>, otherwise it will not
	 * produce any logging.
	 * 
	 * @return <code>true</code> if debug is on, <code>false</code> if not
	 */
	public boolean isDebug() {
		return mIsDebug;
	}

	/**
	 * Sets the debug value indicating if logging is enabled or not. If it is
	 * enabled, Infinitum will produce log statements in <code>Logcat</code>,
	 * otherwise it will not produce any logging. This should be set to
	 * <code>false</code> in a production environment. The debug value can be
	 * enabled in <code>infinitum.cfg.xml</code> with
	 * <code>&lt;property name="debug"&gt;true&lt;/property&gt;</code> and
	 * disabled with
	 * <code>&lt;property name="debug"&gt;false&lt;/property&gt;</code>.
	 * 
	 * @param debug
	 *            <code>true</code> if debug is to be enabled,
	 *            <code>false</code> if it's to be disabled
	 */
	public void setDebug(boolean debug) {
		mIsDebug = debug;
	}

	/**
	 * Returns true if there is a SQLite database configured or false if not. If
	 * infinitum.cfg.xml is missing the <code>sqlite</code> element, this will
	 * be false.
	 * 
	 * @return <code>true</code> if SQLite database is configured or
	 *         <code>false</code> if not
	 */
	public boolean hasSqliteDb() {
		return mHasSqliteDb;
	}

	/**
	 * Sets the value indicating if this <code>ApplicationContext</code> has a
	 * SQLite database configured or not. This will be set to <code>true</code>
	 * if <code>infinitum.cfg.xml</code> has a properly configured
	 * <code>sqlite</code> element.
	 * 
	 * @param hasSqliteDb
	 *            <code>true</code> if there is a database, <code>false</code>
	 *            if not
	 */
	public void setHasSqliteDb(boolean hasSqliteDb) {
		mHasSqliteDb = hasSqliteDb;
	}

	/**
	 * Returns the name of the SQLite database for this
	 * <code>ApplicationContext</code>. This is the name used to construct the
	 * database and subsequently open it.
	 * 
	 * @return the name of the SQLite database for this
	 *         <code>ApplicationContext</code>
	 */
	public String getSqliteDbName() {
		return mSqliteDbName;
	}

	/**
	 * Sets the value of the SQLite database name for this
	 * <code>ApplicationContext</code>. The SQLite database name can be
	 * specified in <code>infinitum.cfg.xml</code> with
	 * <code>&lt;property name="dbName"&gt;MyDB&lt;/property&gt;</code> in the
	 * <code>sqlite</code> element.
	 * 
	 * @param dbName
	 *            the name of the SQLite database
	 */
	public void setSqliteDbName(String dbName) {
		mSqliteDbName = dbName;
	}

	/**
	 * Returns the version number of the SQLite database for this
	 * <code>ApplicationContext</code>.
	 * 
	 * @return the SQLite database version number
	 */
	public int getSqliteDbVersion() {
		return mSqliteDbVersion;
	}

	/**
	 * Sets the version for the SQLite database for this
	 * <code>ApplicationContext</code>. The SQLite database version can be
	 * specified in <code>infinitum.cfg.xml</code> with
	 * <code>&lt;property name="dbVersion"&gt;2&lt;/property&gt;</code> in the
	 * <code>sqlite</code> element.
	 * 
	 * @param version
	 *            the version to set for the SQLite database
	 */
	public void setSqliteDbVersion(int version) {
		mSqliteDbVersion = version;
	}

}
