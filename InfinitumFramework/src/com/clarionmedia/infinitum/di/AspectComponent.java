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

package com.clarionmedia.infinitum.di;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * <p>
 * Encapsulates the notion of an "aspect", which consists of a name or ID, a class,
 * and optional properties.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 07/18/12
 * @since 07/18/12
 */
@Root(strict = false)
public class AspectComponent extends BeanComponent {

	@Attribute(name = "id")
	private String mId;

	@Attribute(name = "src")
	private String mClass;

	@ElementList(required = false, entry = "property", inline = true)
	private List<Property> mProperties;

	public AspectComponent() {
		mProperties = new ArrayList<Property>();
	}
	
	public void setId(String id) {
		mId = id;
	}

	public String getId() {
		return mId;
	}
	
	public void setClassName(String className) {
		mClass = className;
	}

	public String getClassName() {
		return mClass;
	}

}