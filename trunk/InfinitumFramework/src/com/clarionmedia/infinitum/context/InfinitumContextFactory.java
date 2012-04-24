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
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;

import com.clarionmedia.infinitum.context.InfinitumContext.ConfigurationMode;
import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;
import com.clarionmedia.infinitum.rest.AuthenticationStrategy;
import com.clarionmedia.infinitum.rest.impl.SharedSecretAuthentication;

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

	private static InfinitumContextFactory sContextFactory;
	private static InfinitumContext sInfinitumContext;
	private static Context sContext;

	private InfinitumContextFactory() {
	}

	/**
	 * Retrieves an {@code InfinitumContextFactory} instance.
	 * 
	 * @return {@code InfinitumContextFactory}
	 */
	public static InfinitumContextFactory getInstance() {
		if (sContextFactory == null)
			sContextFactory = new InfinitumContextFactory();
		return sContextFactory;
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
		sInfinitumContext = configureFromXml(configId);
		sInfinitumContext.setContext(context);
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
	public InfinitumContext getInfinitumContext() throws InfinitumConfigurationException {
		if (sInfinitumContext == null)
			throw new InfinitumConfigurationException(InfinitumContextConstants.CONFIG_NOT_CALLED);
		return sInfinitumContext;
	}

	private InfinitumContext configureFromXml(int configId) throws InfinitumConfigurationException {
		InfinitumContext ret = new InfinitumContext();
		Resources resources = sContext.getResources();
		XmlResourceParser parser = resources.getXml(configId);
		ret.setBeanContainer(configureBeans(parser, ret));
		parser = resources.getXml(configId);
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

	private BeanContainer configureBeans(XmlResourceParser parser, InfinitumContext ctx) {
		BeanContainer container = new BeanContainer();
		try {
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					String name = parser.getName();
					if (name.equalsIgnoreCase(InfinitumContextConstants.BEANS_ELEMENT)) {
						parser.next();
						while (!parser.getName().equalsIgnoreCase(InfinitumContextConstants.BEANS_ELEMENT)
								|| (parser.getEventType() == XmlPullParser.END_TAG && parser.getName() == null)) {
							if (parser.getEventType() == XmlPullParser.START_TAG
									&& parser.getName().equalsIgnoreCase(InfinitumContextConstants.BEAN_ELEMENT)) {
								String id = parser.getAttributeValue(null, InfinitumContextConstants.ID_ATTRIBUTE);
								String className = parser.getAttributeValue(null,
										InfinitumContextConstants.CLASS_ATTRIBUTE);
								if (id == null)
									throw new InfinitumConfigurationException(String.format(
											InfinitumContextConstants.BEAN_ID_MISSING_LINE, parser.getLineNumber()));
								if (className == null)
									throw new InfinitumConfigurationException(String.format(
											InfinitumContextConstants.BEAN_CLASS_MISSING_LINE, parser.getLineNumber()));
								eventType = parser.next();
								Map<String, String> args = new HashMap<String, String>();
								while (!parser.getName().equalsIgnoreCase(InfinitumContextConstants.BEAN_ELEMENT)) {
									if (parser.getEventType() == XmlPullParser.START_TAG
											&& parser.getName().equalsIgnoreCase(
													InfinitumContextConstants.PROPERTY_ELEMENT)) {
										String prop = parser.getAttributeValue(null,
												InfinitumContextConstants.NAME_ATTRIBUTE);
										parser.next();
										String value = parser.getText();
										args.put(prop, value);
									}
									parser.next();
								}
								container.registerBean(id, className, args);
							}
							parser.next();
						}
					}
				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return container;
	}

	private void configureApplication(XmlResourceParser parser, InfinitumContext ctx) throws XmlPullParserException,
			IOException {
		parser.next();
		while (!parser.getName().equalsIgnoreCase(InfinitumContextConstants.APPLICATION_ELEMENT)) {
			if (parser.getEventType() == XmlPullParser.START_TAG
					&& parser.getName().equalsIgnoreCase(InfinitumContextConstants.PROPERTY_ELEMENT)) {
				String name = parser.getAttributeValue(null, InfinitumContextConstants.NAME_ATTRIBUTE);
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
						throw new InfinitumConfigurationException(String.format(
								InfinitumContextConstants.CONFIG_PARSE_ERROR_LINE, parser.getLineNumber()));
				} else if (name.equalsIgnoreCase(InfinitumContextConstants.RECYCLE_ATTRIBUTE)) {
					ctx.setCacheRecyclable(Boolean.parseBoolean(value));
				}
			}
			parser.next();
		}
	}

	private void configureSqlite(XmlResourceParser parser, InfinitumContext ctx) throws XmlPullParserException,
			IOException {
		ctx.setHasSqliteDb(true);
		parser.next();
		while (!parser.getName().equalsIgnoreCase(InfinitumContextConstants.SQLITE_ELEMENT)) {
			if (parser.getEventType() == XmlPullParser.START_TAG
					&& parser.getName().equalsIgnoreCase(InfinitumContextConstants.PROPERTY_ELEMENT)) {
				String name = parser.getAttributeValue(null, InfinitumContextConstants.NAME_ATTRIBUTE);
				parser.next();
				String value = parser.getText();
				if (InfinitumContextConstants.DB_NAME_ATTRIBUTE.equalsIgnoreCase(name)) {
					if ("".equalsIgnoreCase(value))
						throw new InfinitumConfigurationException(InfinitumContextConstants.SQLITE_DB_NAME_MISSING);
					ctx.setSqliteDbName(value.trim());
				} else if (InfinitumContextConstants.DB_VERSION_ATTRIBUTE.equalsIgnoreCase(name)) {
					ctx.setSqliteDbVersion(Integer.parseInt(value));
				} else if (InfinitumContextConstants.DB_GENERATE_SCHEMA_ATTRIBUTE.equalsIgnoreCase(name)) {
					ctx.setSchemaGenerated(Boolean.parseBoolean(value));
				} else if (InfinitumContextConstants.DB_AUTOCOMMIT_ATTRIBUTE.equalsIgnoreCase(name)) {
					ctx.setAutocommit(Boolean.parseBoolean(value));
				}
			}
			parser.next();
		}
		if (ctx.getSqliteDbName() == null)
			throw new InfinitumConfigurationException(InfinitumContextConstants.SQLITE_DB_NAME_MISSING);
	}

	private void configureRest(XmlResourceParser parser, InfinitumContext ctx) throws XmlPullParserException,
			IOException {
		RestfulContext restCtx = new RestfulContext();
		String clientBean = parser.getAttributeValue(null, InfinitumContextConstants.REF_ATTRIBUTE);
		if (clientBean == null)
			throw new InfinitumConfigurationException(InfinitumContextConstants.REST_CLIENT_BEAN_MISSING);
		if (!ctx.getBeanContainer().beanExists(clientBean))
			throw new InfinitumConfigurationException(InfinitumContextConstants.REST_CLIENT_BEAN_UNRESOLVED);
		restCtx.setClientBean(clientBean);
		ctx.setRestfulContext(restCtx);
		parser.next();
		while (!parser.getName().equalsIgnoreCase(InfinitumContextConstants.REST_ELEMENT)) {
			if (parser.getEventType() == XmlPullParser.START_TAG
					&& parser.getName().equalsIgnoreCase(InfinitumContextConstants.PROPERTY_ELEMENT)) {
				String name = parser.getAttributeValue(null, InfinitumContextConstants.NAME_ATTRIBUTE);
				parser.next();
				String value = parser.getText();
				if (InfinitumContextConstants.REST_HOST_ATTRIBUTE.equalsIgnoreCase(name)) {
					if ("".equalsIgnoreCase(value))
						throw new InfinitumConfigurationException(InfinitumContextConstants.REST_HOST_MISSING);
					ctx.getRestfulContext().setRestHost(value.trim());
				} else if (InfinitumContextConstants.CONNECTION_TIMEOUT_ATTRIBUTE.equalsIgnoreCase(name)) {
					ctx.getRestfulContext().setConnectionTimeout(Integer.parseInt(value));
				} else if (InfinitumContextConstants.RESPONSE_TIMEOUT_ATTRIBUTE.equalsIgnoreCase(name)) {
					ctx.getRestfulContext().setResponseTimeout(Integer.parseInt(value));
				}
			} else if (parser.getEventType() == XmlPullParser.START_TAG
					&& parser.getName().equalsIgnoreCase(InfinitumContextConstants.AUTHENTICATION_ELEMENT)) {
				String enabled = parser.getAttributeValue(null, InfinitumContextConstants.ENABLED_ATTRIBUTE);
				if (enabled == null)
					ctx.getRestfulContext().setRestAuthenticated(true);
				else
					ctx.getRestfulContext().setRestAuthenticated(Boolean.parseBoolean(enabled));
				String strategy = parser.getAttributeValue(null, InfinitumContextConstants.STRATEGY_ATTRIBUTE);
				if (strategy != null)
					ctx.getRestfulContext().setAuthStrategy(strategy);
				else {
					String beanRef = parser.getAttributeValue(null, InfinitumContextConstants.REF_ATTRIBUTE);
					ctx.getRestfulContext().setAuthStrategy(
							(AuthenticationStrategy) ctx.getBeanContainer().loadBean(beanRef));
				}
				SharedSecretAuthentication token = null;
				AuthenticationStrategy strat = ctx.getRestfulContext().getAuthStrategy();
				if (strat == null)
					throw new InfinitumConfigurationException(InfinitumContextConstants.AUTH_STRAT_MISSING);
				if (strat.getClass() == SharedSecretAuthentication.class)
					token = (SharedSecretAuthentication) strat;
				parser.next();
				while (!parser.getName().equalsIgnoreCase(InfinitumContextConstants.AUTHENTICATION_ELEMENT)) {
					if (parser.getEventType() == XmlPullParser.START_TAG
							&& parser.getName().equalsIgnoreCase(InfinitumContextConstants.PROPERTY_ELEMENT)) {
						String name = parser.getAttributeValue(null, InfinitumContextConstants.NAME_ATTRIBUTE);
						parser.next();
						String value = parser.getText();
						if (InfinitumContextConstants.TOKEN_NAME_ATTRIBUTE.equalsIgnoreCase(name)) {
							if ("".equalsIgnoreCase(value))
								throw new InfinitumConfigurationException(
										InfinitumContextConstants.AUTH_TOKEN_NAME_MISSING);
							token.setTokenName(value);
						} else if (InfinitumContextConstants.TOKEN_ATTRIBUTE.equalsIgnoreCase(name)) {
							if ("".equalsIgnoreCase(value))
								throw new InfinitumConfigurationException(InfinitumContextConstants.AUTH_TOKEN_MISSING);
							token.setToken(value);
						}
					}
					parser.next();
				}
				if (token != null && token.getTokenName() == null)
					throw new InfinitumConfigurationException(InfinitumContextConstants.AUTH_TOKEN_NAME_MISSING);
				if (token != null && token.getToken() == null)
					throw new InfinitumConfigurationException(InfinitumContextConstants.AUTH_TOKEN_MISSING);
			}
			parser.next();
		}
		if (ctx.getRestfulContext().getRestHost() == null)
			throw new InfinitumConfigurationException(InfinitumContextConstants.REST_HOST_MISSING);
	}

	private void configureDomain(XmlResourceParser parser, InfinitumContext ctx) throws XmlPullParserException,
			IOException {
		parser.next();
		while (!parser.getName().equalsIgnoreCase(InfinitumContextConstants.DOMAIN_ELEMENT)) {
			if (parser.getEventType() == XmlPullParser.START_TAG
					&& parser.getName().equalsIgnoreCase(InfinitumContextConstants.MODEL_ELEMENT)) {
				String model = parser.getAttributeValue(null, InfinitumContextConstants.DOMAIN_RESOURCE_ATTRIBUTE);
				if (model == null)
					throw new InfinitumConfigurationException(InfinitumContextConstants.MODEL_RESOURCE_MISSING);
				if (model.trim().equalsIgnoreCase(""))
					throw new InfinitumConfigurationException(InfinitumContextConstants.MODEL_RESOURCE_MISSING);
				ctx.addDomainModel(model);
			}
			parser.next();
		}
	}

}
