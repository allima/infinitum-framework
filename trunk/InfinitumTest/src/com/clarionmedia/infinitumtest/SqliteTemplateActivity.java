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
import com.clarionmedia.infinitum.context.ApplicationContextFactory;
import com.clarionmedia.infinitum.orm.ManyToManyRelationship;
import com.clarionmedia.infinitum.orm.criteria.GenCriteria;
import com.clarionmedia.infinitum.orm.criteria.criterion.Conditions;
import com.clarionmedia.infinitum.orm.persistence.PersistenceResolution;
import com.clarionmedia.infinitum.orm.sql.SqlBuilder;
import com.clarionmedia.infinitum.orm.sqlite.SqliteTemplate;
import com.clarionmedia.infinitumtest.domain.Foo;
import com.clarionmedia.infinitumtest.domain.TestModel;

public class SqliteTemplateActivity extends Activity {

	public List<Field> mPrimaryKeys;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		ApplicationContextFactory.configure(this, R.xml.infinitum);
		TestModel model = new TestModel();
		SqliteTemplate sqlite = new SqliteTemplate(this);
		sqlite.open();
		// sqlite.save(model);
		// TestModel loaded = sqlite.load(TestModel.class, 2L);
		TestModel loaded = sqlite.load(TestModel.class, 42L);
		sqlite.close();
	}

}
