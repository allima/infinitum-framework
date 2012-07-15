package com.clarionmedia.infinitumtest.service.impl;

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

}
