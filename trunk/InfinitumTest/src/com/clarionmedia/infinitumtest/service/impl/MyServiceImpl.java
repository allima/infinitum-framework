package com.clarionmedia.infinitumtest.service.impl;

import com.clarionmedia.infinitum.di.annotation.Autowired;
import com.clarionmedia.infinitumtest.dao.MyDao;
import com.clarionmedia.infinitumtest.service.MyService;

public class MyServiceImpl implements MyService {
	
	private MyDao mDao;
	
	@Autowired
	public MyServiceImpl(MyDao dao) {
		mDao = dao;
	}

}
