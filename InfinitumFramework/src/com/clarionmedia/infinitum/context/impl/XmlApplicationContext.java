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

import static java.lang.Boolean.parseBoolean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Commit;
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

/**
 * <p>
 * Implementation of {@link InfinitumContext}. This should not be instantiated
 * directly but rather obtained through the {@link SimpleXmlContextFactory},
 * which creates an instance of this from {@code infinitum.cfg.xml}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 06/26/12
 */
@Root(name = "infinitum-configuration")
public class XmlApplicationContext implements InfinitumContext {
	
	private static PersistencePolicy sPersistencePolicy;

	@ElementMap(name = "application", entry = "property", key = "name", attribute = true, required = false)
	private Map<String, String> mAppConfig;
	
	@ElementMap(name = "sqlite", entry = "property", key = "name", attribute = true, required = false)
	private Map<String, String> mSqliteConfig;
	
	@ElementList(name = "domain")
	private List<Model> mModels;
	
	@ElementList(name = "beans")
	private List<Bean> mBeans;
	private BeanProvider mBeanProvider;
	
	@Element(name = "rest", required = false, type = RestfulContext.class)
	private RestfulConfiguration mRestConfig;
	
	private Context mContext;

	@Override
	public Session getSession(DataSource source)
			throws InfinitumConfigurationException {
		switch (source) {
		case Sqlite:
			return new SqliteSession(mContext);
		default:
			throw new InfinitumConfigurationException("Data source not configured.");
		}
	}

	@Override
	public boolean isDebug() {
		String debug = mAppConfig.get("debug");
		if (debug == null)
			return false;
		return parseBoolean(debug);
	}

	@Override
	public void setDebug(boolean debug) {
		mAppConfig.put("debug", Boolean.toString(debug));
	}

	@Override
	public ConfigurationMode getConfigurationMode() {
		String mode = mAppConfig.get("mode");
		if (mode == null)
			return ConfigurationMode.Annotation;
		if (mode.equalsIgnoreCase(ConfigurationMode.Xml.toString()))
			return ConfigurationMode.Xml;
		else if (mode.equalsIgnoreCase(ConfigurationMode.Annotation.toString()))
			return ConfigurationMode.Annotation;
		throw new InfinitumConfigurationException("Unknown configuration mode '" + mode + "'.");
	}

	@Override
	public void setConfigurationMode(ConfigurationMode mode) {
		mAppConfig.put("mode", mode.toString());
	}

	@Override
	public boolean hasSqliteDb() {
		return mSqliteConfig != null;
	}

	@Override
	public String getSqliteDbName() {
		String dbName = mSqliteConfig.get("dbName");
		if (dbName == null || dbName.length() == 0)
			throw new InfinitumConfigurationException("SQLite database name not specified.");
		return dbName;
	}

	@Override
	public void setSqliteDbName(String dbName) {
		mSqliteConfig.put("dbName", dbName);
	}

	@Override
	public int getSqliteDbVersion() {
		String dbVersion = mSqliteConfig.get("dbVersion");
		if (dbVersion == null)
			throw new InfinitumConfigurationException("SQLite database version not specified.");
		return Integer.parseInt(dbVersion);
	}

	@Override
	public void setSqliteDbVersion(int version) {
		mSqliteConfig.put("dbVersion", Integer.toString(version));
	}

	@Override
	public List<String> getDomainModels() {
		List<String> models = new ArrayList<String>();
		for (Model model : mModels) {
			models.add(model.getResource());
		}
		return models;
	}

	@Override
	public void addDomainModel(String domainModel) {
		Model model = new Model();
		model.setResource(domainModel);
	}

	@Override
	public void setCacheRecyclable(boolean recycleCache) {
		mAppConfig.put("recycleCache", Boolean.toString(recycleCache));
	}

	@Override
	public boolean isCacheRecyclable() {
		String recycleCache = mAppConfig.get("recycleCache");
		if (recycleCache == null)
			return true;
		return Boolean.parseBoolean(recycleCache);
	}

	@Override
	public void setSchemaGenerated(boolean isSchemaGenerated) {
		mSqliteConfig.put("generateSchema", Boolean.toString(isSchemaGenerated));
	}

	@Override
	public boolean isSchemaGenerated() {
		String isGenerated = mSqliteConfig.get("generateSchema");
		if (isGenerated == null)
			return true;
		return parseBoolean(isGenerated);
	}

	@Override
	public void setAutocommit(boolean autocommit) {
		mSqliteConfig.put("autocommit", Boolean.toString(autocommit));
	}

	@Override
	public boolean isAutocommit() {
		String autocommit = mSqliteConfig.get("autocommit");
		if (autocommit == null)
			return true;
		return parseBoolean(autocommit);
	}

	@Override
	public RestfulConfiguration getRestfulConfiguration() {
		return mRestConfig;
	}

	@Override
	public void setRestfulConfiguration(RestfulConfiguration restContext) {
		mRestConfig = restContext;
	}

	@Override
	public Context getAndroidContext() {
		return mContext;
	}

	@Override
	public void setContext(Context context) {
		mContext = context;
	}

	@Override
	public BeanProvider getBeanContainer() {
		return mBeanProvider;
	}

	@Override
	public void setBeanContainer(BeanProvider beanContainer) {
		mBeanProvider = beanContainer;
	}

	@Override
	public Object getBean(String name) {
		return mBeanProvider.loadBean(name);
	}

	@Override
	public <T> T getBean(String name, Class<T> clazz) {
		return mBeanProvider.loadBean(name, clazz);
	}

	@Override
	public PersistencePolicy getPersistencePolicy() {
		if (sPersistencePolicy == null) {
			switch (getConfigurationMode()) {
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
	
	@SuppressWarnings("unused")
	@Commit
	private void postProcess() {
		mRestConfig.setParentContext(this);
		initializeBeanProvider();
	}
	
	private void initializeBeanProvider() {
		mBeanProvider = new BeanFactory();
		for (Bean bean : mBeans) {
			Map<String, Object> propertiesMap = new HashMap<String, Object>();
			for (Bean.Property property : bean.getProperties()) {
				String name = property.getName();
				String ref = property.getRef();
				if (ref != null) {
					propertiesMap.put(name, mBeanProvider.loadBean(ref));
				} else {
					String value = property.getValue();
					propertiesMap.put(name, value);
				}
			}
			mBeanProvider.registerBean(bean.getId(), bean.getClassName(), propertiesMap);
		}
	}
	
	@Root
	private static class Model {
		
		@Attribute(name = "resource")
		private String mResource;

		public String getResource() {
			return mResource;
		}
		
		public void setResource(String resource) {
			mResource = resource;
		}
		
	}
	
	@Root
	private static class Bean {
		
		@Attribute(name = "id")
		private String mId;
		
		@Attribute(name = "src")
		private String mClass;
		
		@ElementList(required = false, entry = "property", inline = true)
		private List<Property> mProperties;
		
		@SuppressWarnings("unused")
		public Bean() {
			mProperties = new ArrayList<Property>();
		}

		public String getId() {
			return mId;
		}

		public String getClassName() {
			return mClass;
		}

		public List<Property> getProperties() {
			return mProperties;
		}
		
		@Root
		private static class Property {
			
			@Attribute(name = "name")
			private String mName;
			
			@Attribute(name = "value", required = false)
			private String mValue;
			
			@Attribute(name = "ref", required = false)
			private String mRef;

			public String getName() {
				return mName;
			}

			public String getValue() {
				return mValue;
			}

			public String getRef() {
				return mRef;
			}
			
		}
	}

}
