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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;

import com.clarionmedia.infinitum.context.ContextConstants;
import com.clarionmedia.infinitum.context.ContextService;
import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.context.InfinitumContext.ConfigurationMode;
import com.clarionmedia.infinitum.context.RestfulConfiguration;
import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;
import com.clarionmedia.infinitum.logging.Logger;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.rest.AuthenticationStrategy;
import com.clarionmedia.infinitum.rest.TokenGenerator;
import com.clarionmedia.infinitum.rest.impl.SharedSecretAuthentication;

/**
 * <p>
 * Provides access to an {@link InfinitumContext} singleton. In order for this
 * class to function properly, an {@code infinitum.cfg.xml} file must be created
 * and {@link ContextFactory#configure(Context, int)} must be called using the
 * location of the XML file before accessing the {@code InfinitumContext} or an
 * {@link InfinitumConfigurationException} will be thrown.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/11/12
 */
public class ContextFactory implements ContextService {

	private static ContextFactory sContextFactory;
	private static InfinitumContext sInfinitumContext;
	private static Context sContext;

	private Logger mLogger;

	/**
	 * Constructs a new {@code ContextFactory}.
	 */
	private ContextFactory() {
		mLogger = Logger.getInstance(getClass().getSimpleName());
	}

	/**
	 * Retrieves an {@code InfinitumContextFactory} instance.
	 * 
	 * @return {@code InfinitumContextFactory}
	 */
	public static ContextFactory getInstance() {
		if (sContextFactory == null)
			sContextFactory = new ContextFactory();
		return sContextFactory;
	}

	@Override
	public InfinitumContext configure(Context context, int configId) throws InfinitumConfigurationException {
		sContext = context;
		sInfinitumContext = configureFromXml(configId);
		sInfinitumContext.setContext(context);
		return sInfinitumContext;
	}

	@Override
	public InfinitumContext getContext() throws InfinitumConfigurationException {
		if (sInfinitumContext == null)
			throw new InfinitumConfigurationException(ContextConstants.CONFIG_NOT_CALLED);
		return sInfinitumContext;
	}

	@Override
	public PersistencePolicy getPersistencePolicy() {
		if (sInfinitumContext == null)
			throw new InfinitumConfigurationException(ContextConstants.CONFIG_NOT_CALLED);
		return sInfinitumContext.getPersistencePolicy();
	}

