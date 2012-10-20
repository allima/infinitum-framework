package com.clarionmedia.infinitumtest.domain;

import com.clarionmedia.infinitum.orm.annotation.OneToOne;

public class SideA extends AbstractBase {
	
	private String mName = "hello";
	
	@OneToOne(className = "com.clarionmedia.infinitumtest.domain.SideB", column = "side_b", name = "A-B", owner = SideA.class)
	private SideB mSideB;

	public void setSideB(SideB mSideB) {
		this.mSideB = mSideB;
	}

	public SideB getSideB() {
		return mSideB;
	}

}
