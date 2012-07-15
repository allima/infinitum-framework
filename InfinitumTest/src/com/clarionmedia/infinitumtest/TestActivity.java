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
import java.util.List;

import android.app.Activity;
import android.os.Bundle;

import com.clarionmedia.infinitum.context.ContextFactory;
import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.rest.TokenGenerator;
import com.clarionmedia.infinitumtest.service.MyService;
import com.clarionmedia.infinitumtest.service.MyTokenGenerator;

public class TestActivity extends Activity {

	public List<Field> mPrimaryKeys;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		InfinitumContext context = ContextFactory.getInstance().configure(this);
		
//		Session session = context.getSession(DataSource.Sqlite).registerDeserializer(Bar.class, new JsonDeserializer<Bar>() {
//			@Override
//			public Bar deserializeObject(String json) {
//				Gson gson = new Gson();
//				String formattedJson = json.substring(json.indexOf('{') + 1, json.lastIndexOf('}') + 1);
//				formattedJson = json.substring(formattedJson.indexOf('{') + 1, formattedJson.lastIndexOf('}') + 1);
//				return gson.fromJson(formattedJson, Bar.class);
//			}
//			@Override
//			public List<Bar> deserializeObjects(String json) {
//				List<Bar> ret = new LinkedList<Bar>();
//				Gson gson = new Gson();
//				json = json.substring(json.indexOf(":") + 1);
//				json = json.substring(json.indexOf("["));
//				json = json.substring(0, json.length() - 1);
//				JsonElement jsonElement = new JsonParser().parse(json);
//				JsonArray jsonArray = jsonElement.getAsJsonArray();
//				for (JsonElement e : jsonArray)
//						ret.add(gson.fromJson(e, Bar.class));
//				return ret;
//			}
//		});
		
		MyService service = (MyService) context.getBean("myService");
		service.foo();
		//TokenGenerator gen = (TokenGenerator) context.getBean("myTokenGenerator");
		//String token = gen.generateToken();
		
	}

}
