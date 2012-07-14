/*
 * Copyright (c) 2012 Tyler Treat
 * 
 * This file is part of Infinitum Framework.
 *
 * Infinitum Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Infinitum Framework is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Infinitum Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.clarionmedia.infinitum.aop;

import java.util.HashSet;
import java.util.Set;

import com.clarionmedia.infinitum.internal.Preconditions;

/**
 * <p>
 * Abstract specialization of {@link AopProxy} for weaving aspect advice into
 * join points.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 07/14/12
 * @since 1.0
 */
public abstract class AdvisedProxy extends AopProxy {

	protected Set<JoinPoint> mBeforeAdvice;
	protected Set<JoinPoint> mAfterAdvice;
	protected Set<JoinPoint> mAroundAdvice;

	/**
	 * Creates a new {@code AdvisedProxy} with the given {@link Pointcut}.
	 * 
	 * @param pointcut
	 *            the {@code Pointcut} to provide advice
	 */
	public AdvisedProxy(Pointcut pointcut) {
		Preconditions.checkNotNull(pointcut);
		mBeforeAdvice = new HashSet<JoinPoint>();
		mAfterAdvice = new HashSet<JoinPoint>();
		mAroundAdvice = new HashSet<JoinPoint>();
		for (JoinPoint joinPoint : pointcut.getJoinPoints()) {
			switch (joinPoint.getLocation()) {
			case Before:
				mBeforeAdvice.add(joinPoint);
				break;
			case After:
				mAfterAdvice.add(joinPoint);
				break;
			case Around:
				mAroundAdvice.add(joinPoint);
				break;
			}
		}
	}

}
