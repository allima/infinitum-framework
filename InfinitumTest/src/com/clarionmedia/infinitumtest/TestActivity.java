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

package com.clarionmedia.infinitumtest;

import android.os.Bundle;
import android.view.View;

import com.clarionmedia.infinitum.activity.InfinitumActivity;
import com.clarionmedia.infinitum.di.annotation.InjectLayout;
import com.clarionmedia.infinitum.di.annotation.InjectResource;

@InjectLayout(R.layout.main)
public class TestActivity extends InfinitumActivity {

	@InjectResource(R.id.hello)
	private View mView;
	
	
//    <color name="my_color">#ffffff</color>
//    
//    <dimen name="my_dimen">10dp</dimen>
//    
//    <bool name="my_bool">true</bool>
//    
//    <integer name="my_int">42</integer>
//    
//    <array name="my_typed_arr">
//        <item>@drawable/shape</item>
//    </array>

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String hello = "hello";
		hello.getClass();
	}

}
