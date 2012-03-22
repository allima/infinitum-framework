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

import com.clarionmedia.infinitum.context.InfinitumContext.ConfigurationMode;
import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;

/**
 * <p>
 * Provides access to an {@link InfinitumContext} singleton. In order for this
 * class to function properly, an {@code infinitum.cfg.xml} file must be created
 * and {@link InfinitumContextFactory#configure(Context, int)} must be called
 * using the location of the XML file before accessing the
 * {@code InfinitumContext} or an {@link InfinitumConfigurationException} will
 * be thrown.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/11/12
 */
public class InfinitumContextFactory {

	private static InfinitumContext sInfinitumContext;
	private static boolean sConfigured;
	private static Context sContext;

	/**
	 * Indicates whether or not the {@code InfinitumContext} has been
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
	 * before attempting to retrieve an {@link InfinitumContext}.
	 * 
	 * @param context
	 *            the calling <code>Context</code>
	 * @param configId
	 *            the resource ID for the XML config file
	 * @return configured {@code InfinitumContext}
	 * @throws InfinitumConfigurationException
	 *             if the configuration file could not be found or if the file
	 *             could not be parsed
	 */
	public static InfinitumContext configure(Context context, int configId)
			throws InfinitumConfigurationException {
		sContext = context;
		Resources resources = sContext.getResources();
		XmlResourceParser config = resources.getXml(configId);
		sInfinitumContext = parseXmlConfig(config);
		sConfigured = sInfinitumContext == null ? false : true;
		return sInfinitumContext;
	}

	/**
	 * Retrieves the {@link InfinitumContext} singleton.
	 * {@link InfinitumContextFactory#configure} must be called before using
	 * this method. Otherwise, an {@link InfinitumConfigurationException} will
	 * be thrown.
	 * 
	 * @return the {@code InfinitumContext} singleton
	 * @throws InfinitumConfigurationException
	 *             if {@code configure} was not called
	 */
	public static InfinitumContext getInfinitumContext()
			throws InfinitumConfigurationException {
		if (!sConfigured || sInfinitumContext == null)
			throw new InfinitumConfigurationException(
					InfinitumContextConstants.CONFIG_NOT_CALLED);
		return sInfinitumContext;
	}

