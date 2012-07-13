package com.clarionmedia.infinitumtest.advice;

import com.clarionmedia.infinitum.aop.JoinPoint;
import com.clarionmedia.infinitum.aop.annotation.After;
import com.clarionmedia.infinitum.aop.annotation.Aspect;
import com.clarionmedia.infinitum.aop.annotation.Before;
import com.clarionmedia.infinitum.logging.Logger;

@Aspect
public class LoggingAdvice {

	@Before(beans = { "myService" })
	public void before(JoinPoint joinPoint) {
		Logger logger = Logger.getInstance(joinPoint.getTarget().getClass()
				.getName());
		logger.debug("Entering " + joinPoint.getMethod().getName());
	}

	@After(beans = { "myService" })
	public void after(JoinPoint joinPoint) {
		Logger logger = Logger.getInstance(joinPoint.getTarget().getClass()
				.getName());
		logger.debug("Exiting " + joinPoint.getMethod().getName());
	}

}
