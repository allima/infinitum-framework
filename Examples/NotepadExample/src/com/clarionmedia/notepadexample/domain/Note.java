package com.clarionmedia.notepadexample.domain;

public class Note {
	
	private long mId;
	private String mName;
	private String mContents;
	
	public long getId() {
		return mId;
	}
	
	public String getName() {
		return mName;
	}
	
	public void setName(String name) {
		mName = name;
	}
	
	public String getContents() {
		return mContents;
	}
	
	public void setContents(String contents) {
		mContents = contents;
	}

}
