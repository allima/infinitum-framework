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
import java.util.Calendar;
import java.util.Scanner;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

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
 * {@link XmlContextFactory} singletons should be acquired by calling the static
 * method {@link XmlContextFactory#instance()}.
 * </p>
 * <p>
 * {@code XmlContextFactory} uses the Simple XML framework to read
 * {@code infinitum.cfg.xml} and create the {@code InfinitumContext}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 06/25/12
 * @since 1.0
 */
public class XmlContextFactory extends ContextFactory {

	private static InfinitumContext sInfinitumContext;

	@Override
	public InfinitumContext configure(Context context) throws InfinitumConfigurationException {
		if (sInfinitumContext != null)
			return sInfinitumContext;
		mContext = context;
		Resources res = context.getResources();
		int id = res.getIdentifier("infinitum", "raw", context.getPackageName());
		if (id == 0)
			throw new InfinitumConfigurationException("Configuration infinitum.cfg.xml could not be found.");
		sInfinitumContext = configureFromXml(context, id);
		return sInfinitumContext;
	}

	@Override
	public InfinitumContext configure(Context context, int configId) throws InfinitumConfigurationException {
		if (sInfinitumContext != null)
			return sInfinitumContext;
		mContext = context;
		sInfinitumContext = configureFromXml(context, configId);
		return sInfinitumContext;
	}

	@Override
	public InfinitumContext getContext() throws InfinitumConfigurationException {
		if (sInfinitumContext == null)
			throw new InfinitumConfigurationException("Infinitum context not configured!");
		return sInfinitumContext;
	}

	@Override
	public PersistencePolicy getPersistencePolicy() {
		if (sInfinitumContext == null)
			throw new InfinitumConfigurationException("Infinitum context not configured!");
		return sInfinitumContext.getPersistencePolicy();
	}

	private InfinitumContext configureFromXml(Context context, int configId) {
		long start = Calendar.getInstance().getTimeInMillis();
		Resources resources = mContext.getResources();
		Serializer serializer = new Persister();
		try {
			InputStream stream = resources.openRawResource(configId);
			String xml = new Scanner(stream).useDelimiter("\\A").next();
			InfinitumContext ret = serializer.read(XmlApplicationContext.class, xml);
			if (ret == null)
				throw new InfinitumConfigurationException("Unable to initialize Infinitum configuration.");
			ret.postProcess(context);
			return ret;
		} catch (Exception e) {
			throw new InfinitumConfigurationException("Unable to initialize Infinitum configuration.", e);
		} finally {
			long stop = Calendar.getInstance().getTimeInMillis();
			Log.i(getClass().getSimpleName(), "Context initialized in " + (stop - start) + " milliseconds");
		}
	}

}
