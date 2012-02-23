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

package com.clarionmedia.infinitumtest.domain;

import java.util.ArrayList;
import java.util.List;

import com.clarionmedia.infinitum.orm.annotation.ManyToMany;

public class Foo extends AbstractBase {

	private String mFoo;

	@ManyToMany(className = "com.clarionmedia.infinitumtest.domain.TestModel", foreignField = "mId", keyField = "mId", tableName = "testmodel_foo")
	private List<TestModel> mRelated;

	public Foo() {
	}

	public Foo(String foo) {
		mFoo = foo;
		setRelated(new ArrayList<TestModel>());
	}

	public String getFoo() {
		return mFoo;
	}

	public void setFoo(String foo) {
		mFoo = foo;
	}

	public List<TestModel> getRelated() {
		return mRelated;
	}

	public void setRelated(List<TestModel> related) {
		mRelated = related;
	}

}
