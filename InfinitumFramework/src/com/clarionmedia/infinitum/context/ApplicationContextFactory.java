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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.clarionmedia.infinitum.context.ApplicationContext.ConfigurationMode;
import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;

/**
 * <p>
 * Provides access to an {@link ApplicationContext} singleton. In order for this
 * class to work, an {@code infinitum.cfg.xml} file must be created and
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
	 * Indicates whether or not the {@code ApplicationContext} has been
	 * configured.
	 * 
	 * @return {@code true} if it has been configured, {@code false} if not
	 */
	public static boolean isConfigured() {
		return sConfigured;
	}

	/**
	 * Configures Infinitum with the specified configuration file. Configuration
	 * file must be named {@code infinitum.cfg.xml}. This method must be called
	 * before attempting to retrieve an {@link ApplicationContext}.
	 * 
	 * @param context
	 *            the calling <code>Context</code>
	 * @param configId
	 *            the resource ID for the XML config file
	 * @throws InfinitumConfigurationException
	 *             if the configuration file could not be found or if the file
	 *             could not be parsed
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
	 * {@link ApplicationContextFactory#configure} must be called before using
	 * this method. Otherwise, an {@link InfinitumConfigurationException} will
	 * be thrown.
	 * 
	 * @return the {@code ApplicationContext} singleton
	 * @throws InfinitumConfigurationException
	 *             if {@code configure} was not called
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

			// Parse entire XML config file
			while (event != XmlPullParser.END_DOCUMENT) {
				event = config.getEventType();

				// Parse <infinitum-configuration> node
				if (event == XmlPullParser.START_TAG
						&& config.getName().contentEquals(ApplicationContextConstants.CONFIG_ELEMENT)) {
					hasConfigNode = true;
					config.next();
					event = config.getEventType();

					// Parse until we reach the end of <infinitum-configuration>
					while (event != XmlPullParser.END_DOCUMENT && event != XmlPullParser.END_TAG
							&& !config.getName().contentEquals(ApplicationContextConstants.CONFIG_ELEMENT)) {

						// Parse <application> node
						if (event == XmlPullParser.START_TAG
								&& config.getName().contentEquals(ApplicationContextConstants.APPLICATION_ELEMENT)) {
							config.next();
							event = config.getEventType();

							// Parse until we reach the end of <application>
							while (event != XmlPullParser.END_DOCUMENT && event != XmlPullParser.END_TAG
									&& !config.getName().contentEquals(ApplicationContextConstants.APPLICATION_ELEMENT)) {

								// Parse properties
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
									} else if (name.equalsIgnoreCase(ApplicationContextConstants.MODE_ATTRIBUTE)) {
										String mode = config.getText();
										if (mode.equalsIgnoreCase(ConfigurationMode.Annotation.toString()))
											ret.setConfigurationMode(ConfigurationMode.Annotation);
										else if (mode.equalsIgnoreCase(ConfigurationMode.XML.toString()))
											ret.setConfigurationMode(ConfigurationMode.XML);
									}
									config.next();
									config.next();
									event = config.getEventType();
								}
							}
							config.next();
							event = config.getEventType();
							continue;
						}

						// Parse <sqlite> node
						if (event == XmlPullParser.START_TAG
								&& config.getName().contentEquals(ApplicationContextConstants.SQLITE_ELEMENT)) {
							config.next();
							event = config.getEventType();

							// Parse until we reach the end of <sqlite>
							while (event != XmlPullParser.END_DOCUMENT && event != XmlPullParser.END_TAG
									&& !config.getName().contentEquals(ApplicationContextConstants.SQLITE_ELEMENT)) {
								ret.setHasSqliteDb(true);

								// Parse properties
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
							config.next();
							event = config.getEventType();
							continue;
						}

						// Parse <domain> node
						if (event == XmlPullParser.START_TAG
								&& config.getName().contentEquals(ApplicationContextConstants.DOMAIN_ELEMENT)) {
							config.next();
							event = config.getEventType();

							// Parse until we reach the end of <domain>
							while (event != XmlPullParser.END_DOCUMENT && event != XmlPullParser.END_TAG
									&& !config.getName().contentEquals(ApplicationContextConstants.DOMAIN_ELEMENT)) {

								// Parse models
								if (event == XmlPullParser.START_TAG
										&& config.getName().contentEquals(ApplicationContextConstants.MODEL_ELEMENT)) {
									String resource = config.getAttributeValue(null,
											ApplicationContextConstants.DOMAIN_RESOURCE_ATTRIBUTE);
									ret.addDomainModel(resource);
									config.next();
									config.next();
									event = config.getEventType();
								}
							}
							config.next();
							event = config.getEventType();
							continue;
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
