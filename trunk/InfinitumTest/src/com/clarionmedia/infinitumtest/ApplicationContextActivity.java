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

package com.clarionmedia.infinitumtest;

import android.app.Activity;
import android.os.Bundle;

import com.clarionmedia.infinitum.context.ApplicationContext;
import com.clarionmedia.infinitum.context.ApplicationContextFactory;

public class ApplicationContextActivity extends Activity {

	public ApplicationContext mAppContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		ApplicationContextFactory.configure(this, R.xml.infinitum);
		mAppContext = ApplicationContextFactory.getApplicationContext();
	}

}