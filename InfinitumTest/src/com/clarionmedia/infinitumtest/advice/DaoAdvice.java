package com.clarionmedia.infinitumtest.advice;

import com.clarionmedia.infinitum.aop.ProceedingJoinPoint;
import com.clarionmedia.infinitum.aop.annotation.Around;
import com.clarionmedia.infinitum.aop.annotation.Aspect;

@Aspect
public class DaoAdvice {

	@Around(within = {"com.clarionmedia.infinitumtest.dao"})
	public int around(ProceedingJoinPoint joinPoint) throws Exception {
		joinPoint.proceed();
		return 42;
	}

}
