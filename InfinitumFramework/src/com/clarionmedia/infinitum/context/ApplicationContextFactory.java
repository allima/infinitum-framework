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

/**
 * Provides access to an {@link ApplicationContext} singleton. In order for this
 * class to work, an infinitum.cfg.xml file must be created and placed in the
 * project's assets directory.
 * 
 * @author Tyler Treat
 * @version 1.0 02/11/12
 */
public class ApplicationContextFactory {
	
	private static ApplicationContext sApplicationContext;
	
	public ApplicationContext getApplicationContext() {
		if (sApplicationContext != null)
			return sApplicationContext;
		else {
			sApplicationContext = new ApplicationContext();
			// TODO
			return sApplicationContext;
		}
	}

}
