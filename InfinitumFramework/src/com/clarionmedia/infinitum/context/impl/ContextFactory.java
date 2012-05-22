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
import com.clarionmedia.infinitum.context.ContextService;
import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.context.InfinitumContext.ConfigurationMode;
import com.clarionmedia.infinitum.context.RestfulConfiguration;
import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;
import com.clarionmedia.infinitum.internal.PropertyLoader;
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
	private static PropertyLoader sPropLoader;

	private ContextFactory() {
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
		sPropLoader = new PropertyLoader(sContext);
		sInfinitumContext = configureFromXml(configId);
		sInfinitumContext.setContext(context);
		return sInfinitumContext;
	}

	@Override
	public InfinitumContext getContext() throws InfinitumConfigurationException {
		if (sInfinitumContext == null)
			throw new InfinitumConfigurationException(sPropLoader.getErrorMessage("CONFIG_NOT_CALLED"));
		return sInfinitumContext;
	}

	@Override
	public PersistencePolicy getPersistencePolicy() {
		if (sInfinitumContext == null)
			throw new InfinitumConfigurationException(sPropLoader.getErrorMessage("CONFIG_NOT_CALLED"));
		return sInfinitumContext.getPersistencePolicy();
	}
	
	@Override
	public Context getAndroidContext() {
		return sContext;
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
					if (name.equalsIgnoreCase(sPropLoader.getContextValue("APPLICATION_ELEMENT")))
						configureApplication(parser, ret);
					else if (name.equalsIgnoreCase(sPropLoader.getContextValue("SQLITE_ELEMENT")))
						configureSqlite(parser, ret);
					else if (name.equalsIgnoreCase(sPropLoader.getContextValue("REST_ELEMENT")))
						configureRest(parser, ret);
					else if (name.equalsIgnoreCase(sPropLoader.getContextValue("DOMAIN_ELEMENT")))
						configureDomain(parser, ret);
					break;
				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
			throw new InfinitumConfigurationException(sPropLoader.getErrorMessage("CONFIG_PARSE_ERROR"));
		} catch (IOException e) {
			throw new InfinitumConfigurationException(sPropLoader.getErrorMessage("CONFIG_PARSE_ERROR"));
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
					if (name.equalsIgnoreCase(sPropLoader.getContextValue("BEANS_ELEMENT"))) {
						parser.next();
						while (!parser.getName().equalsIgnoreCase(sPropLoader.getContextValue("BEANS_ELEMENT"))
								|| (parser.getEventType() == XmlPullParser.END_TAG && parser.getName() == null)) {
							if (parser.getEventType() == XmlPullParser.START_TAG
									&& parser.getName().equalsIgnoreCase(sPropLoader.getContextValue("BEAN_ELEMENT"))) {
								String id = parser.getAttributeValue(null, sPropLoader.getContextValue("ID_ATTRIBUTE"));
								String className = parser.getAttributeValue(null,
										sPropLoader.getContextValue("CLASS_ATTRIBUTE"));
								if (id == null)
									throw new InfinitumConfigurationException(
											String.format(sPropLoader.getErrorMessage("BEAN_ID_MISSING_LINE"),
													parser.getLineNumber()));
								if (className == null)
									throw new InfinitumConfigurationException(String.format(
											sPropLoader.getErrorMessage("BEAN_CLASS_MISSING_LINE"),
											parser.getLineNumber()));
								eventType = parser.next();
								Map<String, Object> args = new HashMap<String, Object>();
								while (!parser.getName().equalsIgnoreCase(sPropLoader.getContextValue("BEAN_ELEMENT"))) {
									if (parser.getEventType() == XmlPullParser.START_TAG
											&& parser.getName().equalsIgnoreCase(
													sPropLoader.getContextValue("PROPERTY_ELEMENT"))) {
										String prop = parser.getAttributeValue(null,
												sPropLoader.getContextValue("NAME_ATTRIBUTE"));
										String ref = parser.getAttributeValue(null,
												sPropLoader.getContextValue("REF_ATTRIBUTE"));
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
			return null;
		} catch (IOException e) {
			return null;
		}
		return container;
	}

	private void configureApplication(XmlResourceParser parser, InfinitumContext ctx) throws XmlPullParserException,
			IOException {
		parser.next();
		while (!parser.getName().equalsIgnoreCase(sPropLoader.getContextValue("APPLICATION_ELEMENT"))) {
			if (parser.getEventType() == XmlPullParser.START_TAG
					&& parser.getName().equalsIgnoreCase(sPropLoader.getContextValue("PROPERTY_ELEMENT"))) {
				String name = parser.getAttributeValue(null, sPropLoader.getContextValue("NAME_ATTRIBUTE"));
				parser.next();
				String value = parser.getText();
				if (sPropLoader.getContextValue("DEBUG_ATTRIBUTE").equalsIgnoreCase(name)) {
					ctx.setDebug(Boolean.parseBoolean(value));
				} else if (name.equalsIgnoreCase(sPropLoader.getContextValue("MODE_ATTRIBUTE"))) {
					if (sPropLoader.getContextValue("MODE_ANNOTATIONS").equalsIgnoreCase(value))
						ctx.setConfigurationMode(ConfigurationMode.Annotation);
					else if (sPropLoader.getContextValue("MODE_XML").equalsIgnoreCase(value))
						ctx.setConfigurationMode(ConfigurationMode.Xml);
					else
						throw new InfinitumConfigurationException(String.format(
								sPropLoader.getErrorMessage("CONFIG_PARSE_ERROR_LINE"), parser.getLineNumber()));
				} else if (name.equalsIgnoreCase(sPropLoader.getContextValue("RECYCLE_ATTRIBUTE"))) {
					ctx.setCacheRecyclable(Boolean.parseBoolean(value));
				}
			}
			parser.next();
		}
	}

	private void configureSqlite(XmlResourceParser parser, InfinitumContext ctx) throws XmlPullParserException,
			IOException {
		parser.next();
		while (!parser.getName().equalsIgnoreCase(sPropLoader.getContextValue("SQLITE_ELEMENT"))) {
			if (parser.getEventType() == XmlPullParser.START_TAG
					&& parser.getName().equalsIgnoreCase(sPropLoader.getContextValue("PROPERTY_ELEMENT"))) {
				String name = parser.getAttributeValue(null, sPropLoader.getContextValue("NAME_ATTRIBUTE"));
				parser.next();
				String value = parser.getText();
				if (sPropLoader.getContextValue("DB_NAME_ATTRIBUTE").equalsIgnoreCase(name)) {
					if ("".equalsIgnoreCase(value))
						throw new InfinitumConfigurationException(sPropLoader.getErrorMessage("SQLITE_DB_NAME_MISSING"));
					ctx.setSqliteDbName(value.trim());
				} else if (sPropLoader.getContextValue("DB_VERSION_ATTRIBUTE").equalsIgnoreCase(name)) {
					ctx.setSqliteDbVersion(Integer.parseInt(value));
				} else if (sPropLoader.getContextValue("DB_GENERATE_SCHEMA_ATTRIBUTE").equalsIgnoreCase(name)) {
					ctx.setSchemaGenerated(Boolean.parseBoolean(value));
				} else if (sPropLoader.getContextValue("DB_AUTOCOMMIT_ATTRIBUTE").equalsIgnoreCase(name)) {
					ctx.setAutocommit(Boolean.parseBoolean(value));
				}
			}
			parser.next();
		}
		if (ctx.getSqliteDbName() == null)
			throw new InfinitumConfigurationException(sPropLoader.getErrorMessage("SQLITE_DB_NAME_MISSING"));
	}

	private void configureRest(XmlResourceParser parser, InfinitumContext ctx) throws XmlPullParserException,
			IOException {
		RestfulConfiguration restCtx = new RestfulContext();
		String clientBean = parser.getAttributeValue(null, sPropLoader.getContextValue("REF_ATTRIBUTE"));
		if (clientBean != null && !ctx.getBeanContainer().beanExists(clientBean))
			throw new InfinitumConfigurationException(sPropLoader.getErrorMessage("REST_CLIENT_BEAN_UNRESOLVED"));
		restCtx.setClientBean(clientBean);
		ctx.setRestfulConfiguration(restCtx);
		parser.next();
		while (!parser.getName().equalsIgnoreCase(sPropLoader.getContextValue("REST_ELEMENT"))) {
			if (parser.getEventType() == XmlPullParser.START_TAG
					&& parser.getName().equalsIgnoreCase(sPropLoader.getContextValue("PROPERTY_ELEMENT"))) {
				String name = parser.getAttributeValue(null, sPropLoader.getContextValue("NAME_ATTRIBUTE"));
				parser.next();
				String value = parser.getText();
				if (sPropLoader.getContextValue("REST_HOST_ATTRIBUTE").equalsIgnoreCase(name)) {
					if ("".equalsIgnoreCase(value))
						throw new InfinitumConfigurationException(sPropLoader.getErrorMessage("REST_HOST_MISSING"));
					ctx.getRestfulConfiguration().setRestHost(value.trim());
				} else if (sPropLoader.getContextValue("CONNECTION_TIMEOUT_ATTRIBUTE").equalsIgnoreCase(name)) {
					ctx.getRestfulConfiguration().setConnectionTimeout(Integer.parseInt(value));
				} else if (sPropLoader.getContextValue("RESPONSE_TIMEOUT_ATTRIBUTE").equalsIgnoreCase(name)) {
					ctx.getRestfulConfiguration().setResponseTimeout(Integer.parseInt(value));
				}
			} else if (parser.getEventType() == XmlPullParser.START_TAG
					&& parser.getName().equalsIgnoreCase(sPropLoader.getContextValue("AUTHENTICATION_ELEMENT"))) {
				String enabled = parser.getAttributeValue(null, sPropLoader.getContextValue("ENABLED_ATTRIBUTE"));
				if (enabled == null)
					ctx.getRestfulConfiguration().setRestAuthenticated(true);
				else
					ctx.getRestfulConfiguration().setRestAuthenticated(Boolean.parseBoolean(enabled));
				String strategy = parser.getAttributeValue(null, sPropLoader.getContextValue("STRATEGY_ATTRIBUTE"));
				if (strategy != null) {
					ctx.getRestfulConfiguration().setAuthStrategy(strategy);
					String generator = parser.getAttributeValue(null,
							sPropLoader.getContextValue("GENERATOR_ATTRIBUTE"));
					if (generator != null && "token".equalsIgnoreCase(strategy)) {
						SharedSecretAuthentication auth = (SharedSecretAuthentication) ctx.getRestfulConfiguration()
								.getAuthStrategy();
						auth.setTokenGenerator((TokenGenerator) ctx.getBean(generator));
					}
				} else {
					String beanRef = parser.getAttributeValue(null, sPropLoader.getContextValue("REF_ATTRIBUTE"));
					ctx.getRestfulConfiguration().setAuthStrategy(
							(AuthenticationStrategy) ctx.getBeanContainer().loadBean(beanRef));
				}
				SharedSecretAuthentication token = null;
				AuthenticationStrategy strat = ctx.getRestfulConfiguration().getAuthStrategy();
				if (strat == null)
					throw new InfinitumConfigurationException(sPropLoader.getErrorMessage("AUTH_STRAT_MISSING"));
				if (strat.getClass() == SharedSecretAuthentication.class)
					token = (SharedSecretAuthentication) strat;
				parser.next();
				while (!parser.getName().equalsIgnoreCase(sPropLoader.getContextValue("AUTHENTICATION_ELEMENT"))) {
					if (parser.getEventType() == XmlPullParser.START_TAG
							&& parser.getName().equalsIgnoreCase(sPropLoader.getContextValue("PROPERTY_ELEMENT"))) {
						String name = parser.getAttributeValue(null, sPropLoader.getContextValue("NAME_ATTRIBUTE"));
						parser.next();
						String value = parser.getText();
						if (sPropLoader.getContextValue("TOKEN_NAME_ATTRIBUTE").equalsIgnoreCase(name)) {
							if ("".equalsIgnoreCase(value))
								throw new InfinitumConfigurationException(
										sPropLoader.getErrorMessage("AUTH_TOKEN_NAME_MISSING"));
							token.setTokenName(value);
						} else if (sPropLoader.getContextValue("TOKEN_ATTRIBUTE").equalsIgnoreCase(name)) {
							if ("".equalsIgnoreCase(value))
								throw new InfinitumConfigurationException(
										sPropLoader.getErrorMessage("AUTH_TOKEN_MISSING"));
							token.setToken(value);
						}
					}
					parser.next();
				}
				if (token != null && token.getTokenName() == null)
					throw new InfinitumConfigurationException(sPropLoader.getErrorMessage("AUTH_TOKEN_NAME_MISSING"));
				if (token != null && token.getToken() == null)
					throw new InfinitumConfigurationException(sPropLoader.getErrorMessage("AUTH_TOKEN_MISSING"));
			}
			parser.next();
		}
		if (ctx.getRestfulConfiguration().getRestHost() == null)
			throw new InfinitumConfigurationException(sPropLoader.getErrorMessage("REST_HOST_MISSING"));
	}

	private void configureDomain(XmlResourceParser parser, InfinitumContext ctx) throws XmlPullParserException,
			IOException {
		parser.next();
		while (!parser.getName().equalsIgnoreCase(sPropLoader.getContextValue("DOMAIN_ELEMENT"))) {
			if (parser.getEventType() == XmlPullParser.START_TAG
					&& parser.getName().equalsIgnoreCase(sPropLoader.getContextValue("MODEL_ELEMENT"))) {
				String model = parser.getAttributeValue(null, sPropLoader.getContextValue("DOMAIN_RESOURCE_ATTRIBUTE"));
				if (model == null)
					throw new InfinitumConfigurationException(sPropLoader.getErrorMessage("MODEL_RESOURCE_MISSING"));
				if (model.trim().equalsIgnoreCase(""))
					throw new InfinitumConfigurationException(sPropLoader.getErrorMessage("MODEL_RESOURCE_MISSING"));
				ctx.addDomainModel(model);
			}
			parser.next();
		}
	}

}
