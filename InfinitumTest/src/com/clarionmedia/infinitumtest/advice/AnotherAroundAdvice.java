package com.clarionmedia.infinitumtest.advice;

import android.util.Log;

import com.clarionmedia.infinitum.aop.ProceedingJoinPoint;
import com.clarionmedia.infinitum.aop.annotation.Around;
import com.clarionmedia.infinitum.aop.annotation.Aspect;

@Aspect
public class AnotherAroundAdvice {

	@Around(beans = { "myService"}, order = 1)
	public void around(ProceedingJoinPoint joinPoint) throws Throwable {
		Log.d("LoggingAdvice", "Around before " + joinPoint.getMethod().getName());
		joinPoint.proceed();
		Log.d("LoggingAdvice", "Around after " + joinPoint.getMethod().getName());
	}

}
