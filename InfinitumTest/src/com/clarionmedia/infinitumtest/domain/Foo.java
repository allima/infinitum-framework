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
import com.clarionmedia.infinitum.orm.annotation.ManyToOne;

public class Foo extends AbstractBase {

	private String mFoo;
	
	@ManyToOne(className = "com.clarionmedia.infinitumtest.domain.Bar", name = "foo-bar", column = "bar")
	private Bar mBar;

	@ManyToMany(className = "com.clarionmedia.infinitumtest.domain.TestModel", foreignField = "mId", keyField = "mId", tableName = "testmodel_foo", name = "foo-testmodel")
	private List<TestModel> mRelated;

	public Foo() {
		setRelated(new ArrayList<TestModel>());
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
	
	public void addRelated(TestModel t) {
		mRelated.add(t);
	}

	public void setBar(Bar mBar) {
		this.mBar = mBar;
	}

	public Bar getBar() {
		return mBar;
	}

}