	// TODO Refactor this nasty parse method
	private static InfinitumContext parseXmlConfig(XmlResourceParser config)
			throws InfinitumConfigurationException {
		InfinitumContext ret = new InfinitumContext();
		try {
			int event = config.getEventType();
			boolean hasConfigNode = false;

			// Parse entire XML config file
			while (event != XmlPullParser.END_DOCUMENT) {
				event = config.getEventType();

				// Parse <infinitum-configuration> node
				if (event == XmlPullParser.START_TAG
						&& config.getName().contentEquals(
								InfinitumContextConstants.CONFIG_ELEMENT)) {
					hasConfigNode = true;
					config.next();
					event = config.getEventType();

					// Parse until we reach the end of <infinitum-configuration>
					while (event != XmlPullParser.END_DOCUMENT
							&& event != XmlPullParser.END_TAG
							&& !config.getName().contentEquals(
									InfinitumContextConstants.CONFIG_ELEMENT)) {

						// Parse <application> node
						if (event == XmlPullParser.START_TAG
								&& config
										.getName()
										.contentEquals(
												InfinitumContextConstants.APPLICATION_ELEMENT)) {
							config.next();
							event = config.getEventType();

							// Parse until we reach the end of <application>
							while (event != XmlPullParser.END_DOCUMENT
									&& event != XmlPullParser.END_TAG
									&& !config
											.getName()
											.contentEquals(
													InfinitumContextConstants.APPLICATION_ELEMENT)) {

								// Parse properties
								if (event == XmlPullParser.START_TAG
										&& config
												.getName()
												.contentEquals(
														InfinitumContextConstants.PROPERTY_ELEMENT)) {
									String name = config
											.getAttributeValue(
													null,
													InfinitumContextConstants.NAME_ATTRIBUTE);
									config.next();
									event = config.getEventType();
									if (event != XmlPullParser.TEXT)
										throw new InfinitumConfigurationException(
												String.format(
														InfinitumContextConstants.CONFIG_PARSE_ERROR_LINE,
														config.getLineNumber()));
									if (name.equalsIgnoreCase(InfinitumContextConstants.DEBUG_ATTRIBUTE)) {
										String debug = config.getText();
										if (Boolean.valueOf(debug))
											ret.setDebug(true);
										else
											ret.setDebug(false);
									} else if (name
											.equalsIgnoreCase(InfinitumContextConstants.MODE_ATTRIBUTE)) {
										String mode = config.getText();
										if (mode.equalsIgnoreCase(ConfigurationMode.Annotation
												.toString()))
											ret.setConfigurationMode(ConfigurationMode.Annotation);
										else if (mode
												.equalsIgnoreCase(ConfigurationMode.XML
														.toString()))
											ret.setConfigurationMode(ConfigurationMode.XML);
									} else if (name
											.equalsIgnoreCase(InfinitumContextConstants.RECYCLE_ATTRIBUTE)) {
										String recycle = config.getText();
										if (Boolean.valueOf(recycle))
											ret.setCacheRecyclable(true);
										else
											ret.setCacheRecyclable(false);
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
								&& config
										.getName()
										.contentEquals(
												InfinitumContextConstants.SQLITE_ELEMENT)) {
							config.next();
							event = config.getEventType();

							// Parse until we reach the end of <sqlite>
							while (event != XmlPullParser.END_DOCUMENT
									&& event != XmlPullParser.END_TAG
									&& !config
											.getName()
											.contentEquals(
													InfinitumContextConstants.SQLITE_ELEMENT)) {
								ret.setHasSqliteDb(true);

								// Parse properties
								if (event == XmlPullParser.START_TAG && config.getName().contentEquals(InfinitumContextConstants.PROPERTY_ELEMENT)) {
									String name = config.getAttributeValue(null,InfinitumContextConstants.NAME_ATTRIBUTE);
									config.next();
									event = config.getEventType();
									if (event != XmlPullParser.TEXT)
										throw new InfinitumConfigurationException(String.format(InfinitumContextConstants.CONFIG_PARSE_ERROR_LINE, config.getLineNumber()));
									if (name.equalsIgnoreCase(InfinitumContextConstants.DB_NAME_ATTRIBUTE)) {
										String dbName = config.getText();
										if (dbName.trim().equals(""))
											throw new InfinitumConfigurationException(InfinitumContextConstants.CONFIG_PARSE_ERROR + " " + InfinitumContextConstants.SQLITE_DB_NAME_MISSING);
										else
											ret.setSqliteDbName(dbName);
									} else if (name.equalsIgnoreCase(InfinitumContextConstants.DB_VERSION_ATTRIBUTE)) {
										ret.setSqliteDbVersion(Integer.parseInt(config.getText()));
									} else if (name.equalsIgnoreCase(InfinitumContextConstants.DB_GENERATE_SCHEMA_ATTRIBUTE)) {
										String generate = config.getText();
										if (Boolean.valueOf(generate))
											ret.setSchemaGenerated(true);
										else
											ret.setSchemaGenerated(false);
									} else if (name.equalsIgnoreCase(InfinitumContextConstants.DB_AUTOCOMMIT_ATTRIBUTE)) {
										String commit = config.getText();
										if (Boolean.valueOf(commit))
											ret.setAutocommit(true);
										else
											ret.setAutocommit(false);
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
						
						// Parse <rest> node
						if (event == XmlPullParser.START_TAG && config.getName().contentEquals(InfinitumContextConstants.REST_ELEMENT)) {
							config.next();
							event = config.getEventType();

							// Parse until we reach the end of <rest>
							while (event != XmlPullParser.END_DOCUMENT && event != XmlPullParser.END_TAG && !config.getName().contentEquals(InfinitumContextConstants.REST_ELEMENT)) {

								// Parse properties
								if (event == XmlPullParser.START_TAG && config.getName().contentEquals(InfinitumContextConstants.PROPERTY_ELEMENT)) {
									String name = config.getAttributeValue(null,InfinitumContextConstants.NAME_ATTRIBUTE);
									config.next();
									event = config.getEventType();
									if (event != XmlPullParser.TEXT)
										throw new InfinitumConfigurationException(String.format(InfinitumContextConstants.CONFIG_PARSE_ERROR_LINE, config.getLineNumber()));
									if (name.equalsIgnoreCase(InfinitumContextConstants.REST_HOST_ATTRIBUTE)) {
										String host = config.getText();
										if (host.trim().equals(""))
											throw new InfinitumConfigurationException(InfinitumContextConstants.CONFIG_PARSE_ERROR + " " + InfinitumContextConstants.REST_HOST_MISSING);
										ret.setRestHost(host);
									}
									config.next();
									config.next();
									event = config.getEventType();
								}
								
								// Parse <authentication> node
								if (event == XmlPullParser.START_TAG && config.getName().contentEquals(InfinitumContextConstants.AUTHENTICATION_ELEMENT)) {
									ret.setRestAuthenticated(true);
									String name = config.getAttributeValue(null, InfinitumContextConstants.STRATEGY_ATTRIBUTE);
									config.next();
									if (name.equalsIgnoreCase(InfinitumContextConstants.STRATEGY_ATTRIBUTE)) {
										String strat = config.getText();
										if (strat.trim().equals(""))
											throw new InfinitumConfigurationException(InfinitumContextConstants.CONFIG_PARSE_ERROR + " " + InfinitumContextConstants.AUTH_STRAT_MISSING);
										ret.setAuthStrategy(strat);
										}
									config.next();
									if (ret.getAuthStrategy() == null)
										throw new InfinitumConfigurationException(InfinitumContextConstants.CONFIG_PARSE_ERROR + " " + InfinitumContextConstants.AUTH_STRAT_MISSING);
									// Parse until we reach the end of <authentication>
									while (event != XmlPullParser.END_DOCUMENT && event != XmlPullParser.END_TAG && !config.getName().contentEquals(InfinitumContextConstants.AUTHENTICATION_ELEMENT)) {
										
										// TODO Parse authentication properties
										
									}
								}
							}
							config.next();
							event = config.getEventType();
							continue;
						}

						// Parse <domain> node
						if (event == XmlPullParser.START_TAG
								&& config
										.getName()
										.contentEquals(
												InfinitumContextConstants.DOMAIN_ELEMENT)) {
							config.next();
							event = config.getEventType();

							// Parse until we reach the end of <domain>
							while (event != XmlPullParser.END_DOCUMENT
									&& event != XmlPullParser.END_TAG
									&& !config
											.getName()
											.contentEquals(
													InfinitumContextConstants.DOMAIN_ELEMENT)) {

								// Parse models
								if (event == XmlPullParser.START_TAG
										&& config
												.getName()
												.contentEquals(
														InfinitumContextConstants.MODEL_ELEMENT)) {
									String resource = config
											.getAttributeValue(
													null,
													InfinitumContextConstants.DOMAIN_RESOURCE_ATTRIBUTE);
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
				throw new InfinitumConfigurationException(
						InfinitumContextConstants.CONFIG_PARSE_ERROR);
		} catch (XmlPullParserException e) {
			throw new InfinitumConfigurationException(
					InfinitumContextConstants.CONFIG_PARSE_ERROR);
		} catch (IOException e) {
			throw new InfinitumConfigurationException(
					InfinitumContextConstants.CONFIG_PARSE_ERROR);
		} catch (NumberFormatException e) {
			throw new InfinitumConfigurationException(String.format(
					InfinitumContextConstants.CONFIG_PARSE_ERROR_LINE,
					config.getLineNumber()));
		} catch (Exception e) {
			throw new InfinitumConfigurationException(
					InfinitumContextConstants.CONFIG_PARSE_ERROR);
		}
		return ret;
	}

}
