package com.clarionmedia.infinitumtest.domain;

import com.clarionmedia.infinitum.orm.annotation.OneToOne;

public class Y {
	
	private long id;
	
	@OneToOne(className = "com.clarionmedia.infinitumtest.domain.X", column = "x", name = "Y-X", owner = Y.class)
	private X x;

	public X getX() {
		return x;
	}

	public void setX(X x) {
		this.x = x;
	}

}
