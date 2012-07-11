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

import com.clarionmedia.infinitum.internal.Pair;
import com.clarionmedia.infinitum.orm.relationship.ManyToOneRelationship;
import com.clarionmedia.infinitum.orm.relationship.ModelRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToOneRelationship;

/**
 * <p>
 * Represents a domain model instance mapped to a database table.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/23/12
 */
public abstract class ModelMap {

	protected Object mModel;
	private List<Pair<ModelRelationship, Iterable<Object>>> mAggregateRelationships;
	private List<Pair<ManyToOneRelationship, Object>> mManyToOneRelationships;
	private List<Pair<OneToManyRelationship, Iterable<Object>>> mOneToManyRelationships;
	private List<Pair<OneToOneRelationship, Object>> mOneToOneRelationships;

	public ModelMap(Object model) {
		mModel = model;
		mAggregateRelationships = new ArrayList<Pair<ModelRelationship, Iterable<Object>>>();
		mOneToManyRelationships = new ArrayList<Pair<OneToManyRelationship, Iterable<Object>>>();
		mManyToOneRelationships = new ArrayList<Pair<ManyToOneRelationship, Object>>();
		mOneToOneRelationships = new ArrayList<Pair<OneToOneRelationship, Object>>();
	}

	public Object getModel() {
		return mModel;
	}

	public void setModel(Object model) {
		mModel = model;
	}

	public List<Pair<ModelRelationship, Iterable<Object>>> getAggregateRelationships() {
		return mAggregateRelationships;
	}

	public void setAggregateRelationships(List<Pair<ModelRelationship, Iterable<Object>>> aggregates) {
		mAggregateRelationships = aggregates;
	}

	public void addAggregateRelationship(Pair<ModelRelationship, Iterable<Object>> aggregate) {
		mAggregateRelationships.add(aggregate);
	}

	public List<Pair<ManyToOneRelationship, Object>> getManyToOneRelationships() {
		return mManyToOneRelationships;
	}

	public void setManyToOneRelationships(List<Pair<ManyToOneRelationship, Object>> manyToOneRelationships) {
		mManyToOneRelationships = manyToOneRelationships;
	}
	
	public void addManyToOneRelationship(Pair<ManyToOneRelationship, Object> relationship) {
		mManyToOneRelationships.add(relationship);
	}

	public void setOneToOneRelationships(List<Pair<OneToOneRelationship, Object>> mOneToOneRelationships) {
		this.mOneToOneRelationships = mOneToOneRelationships;
	}

	public List<Pair<OneToOneRelationship, Object>> getOneToOneRelationships() {
		return mOneToOneRelationships;
	}
	
	public void addOneToOneRelationship(Pair<OneToOneRelationship, Object> relationship) {
		mOneToOneRelationships.add(relationship);
	}

	public void setOneToManyRelationships(List<Pair<OneToManyRelationship, Iterable<Object>>> mOneToManyRelationships) {
		this.mOneToManyRelationships = mOneToManyRelationships;
	}

	public List<Pair<OneToManyRelationship, Iterable<Object>>> getOneToManyRelationships() {
		return mOneToManyRelationships;
	}
	
	public void addOneToManyRelationship(Pair<OneToManyRelationship, Iterable<Object>> relationship) {
		mOneToManyRelationships.add(relationship);
	}

}