	private InfinitumContext configureFromXml(int configId) throws InfinitumConfigurationException {
		InfinitumContext ret = new ApplicationContext();
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
					if (name.equalsIgnoreCase(ContextConstants.APPLICATION_ELEMENT))
						configureApplication(parser, ret);
					else if (name.equalsIgnoreCase(ContextConstants.SQLITE_ELEMENT))
						configureSqlite(parser, ret);
					else if (name.equalsIgnoreCase(ContextConstants.REST_ELEMENT))
						configureRest(parser, ret);
					else if (name.equalsIgnoreCase(ContextConstants.DOMAIN_ELEMENT))
						configureDomain(parser, ret);
					break;
				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
			throw new InfinitumConfigurationException(ContextConstants.CONFIG_PARSE_ERROR);
		} catch (IOException e) {
			throw new InfinitumConfigurationException(ContextConstants.CONFIG_PARSE_ERROR);
		} finally {
			parser.close();
		}
		return ret;
	}

	private BeanFactory configureBeans(XmlResourceParser parser, InfinitumContext ctx) {
		BeanFactory container = new BeanFactory();
		try {
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					String name = parser.getName();
					if (name.equalsIgnoreCase(ContextConstants.BEANS_ELEMENT)) {
						parser.next();
						while (!parser.getName().equalsIgnoreCase(ContextConstants.BEANS_ELEMENT)
								|| (parser.getEventType() == XmlPullParser.END_TAG && parser.getName() == null)) {
							if (parser.getEventType() == XmlPullParser.START_TAG
									&& parser.getName().equalsIgnoreCase(ContextConstants.BEAN_ELEMENT)) {
								String id = parser.getAttributeValue(null, ContextConstants.ID_ATTRIBUTE);
								String className = parser.getAttributeValue(null, ContextConstants.CLASS_ATTRIBUTE);
								if (id == null)
									throw new InfinitumConfigurationException(String.format(
											ContextConstants.BEAN_ID_MISSING_LINE, parser.getLineNumber()));
								if (className == null)
									throw new InfinitumConfigurationException(String.format(
											ContextConstants.BEAN_CLASS_MISSING_LINE, parser.getLineNumber()));
								eventType = parser.next();
								Map<String, Object> args = new HashMap<String, Object>();
								while (!parser.getName().equalsIgnoreCase(ContextConstants.BEAN_ELEMENT)) {
									if (parser.getEventType() == XmlPullParser.START_TAG
											&& parser.getName().equalsIgnoreCase(ContextConstants.PROPERTY_ELEMENT)) {
										String prop = parser.getAttributeValue(null, ContextConstants.NAME_ATTRIBUTE);
										String ref = parser.getAttributeValue(null, ContextConstants.REF_ATTRIBUTE);
										Object val;
										if (ref == null) {
											parser.next();
											val = parser.getText();
										} else {
											val = container.loadBean(ref);
										}
										args.put(prop, val);
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
			mLogger.error("Unable to parse configuration", e);
			return null;
		} catch (IOException e) {
			mLogger.error("Unable to parse configuration", e);
			return null;
		}
		return container;
	}

	private void configureApplication(XmlResourceParser parser, InfinitumContext ctx) throws XmlPullParserException,
			IOException {
		parser.next();
		while (!parser.getName().equalsIgnoreCase(ContextConstants.APPLICATION_ELEMENT)) {
			if (parser.getEventType() == XmlPullParser.START_TAG
					&& parser.getName().equalsIgnoreCase(ContextConstants.PROPERTY_ELEMENT)) {
				String name = parser.getAttributeValue(null, ContextConstants.NAME_ATTRIBUTE);
				parser.next();
				String value = parser.getText();
				if (ContextConstants.DEBUG_ATTRIBUTE.equalsIgnoreCase(name)) {
					ctx.setDebug(Boolean.parseBoolean(value));
				} else if (name.equalsIgnoreCase(ContextConstants.MODE_ATTRIBUTE)) {
					if (ContextConstants.MODE_ANNOTATIONS.equalsIgnoreCase(value))
						ctx.setConfigurationMode(ConfigurationMode.Annotation);
					else if (ContextConstants.MODE_XML.equalsIgnoreCase(value))
						ctx.setConfigurationMode(ConfigurationMode.Xml);
					else
						throw new InfinitumConfigurationException(String.format(
								ContextConstants.CONFIG_PARSE_ERROR_LINE, parser.getLineNumber()));
				} else if (name.equalsIgnoreCase(ContextConstants.RECYCLE_ATTRIBUTE)) {
					ctx.setCacheRecyclable(Boolean.parseBoolean(value));
				}
			}
			parser.next();
		}
	}

	private void configureSqlite(XmlResourceParser parser, InfinitumContext ctx) throws XmlPullParserException,
			IOException {
		parser.next();
		while (!parser.getName().equalsIgnoreCase(ContextConstants.SQLITE_ELEMENT)) {
			if (parser.getEventType() == XmlPullParser.START_TAG
					&& parser.getName().equalsIgnoreCase(ContextConstants.PROPERTY_ELEMENT)) {
				String name = parser.getAttributeValue(null, ContextConstants.NAME_ATTRIBUTE);
				parser.next();
				String value = parser.getText();
				if (ContextConstants.DB_NAME_ATTRIBUTE.equalsIgnoreCase(name)) {
					if ("".equalsIgnoreCase(value))
						throw new InfinitumConfigurationException(ContextConstants.SQLITE_DB_NAME_MISSING);
					ctx.setSqliteDbName(value.trim());
				} else if (ContextConstants.DB_VERSION_ATTRIBUTE.equalsIgnoreCase(name)) {
					ctx.setSqliteDbVersion(Integer.parseInt(value));
				} else if (ContextConstants.DB_GENERATE_SCHEMA_ATTRIBUTE.equalsIgnoreCase(name)) {
					ctx.setSchemaGenerated(Boolean.parseBoolean(value));
				} else if (ContextConstants.DB_AUTOCOMMIT_ATTRIBUTE.equalsIgnoreCase(name)) {
					ctx.setAutocommit(Boolean.parseBoolean(value));
				}
			}
			parser.next();
		}
		if (ctx.getSqliteDbName() == null)
			throw new InfinitumConfigurationException(ContextConstants.SQLITE_DB_NAME_MISSING);
	}

	private void configureRest(XmlResourceParser parser, InfinitumContext ctx) throws XmlPullParserException,
			IOException {
		RestfulConfiguration restCtx = new RestfulContext();
		String clientBean = parser.getAttributeValue(null, ContextConstants.REF_ATTRIBUTE);
		if (clientBean != null && !ctx.getBeanContainer().beanExists(clientBean))
			throw new InfinitumConfigurationException(ContextConstants.REST_CLIENT_BEAN_UNRESOLVED);
		restCtx.setClientBean(clientBean);
		ctx.setRestfulConfiguration(restCtx);
		parser.next();
		while (!parser.getName().equalsIgnoreCase(ContextConstants.REST_ELEMENT)) {
			if (parser.getEventType() == XmlPullParser.START_TAG
					&& parser.getName().equalsIgnoreCase(ContextConstants.PROPERTY_ELEMENT)) {
				String name = parser.getAttributeValue(null, ContextConstants.NAME_ATTRIBUTE);
				parser.next();
				String value = parser.getText();
				if (ContextConstants.REST_HOST_ATTRIBUTE.equalsIgnoreCase(name)) {
					if ("".equalsIgnoreCase(value))
						throw new InfinitumConfigurationException(ContextConstants.REST_HOST_MISSING);
					ctx.getRestfulConfiguration().setRestHost(value.trim());
				} else if (ContextConstants.CONNECTION_TIMEOUT_ATTRIBUTE.equalsIgnoreCase(name)) {
					ctx.getRestfulConfiguration().setConnectionTimeout(Integer.parseInt(value));
				} else if (ContextConstants.RESPONSE_TIMEOUT_ATTRIBUTE.equalsIgnoreCase(name)) {
					ctx.getRestfulConfiguration().setResponseTimeout(Integer.parseInt(value));
				}
			} else if (parser.getEventType() == XmlPullParser.START_TAG
					&& parser.getName().equalsIgnoreCase(ContextConstants.AUTHENTICATION_ELEMENT)) {
				String enabled = parser.getAttributeValue(null, ContextConstants.ENABLED_ATTRIBUTE);
				if (enabled == null)
					ctx.getRestfulConfiguration().setRestAuthenticated(true);
				else
					ctx.getRestfulConfiguration().setRestAuthenticated(Boolean.parseBoolean(enabled));
				String strategy = parser.getAttributeValue(null, ContextConstants.STRATEGY_ATTRIBUTE);
				if (strategy != null) {
					ctx.getRestfulConfiguration().setAuthStrategy(strategy);
					String generator = parser.getAttributeValue(null, ContextConstants.GENERATOR_ATTRIBUTE);
					if (generator != null && "token".equalsIgnoreCase(strategy)) {
						SharedSecretAuthentication auth = (SharedSecretAuthentication) ctx.getRestfulConfiguration()
								.getAuthStrategy();
						auth.setTokenGenerator((TokenGenerator) ctx.getBean(generator));
					}
				} else {
					String beanRef = parser.getAttributeValue(null, ContextConstants.REF_ATTRIBUTE);
					ctx.getRestfulConfiguration().setAuthStrategy(
							(AuthenticationStrategy) ctx.getBeanContainer().loadBean(beanRef));
				}
				SharedSecretAuthentication token = null;
				AuthenticationStrategy strat = ctx.getRestfulConfiguration().getAuthStrategy();
				if (strat == null)
					throw new InfinitumConfigurationException(ContextConstants.AUTH_STRAT_MISSING);
				if (strat.getClass() == SharedSecretAuthentication.class)
					token = (SharedSecretAuthentication) strat;
				parser.next();
				while (!parser.getName().equalsIgnoreCase(ContextConstants.AUTHENTICATION_ELEMENT)) {
					if (parser.getEventType() == XmlPullParser.START_TAG
							&& parser.getName().equalsIgnoreCase(ContextConstants.PROPERTY_ELEMENT)) {
						String name = parser.getAttributeValue(null, ContextConstants.NAME_ATTRIBUTE);
						parser.next();
						String value = parser.getText();
						if (ContextConstants.TOKEN_NAME_ATTRIBUTE.equalsIgnoreCase(name)) {
							if ("".equalsIgnoreCase(value))
								throw new InfinitumConfigurationException(ContextConstants.AUTH_TOKEN_NAME_MISSING);
							token.setTokenName(value);
						} else if (ContextConstants.TOKEN_ATTRIBUTE.equalsIgnoreCase(name)) {
							if ("".equalsIgnoreCase(value))
								throw new InfinitumConfigurationException(ContextConstants.AUTH_TOKEN_MISSING);
							token.setToken(value);
						}
					}
					parser.next();
				}
				if (token != null && token.getTokenName() == null)
					throw new InfinitumConfigurationException(ContextConstants.AUTH_TOKEN_NAME_MISSING);
				if (token != null && token.getToken() == null)
					throw new InfinitumConfigurationException(ContextConstants.AUTH_TOKEN_MISSING);
			}
			parser.next();
		}
		if (ctx.getRestfulConfiguration().getRestHost() == null)
			throw new InfinitumConfigurationException(ContextConstants.REST_HOST_MISSING);
	}

	private void configureDomain(XmlResourceParser parser, InfinitumContext ctx) throws XmlPullParserException,
			IOException {
		parser.next();
		while (!parser.getName().equalsIgnoreCase(ContextConstants.DOMAIN_ELEMENT)) {
			if (parser.getEventType() == XmlPullParser.START_TAG
					&& parser.getName().equalsIgnoreCase(ContextConstants.MODEL_ELEMENT)) {
				String model = parser.getAttributeValue(null, ContextConstants.DOMAIN_RESOURCE_ATTRIBUTE);
				if (model == null)
					throw new InfinitumConfigurationException(ContextConstants.MODEL_RESOURCE_MISSING);
				if (model.trim().equalsIgnoreCase(""))
					throw new InfinitumConfigurationException(ContextConstants.MODEL_RESOURCE_MISSING);
				ctx.addDomainModel(model);
			}
			parser.next();
		}
	}

}
