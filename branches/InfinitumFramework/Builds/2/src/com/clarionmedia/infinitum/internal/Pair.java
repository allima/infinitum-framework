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

package com.clarionmedia.infinitum.internal;

/**
 * A generic data structure for holding two related objects.
 * 
 * @author Tyler Treat
 * @version 1.0 02/25/12
 */
public class Pair<F, S> {

	private F mFirst;
	private S mSecond;

	public Pair() {
	}

	public Pair(F first, S second) {
		setFirst(first);
		setSecond(second);
	}

	public F getFirst() {
		return mFirst;
	}

	public void setFirst(F first) {
		mFirst = first;
	}

	public S getSecond() {
		return mSecond;
	}

	public void setSecond(S second) {
		mSecond = second;
	}

}
