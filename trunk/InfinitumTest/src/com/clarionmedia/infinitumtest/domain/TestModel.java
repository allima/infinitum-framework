package com.clarionmedia.infinitumtest.domain;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import com.clarionmedia.infinitum.orm.Constants.PersistenceMode;
import com.clarionmedia.infinitum.orm.annotation.Column;
import com.clarionmedia.infinitum.orm.annotation.NotNull;
import com.clarionmedia.infinitum.orm.annotation.Persistence;
import com.clarionmedia.infinitum.orm.annotation.Unique;

public class TestModel {

	private long mId;

	@Column(name = "field")
	private String mMyField;

	private int mFoo;

	@NotNull
	private Date mBar;

	@Unique
	private double mBam;

	@Persistence(mode = PersistenceMode.Transient)
	private float mTransient;

	public TestModel() {
		Random rand = new Random(Calendar.getInstance().getTimeInMillis());
		mBar = new Date();
		mFoo = rand.nextInt();
		mMyField = "hello world!";
		mBam = rand.nextDouble();
		mTransient = 42;
	}

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		mId = id;
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

}
