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
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Root;

import com.clarionmedia.infinitum.aop.AspectComponent;
import com.clarionmedia.infinitum.context.AbstractContext;
import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.context.RestfulContext;
import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;
import com.clarionmedia.infinitum.di.BeanComponent;

/**
 * <p>
 * Implementation of {@link InfinitumContext}. This should not be instantiated
 * directly but rather obtained through the {@link XmlContextFactory}, which
 * creates an instance of this from {@code infinitum.cfg.xml}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 06/26/12
 * @since 06/26/12
 */
@Root(name = "infinitum-configuration")
public class XmlApplicationContext extends AbstractContext {

	@ElementMap(name = "application", entry = "property", key = "name", attribute = true, required = false)
	private Map<String, String> mAppConfig;

	@ElementMap(name = "sqlite", entry = "property", key = "name", attribute = true, required = false)
	private Map<String, String> mSqliteConfig;

	@ElementList(name = "domain")
	private List<Model> mModels;

	@Element(name = "rest", required = false, type = XmlRestfulContext.class)
	private RestfulContext mRestConfig;

	@Element(name = "beans", required = false)
	private BeanContainer mBeanContainer;

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
	public RestfulContext getRestfulConfiguration() {
		return mRestConfig;
	}

	@Override
	public void setRestfulConfiguration(RestfulContext restContext) {
		mRestConfig = restContext;
	}

	@Override
	protected List<BeanComponent> getBeans() {
		List<BeanComponent> ret = new ArrayList<BeanComponent>();
		if (mBeanContainer.mBeans != null)
			ret.addAll(mBeanContainer.mBeans);
		if (mBeanContainer.mAspects != null)
			ret.addAll(mBeanContainer.mAspects);
		return ret;
	}

	@Override
	protected List<AspectComponent> getAspects() {
		List<AspectComponent> ret = new ArrayList<AspectComponent>();
		if (mBeanContainer.mAspects != null)
			ret.addAll(mBeanContainer.mAspects);
		return ret;
	}

	@Override
	protected RestfulContext getRestContext() {
		return mRestConfig;
	}

	@Override
	public void setComponentScanPackages(String packages) {
		if (mBeanContainer.mComponentScan == null)
			mBeanContainer.mComponentScan = new BeanContainer.ComponentScan();
		mBeanContainer.mComponentScan.mBasePackages = packages;
	}

	@Override
	protected List<String> getScanPackages() {
		if (mBeanContainer.mComponentScan == null)
			return new ArrayList<String>();
		return mBeanContainer.mComponentScan.getBasePackages();
	}

	@Override
	public void setComponentScanEnabled(boolean componentScan) {
		if (mBeanContainer.mComponentScan == null)
			mBeanContainer.mComponentScan = new BeanContainer.ComponentScan();
		mBeanContainer.mComponentScan.mIsEnabled = componentScan;
	}

	@Override
	public boolean isComponentScanEnabled() {
		if (mBeanContainer.mComponentScan == null)
			return false;
		return mBeanContainer.mComponentScan.mIsEnabled;
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
	private static class BeanContainer {

		@ElementList(entry = "bean", inline = true, required = false)
		private List<BeanComponent> mBeans;

		@ElementList(entry = "aspect", inline = true, required = false)
		private List<AspectComponent> mAspects;

		@Element(name = "component-scan", required = false)
		private ComponentScan mComponentScan;

		@Root
		private static class ComponentScan {

			@Attribute(name = "enabled", required = false)
			private boolean mIsEnabled = true;

			@Attribute(name = "base-package", required = false)
			private String mBasePackages;

			public List<String> getBasePackages() {
				List<String> packages = new ArrayList<String>(asList(mBasePackages.split(",")));
				Iterator<String> iter = packages.iterator();
				while (iter.hasNext()) {
					String pkg = iter.next().trim();
					if (pkg.length() == 0)
						iter.remove();
				}
				packages.add("com.clarionmedia.infinitum.internal");
				return packages;
			}

		}

	}

}
