package com.clarionmedia.infinitumtest.dao.impl;

import android.util.Log;

import com.clarionmedia.infinitum.di.annotation.Bean;
import com.clarionmedia.infinitumtest.dao.MyDao;

@Bean("myDao")
public class MyDaoImpl implements MyDao {

	@Override
	public int test() {
		Log.i("MyDaoImpl", "Inside test");
		return 72;
	}

	@Override
	public void hello() {
		Log.i("MyDaoImpl", "hello world!");
	}

}
