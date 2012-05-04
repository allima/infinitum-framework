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

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;

import com.clarionmedia.infinitum.context.InfinitumContextFactory;
import com.clarionmedia.infinitum.rest.JsonDeserializer;
import com.clarionmedia.infinitum.rest.impl.BasicRestfulClient;
import com.clarionmedia.infinitum.rest.impl.RestfulClientFactory;
import com.clarionmedia.infinitumtest.domain.Bar;
import com.clarionmedia.infinitumtest.service.MyService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class TestActivity extends Activity {

	public List<Field> mPrimaryKeys;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		InfinitumContextFactory.getInstance().configure(this, R.xml.infinitum);
		BasicRestfulClient rest = (BasicRestfulClient) new RestfulClientFactory().registerJsonDeserializer(Bar.class, new JsonDeserializer<Bar>() {
			@Override
			public Bar deserializeObject(String json) {
				Gson gson = new Gson();
				String formattedJson = json.substring(json.indexOf('{') + 1, json.lastIndexOf('}') + 1);
				formattedJson = json.substring(formattedJson.indexOf('{') + 1, formattedJson.lastIndexOf('}') + 1);
				return gson.fromJson(formattedJson, Bar.class);
			}
			@Override
			public List<Bar> deserializeObjects(String json) {
				List<Bar> ret = new LinkedList<Bar>();
				Gson gson = new Gson();
				json = json.substring(json.indexOf(":") + 1);
				json = json.substring(json.indexOf("["));
				json = json.substring(0, json.length() - 1);
				JsonElement jsonElement = new JsonParser().parse(json);
				JsonArray jsonArray = jsonElement.getAsJsonArray();
				for (JsonElement e : jsonArray)
						ret.add(gson.fromJson(e, Bar.class));
				return ret;
			}
		}).build();
		Bar b = rest.load(Bar.class, 1L);
		
		MyService service = (MyService) InfinitumContextFactory.getInstance().getInfinitumContext().getBean("myService");
		service.getClass();
	}

}
