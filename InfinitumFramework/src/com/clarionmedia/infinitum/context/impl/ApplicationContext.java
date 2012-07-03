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

package com.clarionmedia.infinitum.context.impl;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import com.clarionmedia.infinitum.context.BeanProvider;
import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.context.RestfulConfiguration;
import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;
import com.clarionmedia.infinitum.orm.Session;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.persistence.impl.AnnotationPersistencePolicy;
import com.clarionmedia.infinitum.orm.persistence.impl.XmlPersistencePolicy;
import com.clarionmedia.infinitum.orm.sqlite.impl.SqliteSession;
import com.clarionmedia.infinitum.context.impl.PullParserContextFactory;

/**
 * <p>
 * Implementation of {@link InfinitumContext}. This should not be instantiated
 * directly but rather obtained through the {@link PullParserContextFactory}, which
 * creates an instance of this from {@code infinitum.cfg.xml}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/11/12
 */
@Deprecated
public final class ApplicationContext implements InfinitumContext {

	private static final ConfigurationMode DEFAULT_MODE = ConfigurationMode.Annotation;

	private static PersistencePolicy sPersistencePolicy;

	private boolean mIsDebug;
	private ConfigurationMode mConfigMode;
	private boolean mRecycleCache;
	private String mSqliteDbName;
	private int mSqliteDbVersion;
	private boolean mIsSchemaGenerated;
	private boolean mAutocommit;
	private List<String> mDomainModels;
	private RestfulConfiguration mRestContext;
	private Context mContext;
	private BeanProvider mBeanContainer;

	/**
	 * Constructs a new {@code ApplicationContext}. This constructor should not
	 * be called outside of {@link ContextFactory} as it is generated from
	 * {@code infinitum.cfg.xml}.
	 */
	public ApplicationContext() {
		mConfigMode = DEFAULT_MODE;
		mDomainModels = new ArrayList<String>();
		mIsSchemaGenerated = true;
		mAutocommit = true;
		mRecycleCache = true;
	}

	@Override
	public Session getSession(DataSource source) throws InfinitumConfigurationException {
		switch (source) {
		case Sqlite:
			return new SqliteSession(mContext);
		default:
			throw new InfinitumConfigurationException("Data source not configured.");
		}
	}

	@Override
	public boolean isDebug() {
		return mIsDebug;
	}

	@Override
	public void setDebug(boolean debug) {
		mIsDebug = debug;
	}

	@Override
	public ConfigurationMode getConfigurationMode() {
		return mConfigMode;
	}

	@Override
	public void setConfigurationMode(ConfigurationMode mode) {
		mConfigMode = mode;
	}

	@Override
	public boolean hasSqliteDb() {
		return mSqliteDbName != null;
	}

	@Override
	public String getSqliteDbName() {
		return mSqliteDbName;
	}

	@Override
	public void setSqliteDbName(String dbName) {
		mSqliteDbName = dbName;
	}

	@Override
	public int getSqliteDbVersion() {
		return mSqliteDbVersion;
	}

	@Override
	public void setSqliteDbVersion(int version) {
		mSqliteDbVersion = version;
	}

	@Override
	public List<String> getDomainModels() {
		return mDomainModels;
	}

	@Override
	public void addDomainModel(String domainModel) {
		mDomainModels.add(domainModel);
	}

	@Override
	public void setCacheRecyclable(boolean recycleCache) {
		mRecycleCache = recycleCache;
	}

	@Override
	public boolean isCacheRecyclable() {
		return mRecycleCache;
	}

	@Override
	public void setSchemaGenerated(boolean isSchemaGenerated) {
		mIsSchemaGenerated = isSchemaGenerated;
	}

	@Override
	public boolean isSchemaGenerated() {
		return mIsSchemaGenerated;
	}

	@Override
	public void setAutocommit(boolean autocommit) {
		mAutocommit = autocommit;
	}

	@Override
	public boolean isAutocommit() {
		return mAutocommit;
	}

	@Override
	public RestfulConfiguration getRestfulConfiguration() {
		return mRestContext;
	}

	@Override
	public void setRestfulConfiguration(RestfulConfiguration restContext) {
		mRestContext = restContext;
	}

	@Override
	public void setContext(Context context) {
		mContext = context;
	}

	@Override
	public Context getAndroidContext() {
		return mContext;
	}

	@Override
	public BeanProvider getBeanContainer() {
		return mBeanContainer;
	}

	@Override
	public void setBeanContainer(BeanProvider beanContainer) {
		mBeanContainer = beanContainer;
	}

	@Override
	public Object getBean(String name) {
		return mBeanContainer.loadBean(name);
	}
	
	@Override
	public <T> T getBean(String name, Class<T> clazz) {
		return mBeanContainer.loadBean(name, clazz);
	}

	@Override
	public PersistencePolicy getPersistencePolicy() {
		if (sPersistencePolicy == null) {
			switch (mConfigMode) {
			case Annotation:
				sPersistencePolicy = new AnnotationPersistencePolicy();
				break;
			case Xml:
				sPersistencePolicy = new XmlPersistencePolicy(mContext);
				break;
			}
		}
		return sPersistencePolicy;
	}
}
