package com.clarionmedia.infinitumtest.service.impl;

import java.util.Date;

import android.util.Log;

import com.clarionmedia.infinitum.di.annotation.Autowired;
import com.clarionmedia.infinitum.di.annotation.Bean;
import com.clarionmedia.infinitumtest.dao.MyDao;
import com.clarionmedia.infinitumtest.service.MyService;

@Bean("myService")
public class MyServiceImpl implements MyService {
	
	@Autowired
	private MyDao mDao;

	@Override
	public void foo() {
		Log.i("MyServiceImpl", "Inside MyServiceImpl.foo()");
	}

	@Override
	public int bar(Integer x, Date y) {
		return 42;
	}

	@Override
	public int bar() {
		return 99;
	}

}
