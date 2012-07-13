package com.clarionmedia.infinitumtest.rest;

import java.io.Serializable;

import com.clarionmedia.infinitum.di.annotation.Autowired;
import com.clarionmedia.infinitum.di.annotation.Bean;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.Session;
import com.clarionmedia.infinitum.rest.Deserializer;
import com.clarionmedia.infinitum.rest.impl.RestfulSession;
import com.clarionmedia.infinitumtest.service.MyService;

@Bean
public class MyRestClient extends RestfulSession {
	
	private MyService mService;
	
	@Autowired
	public void setMyService(MyService service) {
		mService = service;
	}

	@Override
	public <T> T load(Class<T> type, Serializable id)
			throws InfinitumRuntimeException, IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Session registerDeserializer(Class<T> type,
			Deserializer<T> deserializer) {
		// TODO Auto-generated method stub
		return this;
	}

}
