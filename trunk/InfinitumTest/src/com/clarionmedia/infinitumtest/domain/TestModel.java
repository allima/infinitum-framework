package com.clarionmedia.infinitumtest.domain;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import com.clarionmedia.infinitum.orm.annotation.Column;
import com.clarionmedia.infinitum.orm.annotation.Table;
import com.clarionmedia.infinitum.orm.annotation.Unique;

@Table(name = "test")
public class TestModel {

	private long mId;

	@Column(name = "field")
	private String mMyField;

	private int mFoo;

	private Date mBar;

	@Unique
	private double mBam;

	public TestModel() {
		Random rand = new Random(Calendar.getInstance().getTimeInMillis());
		mBar = new Date();
		mFoo = rand.nextInt();
		mMyField = "hello world!";
		setBam(rand.nextDouble());
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

}
