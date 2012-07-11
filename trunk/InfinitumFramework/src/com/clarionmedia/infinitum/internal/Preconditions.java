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

package com.clarionmedia.infinitum.internal;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.res.XmlResourceParser;

import com.clarionmedia.infinitum.context.ContextFactory;
import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.exception.InvalidMapFileException;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;

/**
 * <p>
 * Utility class used to check method preconditions.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/18/12
 */
public class Preconditions {
	
	public static void checkNotNull(Object arg) {
		if (arg == null)
			throw new IllegalArgumentException("Argument may not be null.");
	}

	/**
	 * Verifies that if autocommit is disabled, a transaction is open.
	 * 
	 * @param autocommit
	 *            indicates if autocommit is enabled or disabled
	 * @param txOpen
	 *            indicates if there is a transaction open or not
	 */
	public static void checkForTransaction(boolean autocommit, boolean txOpen) {
		if (!autocommit && !txOpen)
			throw new InfinitumRuntimeException("Autocommit is disabled, but there is no open transaction.");
	}

	/**
	 * Verifies that the given model is persistent and can be saved, updated, or
	 * deleted.
	 * 
	 * @param model
	 *            model to check persistence for
	 */
	public static void checkPersistenceForModify(Object model) {
		InfinitumContext ctx = ContextFactory.getInstance().getContext();
		PersistencePolicy policy = ctx.getPersistencePolicy();
		if (!policy.isPersistent(model.getClass()))
			throw new InfinitumRuntimeException(String.format(new PropertyLoader(ctx.getAndroidContext())
					.getErrorMessage("CANNOT_MODIFY_TRANSIENT"), model.getClass().getName()));
	}

	/**
	 * Verifies that the given model {@link Class} is persistent and can be
	 * loaded.
	 * 
	 * @param c
	 *            {@code Class} to check persistence for
	 */
	public static void checkPersistenceForLoading(Class<?> c) {
		InfinitumContext ctx = ContextFactory.getInstance().getContext();
		PersistencePolicy policy = ctx.getPersistencePolicy();
		if (!policy.isPersistent(c))
			throw new InfinitumRuntimeException(String.format(
					new PropertyLoader(ctx.getAndroidContext()).getErrorMessage("CANNOT_LOAD_TRANSIENT"), c.getName()));
	}

	/**
	 * Verifies that the map file referenced by the given
	 * {@link XmlResourceParser} references the given {@link Class}.
	 * 
	 * @param c
	 *            the {@code Class} to check for
	 * @param parser
	 *            the {@code XmlResourceParser} reading the XML file
	 */
	public static void checkMapFileClass(Class<?> c, XmlResourceParser parser) {
		PropertyLoader propLoader = new PropertyLoader(ContextFactory.getInstance().getAndroidContext());
		try {
			int code = parser.getEventType();
			while (code != XmlPullParser.END_DOCUMENT) {
				if (code == XmlPullParser.START_TAG
						&& parser.getName().equalsIgnoreCase(propLoader.getPersistenceValue("ELEM_CLASS"))) {
					String name = parser.getAttributeValue(null, propLoader.getPersistenceValue("ATTR_NAME"));
					if (name == null)
						throw new InvalidMapFileException("'" + c.getName() + "' map file does not specify class name.");
					if (!name.equalsIgnoreCase(c.getName()))
						throw new InvalidMapFileException("'" + c.getName() + "' map file references the wrong class.");
					else
						break;
				}
				code = parser.next();
			}
		} catch (XmlPullParserException e) {
			throw new InvalidMapFileException("Unable to parse XML map file for '" + c.getName() + "'.");
		} catch (IOException e) {
			throw new InvalidMapFileException("Unable to parse XML map file for '" + c.getName() + "'.");
		}
	}

}
