package com.clarionmedia.infinitumtest.domain;

import com.clarionmedia.infinitum.orm.annotation.Column;
import com.clarionmedia.infinitum.orm.annotation.Entity;
import com.clarionmedia.infinitum.orm.annotation.PrimaryKey;
import com.clarionmedia.infinitum.orm.annotation.Table;

@Entity
@Table(name = "test")
public class TestModel {
	
	@PrimaryKey
	private long mId;

	@Column(name = "field")
	private String mMyField;
	
	private int mX;
	
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

	public int getX() {
		return mX;
	}

	public void setX(int x) {
		mX = x;
	}

}
