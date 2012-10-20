package com.clarionmedia.infinitumtest.service;

import com.clarionmedia.infinitum.di.annotation.Bean;
import com.clarionmedia.infinitum.http.rest.TokenGenerator;

@Bean
public class MyTokenGenerator implements TokenGenerator {

	@Override
	public String generateToken() {
		return "abc123";
	}

}
