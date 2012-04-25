package com.clarionmedia.infinitumtest.domain;

import java.util.LinkedList;
import java.util.List;

import com.clarionmedia.infinitum.orm.annotation.OneToMany;

public class Bar extends AbstractBase {
	
	private int mVal = 42;
	
	@OneToMany(className = "com.clarionmedia.infinitumtest.domain.Foo", column = "bar", name = "bar-foo")
	private List<Foo> mFoos;
	
	public Bar() {
		mFoos = new LinkedList<Foo>();
	}

	public void setFoos(List<Foo> mFoos) {
		this.mFoos = mFoos;
	}

	public List<Foo> getFoos() {
		return mFoos;
	}
	
	public void addFoo(Foo foo) {
		mFoos.add(foo);
	}

	public void setVal(int mVal) {
		this.mVal = mVal;
	}

	public int getVal() {
		return mVal;
	}

}
