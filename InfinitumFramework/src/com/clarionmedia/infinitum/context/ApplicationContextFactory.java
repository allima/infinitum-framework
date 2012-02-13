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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.clarionmedia.infinitum.orm.Constants.PersistenceMode;
import com.clarionmedia.infinitum.orm.annotation.Persistence;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;

/**
 * <p>
 * Provides access to an {@link ApplicationContext} singleton. In order for this
 * class to work, an infinitum.cfg.xml file must be created and
 * <code>configure</code> must be called using the location of the XML file
 * before accessing the <code>ApplicationContext</code> or an
 * {@link InfinitumConfigurationException} will be thrown.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/11/12
 */
public class ApplicationContextFactory {

	private static ApplicationContext sApplicationContext;
	private static boolean sConfigured;
	private static Context sContext;

	/**
	 * Indicates whether or not the <code>ApplicationContext</code> has been
	 * configured.
	 * 
	 * @return true if it has been configured, false if not
	 */
	public static boolean isConfigured() {
		return sConfigured;
	}

	/**
	 * Configures Infinitum with the specified configuration file. Configuration
	 * file must be named infinitum.cfg.xml. This method must be called before
	 * attempting to retrieve an {@link ApplicationContext}.
	 * 
	 * @param context
	 *            the calling <code>Context</code>
	 * @param configId
	 *            the resource ID for the XML config file
	 * @throws InfinitumConfigurationException
	 *             thrown if the configuration file could not be found or if the
	 *             file could not be parsed
	 */
	public static void configure(Context context, int configId) throws InfinitumConfigurationException {
		sContext = context;
		Resources resources = sContext.getResources();
		XmlResourceParser config = resources.getXml(configId);
		sApplicationContext = parseXmlConfig(config);
		sConfigured = sApplicationContext == null ? false : true;
	}

	/**
	 * Retrieves the {@link ApplicationContext} singleton.
	 * <code>configure</code> must be called before using this method.
	 * Otherwise, an {@link InfinitumConfigurationException} will be thrown.
	 * 
	 * @return the ApplicationContext singleton
	 * @throws InfinitumConfigurationException
	 *             thrown if <code>configure</code> was not called
	 */
	public static ApplicationContext getApplicationContext() throws InfinitumConfigurationException {
		if (!sConfigured || sApplicationContext == null)
			throw new InfinitumConfigurationException(ApplicationContextConstants.CONFIG_NOT_CALLED);
		return sApplicationContext;
	}

	private static ApplicationContext parseXmlConfig(XmlResourceParser config) throws InfinitumConfigurationException {
		ApplicationContext ret = new ApplicationContext();
		try {
			int event = config.getEventType();
			boolean hasConfigNode = false;
			while (event != XmlPullParser.END_DOCUMENT) {
				event = config.getEventType();
				if (event == XmlPullParser.START_TAG
						&& config.getName().contentEquals(ApplicationContextConstants.CONFIG_ELEMENT)) {
					hasConfigNode = true;
					config.next();
					event = config.getEventType();
					while (event != XmlPullParser.END_DOCUMENT && event != XmlPullParser.END_TAG
							&& !config.getName().contentEquals(ApplicationContextConstants.CONFIG_ELEMENT)) {
						if (event == XmlPullParser.START_TAG
								&& config.getName().contentEquals(ApplicationContextConstants.APPLICATION_ELEMENT)) {
							config.next();
							event = config.getEventType();
							while (event != XmlPullParser.END_DOCUMENT && event != XmlPullParser.END_TAG
									&& !config.getName().contentEquals(ApplicationContextConstants.APPLICATION_ELEMENT)) {
								if (event == XmlPullParser.START_TAG
										&& config.getName().contentEquals(ApplicationContextConstants.PROPERTY_ELEMENT)) {
									String name = config.getAttributeValue(null,
											ApplicationContextConstants.NAME_ATTRIBUTE);
									config.next();
									event = config.getEventType();
									if (event != XmlPullParser.TEXT)
										throw new InfinitumConfigurationException(String.format(
												ApplicationContextConstants.CONFIG_PARSE_ERROR_LINE,
												config.getLineNumber()));
									if (name.equalsIgnoreCase(ApplicationContextConstants.DEBUG_ATTRIBUTE)) {
										String debug = config.getText();
										if (Boolean.valueOf(debug))
											ret.setDebug(true);
										else
											ret.setDebug(false);
									}
									config.next();
									config.next();
									event = config.getEventType();
								}
							}
						}
						if (event == XmlPullParser.START_TAG
								&& config.getName().contentEquals(ApplicationContextConstants.SQLITE_ELEMENT)) {
							config.next();
							event = config.getEventType();
							while (event != XmlPullParser.END_DOCUMENT && event != XmlPullParser.END_TAG
									&& !config.getName().contentEquals(ApplicationContextConstants.SQLITE_ELEMENT)) {
								ret.setHasSqliteDb(true);
								if (event == XmlPullParser.START_TAG
										&& config.getName().contentEquals(ApplicationContextConstants.PROPERTY_ELEMENT)) {
									String name = config.getAttributeValue(null,
											ApplicationContextConstants.NAME_ATTRIBUTE);
									config.next();
									event = config.getEventType();
									if (event != XmlPullParser.TEXT)
										throw new InfinitumConfigurationException(String.format(
												ApplicationContextConstants.CONFIG_PARSE_ERROR_LINE,
												config.getLineNumber()));
									if (name.equalsIgnoreCase(ApplicationContextConstants.DB_NAME_ATTRIBUTE)) {
										String dbName = config.getText();
										if (dbName.trim().equals(""))
											throw new InfinitumConfigurationException(
													ApplicationContextConstants.CONFIG_PARSE_ERROR + " "
															+ ApplicationContextConstants.SQLITE_DB_NAME_MISSING);
										else
											ret.setSqliteDbName(dbName);
									} else if (name.equalsIgnoreCase(ApplicationContextConstants.DB_VERSION_ATTRIBUTE))
										ret.setSqliteDbVersion(Integer.parseInt(config.getText()));
									config.next();
									config.next();
									event = config.getEventType();
								}
							}
						}
					}
				}
				config.next();
				event = config.getEventType();
			}
			config.close();
			if (!hasConfigNode)
				throw new InfinitumConfigurationException(ApplicationContextConstants.CONFIG_PARSE_ERROR);
		} catch (XmlPullParserException e) {
			throw new InfinitumConfigurationException(ApplicationContextConstants.CONFIG_PARSE_ERROR);
		} catch (IOException e) {
			throw new InfinitumConfigurationException(ApplicationContextConstants.CONFIG_PARSE_ERROR);
		} catch (NumberFormatException e) {
			throw new InfinitumConfigurationException(String.format(
					ApplicationContextConstants.CONFIG_PARSE_ERROR_LINE, config.getLineNumber()));
		} catch (Exception e) {
			throw new InfinitumConfigurationException(ApplicationContextConstants.CONFIG_PARSE_ERROR);
		}
		return ret;
	}

}
