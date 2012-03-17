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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;
import com.clarionmedia.infinitum.orm.Session;
import com.clarionmedia.infinitum.orm.sqlite.SqliteSession;

/**
 * <p>
 * Acts as a container for application-wide context information. This should not
 * be instantiated directly but rather obtained through the
 * {@link InfinitumContextFactory}, which creates an instance of this from
 * {@code infinitum.cfg.xml}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/11/12
 */
public class InfinitumContext {

	public static enum ConfigurationMode {
		XML, Annotation
	}

	public static enum DataSource {
		Sqlite
	}

	private static final ConfigurationMode DEFAULT_MODE = ConfigurationMode.Annotation;

	private boolean mIsDebug;
	private ConfigurationMode mConfigMode;
	private boolean mRecycleCache;
	private boolean mHasSqliteDb;
	private String mSqliteDbName;
	private int mSqliteDbVersion;
	private List<String> mDomainModels;

	/**
	 * Constructs a new {@code InfinitumContext}. This constructor should
	 * not be called outside of {@link InfinitumContextFactory} as it is
	 * generated from {@code infinitum.cfg.xml}.
	 */
	public InfinitumContext() {
		mConfigMode = DEFAULT_MODE;
		mDomainModels = new ArrayList<String>();
	}

	/**
	 * Retrieves a new {@link Session} instance for the configured data source.
	 * 
	 * @param context
	 *            the {@link Context} of the {@code Session}
	 * @param source
	 *            the {@link DataSource} to target
	 * @return new {@code Session} instance
	 * @throws InfinitumConfigurationException
	 *             if the specified {@code DataSource} was not configured
	 */
	public Session getSession(Context context, DataSource source)
			throws InfinitumConfigurationException {
		switch (source) {
		case Sqlite:
			return new SqliteSession(context);
		default:
			throw new InfinitumConfigurationException(
					"Data source not configured.");
		}
	}

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
	 * Returns the <code>ConfigurationMode</code> value of this
	 * {@code InfinitumContext}, indicating which style of configuration
	 * this application is using, XML- or annotation-based. An XML configuration
	 * means that domain model mappings are provided through XML mapping files,
	 * while an annotation configuration means that mappings and other
	 * properties are provided in source code using Java annotations.
	 * 
	 * @return <code>ConfigurationMode</code> for this application
	 */
	public ConfigurationMode getConfigurationMode() {
		return mConfigMode;
	}

	/**
	 * Sets the <code>ConfigurationMode</code> value for this
	 * {@code InfinitumContext}. The mode can be set in
	 * <code>infinitum.cfg.xml</code> with
	 * <code>&lt;property name="mode"&gt;xml&lt;/property&gt;</code> or
	 * <code>&lt;property name="mode"&gt;annotations&lt;/property&gt;</code> in
	 * the <code>application</code> element. An XML configuration means that
	 * domain model mappings are provided through XML mapping files, while an
	 * annotation configuration means that mappings and other properties are
	 * provided in source code using Java annotations. If annotations are used,
	 * all domain model classes must be stored in the same package. If a mode
	 * property is not provided in <code>infinitum.cfg.xml</code>, annotations
	 * will be used by default.
	 * 
	 * @param mode
	 */
	public void setConfigurationMode(ConfigurationMode mode) {
		mConfigMode = mode;
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
	 * Sets the value indicating if this {@code InfinitumContext} has a
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
	 * {@code InfinitumContext}. This is the name used to construct the
	 * database and subsequently open it.
	 * 
	 * @return the name of the SQLite database for this
	 *         {@code InfinitumContext}
	 */
	public String getSqliteDbName() {
		return mSqliteDbName;
	}

	/**
	 * Sets the value of the SQLite database name for this
	 * {@code InfinitumContext}. The SQLite database name can be
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
	 * {@code InfinitumContext}.
	 * 
	 * @return the SQLite database version number
	 */
	public int getSqliteDbVersion() {
		return mSqliteDbVersion;
	}

	/**
	 * Sets the version for the SQLite database for this
	 * {@code InfinitumContext}. The SQLite database version can be
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

	/**
	 * Returns a <code>List</code> of all fully-qualified domain model classes
	 * registered with this {@code InfinitumContext}. Domain models are
	 * defined as being persistent entities.
	 * 
	 * @return a <code>List</code> of all registered domain model classes
	 */
	public List<String> getDomainModels() {
		return mDomainModels;
	}

	/**
	 * Adds the specified domain model class to the entire collection of domain
	 * models registered with this {@code InfinitumContext}. Domain
	 * models are defined in <code>infinitum.cfg.xml</code> using
	 * <code>&lt;model resource="com.foo.domain.MyModel" /&gt;</code> in the
	 * <code>domain</code> element.
	 * 
	 * @param domainModel
	 */
	public void addDomainModel(String domainModel) {
		mDomainModels.add(domainModel);
	}

	/**
	 * Sets the value indicating if the {@link Session} cache should be
	 * automatically recycled. This value can be enabled in
	 * {@code infinitum.cfg.xml} with
	 * <code>&lt;property name="recycleCache"&gt;true&lt;/property&gt;</code> in
	 * the {@code application} element.
	 * 
	 * @param recycleCache
	 *            {@code true} if the cache should be recycled automatically,
	 *            {@code false} if not
	 */
	public void setCacheRecyclable(boolean recycleCache) {
		mRecycleCache = recycleCache;
	}

	/**
	 * Indicates if the {@link Session} cache is configured to be automatically
	 * recycled or not.
	 * 
	 * @return {@code true} if the cache is set to automatically recycle,
	 *         {@code false} if not
	 */
	public boolean isCacheRecyclable() {
		return mRecycleCache;
	}

}
