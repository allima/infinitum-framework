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

package com.clarionmedia.infinitum.orm;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a domain model instance mapped to a database table.
 * 
 * @author Tyler Treat
 * @version 1.0 02/23/12
 */
public abstract class ModelMap {

	protected Object mModel;
	private List<Iterable<Object>> mRelationships;

	public ModelMap(Object model) {
		mModel = model;
		mRelationships = new ArrayList<Iterable<Object>>();
	}

	public Object getModel() {
		return mModel;
	}

	public void setModel(Object model) {
		mModel = model;
	}

	public List<Iterable<Object>> getRelationships() {
		return mRelationships;
	}

	public void setRelationships(List<Iterable<Object>> aggregates) {
		mRelationships = aggregates;
	}

	public void addRelationship(Iterable<Object> aggregate) {
		mRelationships.add(aggregate);
	}

}
