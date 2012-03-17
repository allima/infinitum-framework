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

import java.lang.reflect.Field;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;

import com.clarionmedia.infinitum.context.InfinitumContext.DataSource;
import com.clarionmedia.infinitum.context.InfinitumContextFactory;
import com.clarionmedia.infinitum.orm.Session;
import com.clarionmedia.infinitumtest.domain.Foo;

public class SqliteTemplateActivity extends Activity {

	public List<Field> mPrimaryKeys;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Session session = InfinitumContextFactory.configure(this,
				R.xml.infinitum).getSession(this, DataSource.Sqlite);
		session.open();
		
		Foo f = new Foo("test");
		session.save(f);
		
		session.close();
	}

}
