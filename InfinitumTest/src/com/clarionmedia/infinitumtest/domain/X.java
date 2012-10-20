package com.clarionmedia.infinitumtest.domain;

import com.clarionmedia.infinitum.orm.annotation.OneToOne;

public class X {
	
	private long id;
	
	@OneToOne(className = "com.clarionmedia.infinitumtest.domain.Y", column = "y", name = "X-Y", owner = X.class)
	private Y y;

	public Y getY() {
		return y;
	}

	public void setY(Y y) {
		this.y = y;
	}

}
