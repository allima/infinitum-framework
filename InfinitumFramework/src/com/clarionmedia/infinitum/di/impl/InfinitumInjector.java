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

package com.clarionmedia.infinitum.di.impl;

import java.lang.reflect.Field;

import android.app.Activity;

import com.clarionmedia.infinitum.di.Injector;
import com.clarionmedia.infinitum.di.annotation.InjectView;
import com.clarionmedia.infinitum.reflection.ClassReflector;
import com.clarionmedia.infinitum.reflection.impl.DefaultClassReflector;

public class InfinitumInjector implements Injector {
	
	private ClassReflector mClassReflector;
	
	public InfinitumInjector() {
		mClassReflector = new DefaultClassReflector();
	}

	@Override
	public void injectViews(Activity activity) {
		for (Field field : mClassReflector.getAllFields(activity.getClass())) {
			if (!field.isAnnotationPresent(InjectView.class))
				continue;
			InjectView injectView = field.getAnnotation(InjectView.class);
			int viewId = injectView.value();
			field.setAccessible(true);
			try {
				field.set(activity, activity.findViewById(viewId));
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
