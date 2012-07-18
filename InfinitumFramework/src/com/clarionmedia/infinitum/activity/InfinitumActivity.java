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

package com.clarionmedia.infinitum.activity;

import android.app.Activity;
import android.os.Bundle;
import com.clarionmedia.infinitum.context.ContextFactory;
import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.di.Injector;

public class InfinitumActivity extends Activity {

	private InfinitumContext mContext;

	protected int mInfinitumConfigId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mContext = mInfinitumConfigId == 0 ? ContextFactory.getInstance()
				.configure(this) : ContextFactory.getInstance().configure(this,
				mInfinitumConfigId);
		final Injector injector = mContext.getInjector(this);
		injector.injectViews(this);
	}

	/**
	 * Returns the {@link InfinitumContext} for the {@code InfinitumActivity}.
	 * 
	 * @return {@code InfinitumContext}
	 */
	protected InfinitumContext getInfinitumContext() {
		return mContext;
	}

}
