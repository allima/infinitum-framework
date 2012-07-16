package com.clarionmedia.infinitumtest.advice;

import android.util.Log;

import com.clarionmedia.infinitum.aop.JoinPoint;
import com.clarionmedia.infinitum.aop.annotation.After;
import com.clarionmedia.infinitum.aop.annotation.Aspect;
import com.clarionmedia.infinitum.aop.annotation.Before;

@Aspect
public class TestAdvice {

	@Before(beans = {"myService.bar(*)"})
	public void before(JoinPoint joinPoint) {
		Log.d("TestAdvice", "Before advice: " + joinPoint.getMethod().getName());
	}

	@After(beans = {"myService.bar(*)"})
	public void after(JoinPoint joinPoint) {
		Log.d("TestAdvice", "After advice: " + joinPoint.getMethod().getName());
	}

}
