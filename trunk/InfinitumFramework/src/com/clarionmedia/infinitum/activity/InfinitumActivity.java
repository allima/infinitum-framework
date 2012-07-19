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
import com.clarionmedia.infinitum.di.ActivityInjector;
import com.clarionmedia.infinitum.di.impl.ContextBasedActivityInjector;

/**
 * <p>
 * This {@link Activity} provides dependency injection support to any inheriting
 * {@code Activity} as well as access to an {@link InfinitumContext}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 07/18/12
 * @since 1.0
 */
public class InfinitumActivity extends Activity {

	private InfinitumContext mContext;
	private int mInfinitumConfigId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mContext = mInfinitumConfigId == 0 ? ContextFactory.getInstance().configure(this) : ContextFactory
				.getInstance().configure(this, mInfinitumConfigId);
		final ActivityInjector injector = new ContextBasedActivityInjector(this);
		injector.inject();
		super.onCreate(savedInstanceState);
	}

	/**
	 * Returns the {@link InfinitumContext} for the {@code InfinitumActivity}.
	 * 
	 * @return {@code InfinitumContext}
	 */
	protected InfinitumContext getInfinitumContext() {
		return mContext;
	}

	/**
	 * Sets the resource ID of the Infinitum XML config to use.
	 * 
	 * @param configId
	 *            Infinitum config ID
	 */
	protected void setInfinitumConfigId(int configId) {
		mInfinitumConfigId = configId;
	}

}
