package com.clarionmedia.infinitumtest.advice;

import android.util.Log;

import com.clarionmedia.infinitum.aop.ProceedingJoinPoint;
import com.clarionmedia.infinitum.aop.annotation.Around;
import com.clarionmedia.infinitum.aop.annotation.Aspect;

@Aspect
public class LoggingAroundAdvice {

	@Around(beans = { "myService" }, order = 2)
	public void around(ProceedingJoinPoint joinPoint) throws Throwable {
		Log.d("LoggingAdvice", "Entering " + joinPoint.getMethod().getName());
		joinPoint.proceed();
		Log.d("LoggingAdvice", "Exiting " + joinPoint.getMethod().getName());
	}

}
