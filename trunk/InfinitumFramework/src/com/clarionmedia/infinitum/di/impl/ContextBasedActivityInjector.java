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
import java.util.List;

import android.app.Activity;
import android.content.res.Resources;
import android.view.animation.AnimationUtils;

import com.clarionmedia.infinitum.di.ActivityInjector;
import com.clarionmedia.infinitum.di.annotation.InjectLayout;
import com.clarionmedia.infinitum.di.annotation.InjectResource;
import com.clarionmedia.infinitum.di.annotation.InjectView;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.reflection.ClassReflector;
import com.clarionmedia.infinitum.reflection.impl.DefaultClassReflector;

/**
 * <p>
 * Implementation of {@link ActivityInjector} for injecting Android resources
 * and framework components into an {@link Activity}.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 07/18/12
 * @since 1.0
 */
public class ContextBasedActivityInjector implements ActivityInjector {

	private Activity mActivity;
	private ClassReflector mClassReflector;
	private List<Field> mFields;

	public ContextBasedActivityInjector(Activity activity) {
		mActivity = activity;
		mClassReflector = new DefaultClassReflector();
		mFields = mClassReflector.getAllFields(activity.getClass());
	}

	@Override
	public void inject() {
		injectLayout();
		injectViews();
		injectResources();
	}

	/**
	 * Injects the {@code Activity} layout based on the {@code @InjectLayout}
	 * annotation. This takes the place of calling
	 * {@link Activity#setContentView(int)}.
	 */
	private void injectLayout() {
		InjectLayout injectLayout = mActivity.getClass().getAnnotation(InjectLayout.class);
		if (injectLayout == null)
			return;
		mActivity.setContentView(injectLayout.value());
	}

	/**
	 * Injects fields annotated with {@code @InjectView}.
	 */
	private void injectViews() {
		for (Field field : mFields) {
			if (!field.isAnnotationPresent(InjectView.class))
				continue;
			InjectView injectView = field.getAnnotation(InjectView.class);
			int viewId = injectView.value();
			field.setAccessible(true);
			try {
				field.set(mActivity, mActivity.findViewById(viewId));
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Injects the fields annotated with {@code @InjectResource}.
	 */
	private void injectResources() {
		for (Field field : mFields) {
			if (!field.isAnnotationPresent(InjectResource.class))
				continue;
			InjectResource injectResource = field.getAnnotation(InjectResource.class);
			int resourceId = injectResource.value();
			field.setAccessible(true);
			try {
				Object resource = resolveResourceForField(field, resourceId);
				field.set(mActivity, resource);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Loads the appropriate resource based on the {@link Field} type and the
	 * given resource ID.
	 */
	private Object resolveResourceForField(Field field, int resourceId) {
		Resources resources = mActivity.getResources();
		String resourceType = resources.getResourceTypeName(resourceId);
		if (resourceType.equalsIgnoreCase("anim"))
			return AnimationUtils.loadAnimation(mActivity, resourceId);
		if (resourceType.equalsIgnoreCase("drawable"))
			return resources.getDrawable(resourceId);
		if (resourceType.equalsIgnoreCase("color"))
			return resources.getColor(resourceId);
		if (resourceType.equalsIgnoreCase("string"))
			return resources.getString(resourceId);
		if (resourceType.equalsIgnoreCase("integer"))
			return resources.getInteger(resourceId);
		if (resourceType.equalsIgnoreCase("bool"))
			return resources.getBoolean(resourceId);
		if (resourceType.equalsIgnoreCase("dimen"))
			return resources.getDimension(resourceId);
		if (resourceType.equalsIgnoreCase("movie"))
			return resources.getMovie(resourceId);
		if (resourceType.equalsIgnoreCase("array")) {
			if (field.getType() == int[].class || field.getType() == Integer[].class)
				return resources.getIntArray(resourceId);
			else if (field.getType() == String[].class || field.getType() == CharSequence[].class)
				return resources.getStringArray(resourceId);
			else
				return resources.obtainTypedArray(resourceId); // TODO: convert
																// to actual
																// array
		}
		if (resourceType.equalsIgnoreCase("id"))
			throw new InfinitumRuntimeException("Unable to inject field '" + field.getName() + "' in Activity '"
					+ mActivity.getClass().getName() + "'. Are you injecting a view?");
		throw new InfinitumRuntimeException("Unable to inject field '" + field.getName() + "' in Activity '"
				+ mActivity.getClass().getName() + "' (unsupported type).");
	}

}
