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

import java.util.List;

import android.content.Context;

import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;
import com.clarionmedia.infinitum.context.impl.XmlContextFactory;
import com.clarionmedia.infinitum.di.BeanFactory;
import com.clarionmedia.infinitum.orm.Session;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;

/**
 * <p>
 * Acts as a container for framework-wide context information. This should not
 * be instantiated directly but rather obtained through the
 * {@link XmlContextFactory}, which creates an instance of this from
 * {@code infinitum.cfg.xml}. {@code InfinitumContext} describes an
 * application's domain model and how it should be persisted. Entity persistence
 * can be configured using one of two policies: XML map files or annotations.
 * </p>
 * <p>
 * {@code InfinitumContext} is used to retrieve {@link Session} instances for
 * configured data sources. For example, a SQLite {@code Session} would be
 * retrieved by doing the following:
 * </p>
 * 
 * <pre>
 * Session session = context.getSession(DataSource.SQLITE);
 * </pre>
 * 
 * @author Tyler Treat
 * @version 1.0 05/18/12
 * @since 05/18/12
 */
public interface InfinitumContext {

	/**
	 * Represents the entity persistence configuration mode.
	 */
	public static enum ConfigurationMode {
		XML {
			public String toString() {
				return "xml";
			}
		},
		ANNOTATION {
			public String toString() {
				return "annotations";
			}
		}
	}

	/**
	 * Represents the configured data source for a {@link Session}.
	 */
	public static enum DataSource {
		SQLITE, REST
	}

	/**
	 * Must be executed after the {@code InfinitumContext} has been initialized.
	 * 
	 * @param context
	 *            the {@link Context} used for post processing
	 */
	void postProcess(Context context);

	/**
	 * Retrieves a new {@link Session} instance for the configured data source.
	 * 
	 * @param source
	 *            the {@link DataSource} to target
	 * @return new {@code Session} instance
	 * @throws InfinitumConfigurationException
	 *             if the specified {@code DataSource} was not configured
	 */
	Session getSession(DataSource source) throws InfinitumConfigurationException;

	/**
	 * Indicates if debug is enabled or not. If it is enabled, Infinitum will
	 * produce log statements in {@code Logcat}, otherwise it will not produce
	 * any logging. This value is also used by Infinitum's logging framework.
	 * 
	 * @return {@code true} if debug is on, {@code false} if not
	 */
	boolean isDebug();

	/**
	 * Returns the {@link ConfigurationMode} value of this
	 * {@code InfinitumContext}, indicating which style of configuration this
	 * application is using, XML- or annotation-based. An XML configuration
	 * means that domain model mappings are provided through XML mapping files,
	 * while an annotation configuration means that mappings and other
	 * properties are provided in source code using Java annotations.
	 * 
	 * @return {@code ConfigurationMode} for this application
	 */
	ConfigurationMode getConfigurationMode();

	/**
	 * Returns true if there is a SQLite database configured or false if not. If
	 * {@code infinitum.cfg.xml} is missing the {@code sqlite} element, this
	 * will be false.
	 * 
	 * @return {@code true} if a SQLite database is configured or {@code false}
	 *         if not
	 */
	boolean hasSqliteDb();

	/**
	 * Returns the name of the SQLite database for this {@code InfinitumContext}
	 * . This is the name used to construct the database and subsequently open
	 * it.
	 * 
	 * @return the name of the SQLite database for this {@code InfinitumContext}
	 */
	String getSqliteDbName();

	/**
	 * Returns the version number of the SQLite database for this
	 * {@code InfinitumContext}.
	 * 
	 * @return the SQLite database version number
	 */
	int getSqliteDbVersion();

	/**
	 * Returns a {@link List} of all fully-qualified domain model classes
	 * registered with this {@code InfinitumContext}. Domain types are defined
	 * as being persistent entities.
	 * 
	 * @return a {@code List} of all registered domain model classes
	 */
	List<String> getDomainTypes();

	/**
	 * Indicates if the database schema is configured to be automatically
	 * generated by the framework.
	 * 
	 * @return {@code true} if the schema is set to automatically generate,
	 *         {@code false} if not
	 */
	boolean isSchemaGenerated();

	/**
	 * Indicates if autocommit is enabled or disabled.
	 * 
	 * @return {@code true} if autocommit is enabled, {@code false} if not
	 */
	boolean isAutocommit();

	/**
	 * Retrieves the {@link RestfulContext} for this {@code InfinitumContext}.
	 * The {@code RestfulConfiguration} contains configuration settings for the
	 * RESTful client.
	 * 
	 * @return {@code RestfulConfiguration}
	 */
	RestfulContext getRestfulConfiguration();

	/**
	 * Retrieves the Android {@link Context} for this {@code InfinitumContext},
	 * which contains application-wide context information.
	 * 
	 * @return {@code Context}
	 */
	Context getAndroidContext();

	/**
	 * Retrieves the {@link BeanFactory} for this {@code InfinitumContext}. The
	 * {@code BeanContainer} is used to retrieve beans that have been configured
	 * in {@code infinitum.cfg.xml}.
	 * 
	 * @return {@code BeanContainer}
	 */
	BeanFactory getBeanFactory();

	/**
	 * Retrieves a bean with the given name. Beans are configured in
	 * {@code infinitum.cfg.xml}.
	 * 
	 * @param name
	 *            the name of the bean to retrieve
	 * @return a bean instance or {@code null} if no bean has been configured
	 *         with the given name
	 */
	Object getBean(String name);

	/**
	 * Retrieves a bean with the given name and {@link Class}. Beans are
	 * configured in {@code infinitum.cfg.xml}.
	 * 
	 * @param name
	 *            the name of the bean to retrieve
	 * @param clazz
	 *            the type of the bean to retrieve
	 * @return an instance of the bean
	 */
	<T> T getBean(String name, Class<T> clazz);

	/**
	 * Retrieves the application {@link PersistencePolicy}, which is configured
	 * in {@code infinitum.cfg.xml}.
	 * 
	 * @return {@code PersistencePolicy} for this application
	 */
	PersistencePolicy getPersistencePolicy();

	/**
	 * Indicates if component scan is enabled.
	 * 
	 * @return {@code true} if component scan is enabled, {@code false} if not
	 */
	boolean isComponentScanEnabled();

}
