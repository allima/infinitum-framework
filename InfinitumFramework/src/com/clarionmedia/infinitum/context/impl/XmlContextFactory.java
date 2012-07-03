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

import java.io.InputStream;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Context;
import android.content.res.Resources;

import com.clarionmedia.infinitum.context.ContextFactory;
import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;

/**
 * <p>
 * Provides access to an {@link InfinitumContext} singleton. In order for this
 * class to function properly, an {@code infinitum.cfg.xml} file must be created
 * and {@link ContextFactory#configure(Context, int)} must be called before
 * accessing the {@code InfinitumContext} or an
 * {@link InfinitumConfigurationException} will be thrown.
 * {@link ContextFactory} singletons should be acquired by calling the static
 * method {@link ContextFactory#getInstance()}.
 * </p>
 * <p>
 * {@code SimpleXmlContextFactory} uses the Simple XML framework to read
 * {@code infinitum.cfg.xml} and create the {@code InfinitumContext}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0
 * @since 06/25/12
 */
public class XmlContextFactory extends ContextFactory {

	private static XmlContextFactory sContextFactory;
	private static Context sContext;
	private static InfinitumContext sInfinitumContext;

	/**
	 * Constructs a new {@code SimpleXmlContextFactory}. This is marked
	 * {@code private} to prevent direct instantiation.
	 * {@code SimpleXmlContextFactory} should be retrieved as a singleton.
	 */
	private XmlContextFactory() {

	}

	/**
	 * Retrieves a {@code SimpleXmlContextFactory} instance.
	 * 
	 * @return {@code SimpleContextFactory}
	 */
	public static XmlContextFactory newInstance() {
		if (sContextFactory == null)
			sContextFactory = new XmlContextFactory();
		return sContextFactory;
	}

	@Override
	public InfinitumContext configure(Context context) throws InfinitumConfigurationException {
		sContext = context;
		Resources res = context.getResources();
		int id = res.getIdentifier("infinitum", "raw", context.getPackageName());
		if (id == 0)
			throw new InfinitumConfigurationException("Configuration infinitum.cfg.xml could not be found.");
		sInfinitumContext = configureFromXml(id);
		sInfinitumContext.setContext(context);
		return sInfinitumContext;
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
		// TODO
		if (sInfinitumContext == null)
			throw new InfinitumConfigurationException("not configured");
		return sInfinitumContext;
	}

	@Override
	public PersistencePolicy getPersistencePolicy() {
		// TODO
		if (sInfinitumContext == null)
			throw new InfinitumConfigurationException("not configured");
		return sInfinitumContext.getPersistencePolicy();
	}

	@Override
	public Context getAndroidContext() {
		return sContext;
	}

	private InfinitumContext configureFromXml(int configId) {
		Resources resources = sContext.getResources();
		Serializer serializer = new Persister();
		try {
			InputStream stream = resources.openRawResource(configId);
			String xml = new java.util.Scanner(stream).useDelimiter("\\A").next();
			return serializer.read(XmlApplicationContext.class, xml);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

}
