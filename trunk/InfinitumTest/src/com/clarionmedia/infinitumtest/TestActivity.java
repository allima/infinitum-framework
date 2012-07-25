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

import android.os.Bundle;

import com.clarionmedia.infinitum.activity.InfinitumActivity;
import com.clarionmedia.infinitum.context.InfinitumContext.DataSource;
import com.clarionmedia.infinitum.orm.Session;
import com.clarionmedia.infinitumtest.domain.X;
import com.clarionmedia.infinitumtest.domain.Y;

public class TestActivity extends InfinitumActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Session session = getInfinitumContext().getSession(DataSource.Sqlite);
		
		X x = new X();
		x.setY(new Y());
		session.open();
		session.save(x);
		session.close();
	}

}
