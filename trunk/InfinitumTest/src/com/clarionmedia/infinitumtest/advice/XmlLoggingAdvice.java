package com.clarionmedia.infinitumtest.advice;

import android.util.Log;

import com.clarionmedia.infinitum.aop.JoinPoint;
import com.clarionmedia.infinitum.aop.ProceedingJoinPoint;

public class XmlLoggingAdvice {
	
	public void before(JoinPoint joinPoint) {
		Log.d("XmlLoggingAdvice", "Before advice: " + joinPoint.getMethod().getName());
	}
	
	public void after(JoinPoint joinPoint) {
		Log.d("XmlLoggingAdvice", "After advice: " + joinPoint.getMethod().getName());
	}
	
	public Object around(ProceedingJoinPoint joinPoint) throws Exception {
		Log.d("XmlLoggingAdvice", "Test around before");
		Object ret = joinPoint.proceed();
		Log.d("XmlLoggingAdvice", "Test around after");
		return ret;
	}

}
