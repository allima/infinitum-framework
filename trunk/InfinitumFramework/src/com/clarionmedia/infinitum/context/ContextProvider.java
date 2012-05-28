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

import android.content.Context;
import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;
import com.clarionmedia.infinitum.context.impl.ContextFactory;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;

/**
 * <p>
 * Provides access to an {@link InfinitumContext} singleton. This is typically
 * not used directly, rather the implementation {@link ContextFactory} is used.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 05/18/12
 */
public interface ContextProvider {

	/**
	 * Configures Infinitum with the implicit configuration file
	 * {@code res/xml/infinitum.cfg.xml}. This method must be called before
	 * attempting to retrieve an {@link InfinitumContext}.
	 * 
	 * @param context
	 *            the calling {@link Context}.
	 * @return configured {@code InfinitumContext}
	 * @throws InfinitumConfigurationException
	 *             if the implied configuration file could not be found or if
	 *             the file could not be parsed
	 */
	InfinitumContext configure(Context context)
			throws InfinitumConfigurationException;

	/**
	 * Configures Infinitum with the specified configuration file. This method
	 * must be called before attempting to retrieve an {@link InfinitumContext}.
	 * 
	 * @param context
	 *            the calling {@code Context}
	 * @param configId
	 *            the resource ID for the XML configuration file
	 * @return configured {@code InfinitumContext}
	 * @throws InfinitumConfigurationException
	 *             if the configuration file could not be found or if the file
	 *             could not be parsed
	 */
	InfinitumContext configure(Context context, int configId)
			throws InfinitumConfigurationException;

	/**
	 * Retrieves the {@link InfinitumContext} singleton.
	 * {@link ContextFactory#configure} must be called before using this method.
	 * Otherwise, an {@link InfinitumConfigurationException} will be thrown.
	 * 
	 * @return the {@code InfinitumContext} singleton
	 * @throws InfinitumConfigurationException
	 *             if {@code configure} was not called
	 */
	InfinitumContext getContext() throws InfinitumConfigurationException;

	/**
	 * Retrieves the configured {@link PersistencePolicy}.
	 * 
	 * @return {@code PersistencePolicy}
	 */
	PersistencePolicy getPersistencePolicy();

	/**
	 * Retrieves the Android {@link Context} registered with the configured
	 * {@link InfinitumContext}.
	 * 
	 * @return {@code Context}
	 */
	Context getAndroidContext();

}
