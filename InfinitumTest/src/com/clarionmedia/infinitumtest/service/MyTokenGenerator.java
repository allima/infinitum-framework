package com.clarionmedia.infinitumtest.service;

import com.clarionmedia.infinitum.rest.TokenGenerator;

public class MyTokenGenerator implements TokenGenerator {

	@Override
	public String generateToken() {
		return "abc123";
	}

}
