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

package com.clarionmedia.infinitumtest.test;

import android.test.ActivityInstrumentationTestCase2;
import com.clarionmedia.infinitum.context.ApplicationContext;
import com.clarionmedia.infinitum.context.ApplicationContext.ConfigurationMode;
import com.clarionmedia.infinitumtest.ApplicationContextActivity;

/**
 * Unit test for testing {@link ApplicationContext},
 * {@link ApplicationContextFactory}, and supporting code.
 * 
 * @author Tyler Treat
 * @version 1.0 02/15/12
 */
public class ApplicationContextTest extends ActivityInstrumentationTestCase2<ApplicationContextActivity> {

	private static final String DOMAIN_PACKAGE = "com.clarionmedia.infinitumtest.domain";
	private static final String DB_NAME = "test";
	private static final int DB_VERSION = 2;
	private static final ConfigurationMode CONFIG_MODE = ConfigurationMode.Annotation;

	private ApplicationContextActivity mActivity;
	private ApplicationContext mAppContext;

	public ApplicationContextTest() {
		super("com.clarionmedia.infinitumtest", ApplicationContextActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mActivity = this.getActivity();
		mAppContext = mActivity.mAppContext;
	}

	public void testPreconditions() {
		assertNotNull(mAppContext);
	}

	public void testApplicationContextFactory() {
		assertEquals(DOMAIN_PACKAGE, mAppContext.getDomainPackage());
		assertEquals(true, mAppContext.hasSqliteDb());
		assertEquals(DB_NAME, mAppContext.getSqliteDbName());
		assertEquals(DB_VERSION, mAppContext.getSqliteDbVersion());
		assertEquals(CONFIG_MODE, mAppContext.getConfigurationMode());
		assertEquals(true, mAppContext.isDebug());
	}

}
