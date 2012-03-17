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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.clarionmedia.infinitum.orm.OrmConstants.PersistenceMode;
import com.clarionmedia.infinitum.orm.annotation.Column;
import com.clarionmedia.infinitum.orm.annotation.ManyToMany;
import com.clarionmedia.infinitum.orm.annotation.NotNull;
import com.clarionmedia.infinitum.orm.annotation.Persistence;
import com.clarionmedia.infinitum.orm.annotation.Unique;

public class TestModel extends AbstractBase {

	@Column("field")
	private String mMyField;

	private int mFoo;

	@NotNull
	private Date mBar;

	@Unique
	private double mBam;

	@Persistence(PersistenceMode.Transient)
	private float mTransient;

	@ManyToMany(className = "com.clarionmedia.infinitumtest.domain.Foo", foreignField = "mId", keyField = "mId", tableName = "testmodel_foo", name = "testmodel-foo")
	private List<Foo> mRelated;

	public TestModel() {
		Random rand = new Random(Calendar.getInstance().getTimeInMillis());
		mBar = new Date();
		mFoo = rand.nextInt();
		mMyField = "hello world!";
		mBam = rand.nextDouble();
		mTransient = 42;
		setRelated(new ArrayList<Foo>());
	}

	public String getMyField() {
		return mMyField;
	}

	public void setMyField(String myField) {
		mMyField = myField;
	}

	public int getFoo() {
		return mFoo;
	}

	public void setFoo(int foo) {
		mFoo = foo;
	}

	public Date getBar() {
		return mBar;
	}

	public void setBar(Date bar) {
		mBar = bar;
	}

	public double getBam() {
		return mBam;
	}

	public void setBam(double bam) {
		mBam = bam;
	}

	public float getTransient() {
		return mTransient;
	}

	public void setTransient(float t) {
		mTransient = t;
	}

	public List<Foo> getRelated() {
		return mRelated;
	}

	public void setRelated(List<Foo> related) {
		mRelated = related;
	}
	
	public void addRelated(Foo foo) {
		mRelated.add(foo);
	}

}
