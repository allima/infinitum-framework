package com.clarionmedia.infinitumtest.advice;

import android.util.Log;

import com.clarionmedia.infinitum.aop.JoinPoint;
import com.clarionmedia.infinitum.aop.annotation.After;
import com.clarionmedia.infinitum.aop.annotation.Aspect;
import com.clarionmedia.infinitum.aop.annotation.Before;

@Aspect
public class LoggingAdvice {

//	@Before(beans = { "myService", "myTokenGenerator" }, order = 1)
//	public void before(JoinPoint joinPoint) {
//		Log.d("LoggingAdvice", "Before advice: " + joinPoint.getMethod().getName());
//	}
//
//	@After(beans = { "myService", "myTokenGenerator" }, order = 1)
//	public void after(JoinPoint joinPoint) {
//		Log.d("LoggingAdvice", "After advice: " + joinPoint.getMethod().getName());
//	}

}
