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
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;

import com.clarionmedia.infinitum.context.InfinitumContext.ConfigurationMode;
import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;

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
public abstract class InfinitumContextFactory {

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
	 *            the calling {@code Context}
	 * @param configId
	 *            the resource ID for the XML config file
	 * @return configured {@code InfinitumContext}
	 * @throws InfinitumConfigurationException
	 *             if the configuration file could not be found or if the file
	 *             could not be parsed
	 */
	public InfinitumContext configure(Context context, int configId) throws InfinitumConfigurationException {
		sContext = context;
		Resources resources = sContext.getResources();
		XmlResourceParser config = resources.getXml(configId);
		sInfinitumContext = configureFromXml(config);
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
	public static InfinitumContext getInfinitumContext() throws InfinitumConfigurationException {
		if (!sConfigured || sInfinitumContext == null)
			throw new InfinitumConfigurationException(InfinitumContextConstants.CONFIG_NOT_CALLED);
		return sInfinitumContext;
	}
	
	private InfinitumContext configureFromXml(XmlResourceParser parser) throws InfinitumConfigurationException {
		InfinitumContext ret = new InfinitumContext();
		try {
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String name = null;
				switch (eventType) {
				case XmlPullParser.START_TAG:
					name = parser.getName();
					if (name.equalsIgnoreCase(InfinitumContextConstants.APPLICATION_ELEMENT))
						configureApplication(parser, ret);
					else if (name.equalsIgnoreCase(InfinitumContextConstants.SQLITE_ELEMENT))
						configureSqlite(parser, ret);
					else if (name.equalsIgnoreCase(InfinitumContextConstants.REST_ELEMENT))
						configureRest(parser, ret);
					else if (name.equalsIgnoreCase(InfinitumContextConstants.DOMAIN_ELEMENT))
						configureDomain(parser, ret);
					break;
				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
		    throw new InfinitumConfigurationException(InfinitumContextConstants.CONFIG_PARSE_ERROR);
		} catch (IOException e) {
			throw new InfinitumConfigurationException(InfinitumContextConstants.CONFIG_PARSE_ERROR);
		} finally {
			parser.close();
		}
		return ret;
	}
	
	private void configureApplication(XmlResourceParser parser, InfinitumContext ctx) throws XmlPullParserException, IOException {
		int eventType = parser.getEventType();
		while (eventType != XmlPullParser.END_TAG) {
			parser.next();
			if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equalsIgnoreCase(InfinitumContextConstants.PROPERTY_ELEMENT)) {
				String name = parser.getAttributeValue(0);
				parser.next();
				String value = parser.getText();
				if (InfinitumContextConstants.DEBUG_ATTRIBUTE.equalsIgnoreCase(name)) {
					ctx.setDebug(Boolean.parseBoolean(value));
				} else if (name.equalsIgnoreCase(InfinitumContextConstants.MODE_ATTRIBUTE)) {
					if (InfinitumContextConstants.MODE_ANNOTATIONS.equalsIgnoreCase(value))
						ctx.setConfigurationMode(ConfigurationMode.Annotation);
					else if (InfinitumContextConstants.MODE_XML.equalsIgnoreCase(value))
						ctx.setConfigurationMode(ConfigurationMode.XML);
					else
						throw new InfinitumConfigurationException(String.format(InfinitumContextConstants.CONFIG_PARSE_ERROR_LINE, parser.getLineNumber()));
				} else if (name.equalsIgnoreCase(InfinitumContextConstants.RECYCLE_ATTRIBUTE)) {
					ctx.setCacheRecyclable(Boolean.parseBoolean(value));
				}
			}
			eventType = parser.next();
		}
	}
	
	private void configureSqlite(XmlResourceParser parser, InfinitumContext ctx) throws XmlPullParserException, IOException {
		ctx.setHasSqliteDb(true);
		int eventType = parser.getEventType();
		while (eventType != XmlPullParser.END_TAG) {
			parser.next();
			if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equalsIgnoreCase(InfinitumContextConstants.PROPERTY_ELEMENT)) {
				String name = parser.getAttributeValue(0);
				parser.next();
				String value = parser.getText();
				if (InfinitumContextConstants.DB_NAME_ATTRIBUTE.equalsIgnoreCase(name)) {
					if ("".equalsIgnoreCase(value))
						throw new InfinitumConfigurationException(InfinitumContextConstants.SQLITE_DB_NAME_MISSING);
					ctx.setSqliteDbName(value.trim());
				} else if (InfinitumContextConstants.DB_VERSION_ATTRIBUTE.equalsIgnoreCase(name)) {
					ctx.setSqliteDbVersion(Integer.parseInt(value));
				}
				// TODO parse remaining properties
			}
			eventType = parser.next();
		}
		if (ctx.getSqliteDbName() == null)
			throw new InfinitumConfigurationException(InfinitumContextConstants.SQLITE_DB_NAME_MISSING);
	}
	
	private void configureRest(XmlResourceParser parser, InfinitumContext ctx) {
		// TODO
	}
	
	private void configureDomain(XmlResourceParser parser, InfinitumContext ctx) {
		// TODO
	}

}
