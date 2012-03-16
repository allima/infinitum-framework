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

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * <p>
 * Used to proxy a collection which has been configured to lazily load.
 * {@code LazilyLoadedCollection} is implemented as a paged collection, meaning
 * that pages are loaded and then unloaded as the collection is iterated over in
 * a lazy fashion.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/15/12
 */
public abstract class LazilyLoadedCollection<E> extends AbstractCollection<E> {

	private final int mSize;

	/**
	 * Constructs a new {@code LazilyLoadedCollection} of the given size. Note
	 * that, although the size is specified, not all elements are loaded into
	 * memory. This is implemented as a lazily-loaded, paged collection, meaning
	 * that pages are loaded and then unloaded as the collection is iterated
	 * over in a lazy fashion.
	 * 
	 * @param size
	 *            the number of items the proxy collection contains
	 */
	public LazilyLoadedCollection(int size) {
		mSize = size;
	}

	@Override
	public Iterator<E> iterator() {
		return new LazilyLoadedCollectionIterator();
	}

	@Override
	public int size() {
		return mSize;
	}

	/**
	 * Loads a page of the given size and offset into memory.
	 * 
	 * @param offset
	 *            the index to start loading from
	 * @param pageSize
	 *            the number of items to load
	 * @return loaded page
	 */
	public abstract Collection<E> loadPage(int offset, int pageSize);

	/**
	 * Unloads the given page from memory.
	 * 
	 * @param page
	 *            the page to unload
	 */
	public abstract void unloadPage(Collection<E> page);

	/**
	 * Iterator for {@link LazilyLoadedCollection}. This {@code Iterator} takes
	 * care of loading and unloading pages on-the-fly.
	 * 
	 * @author Tyler Treat
	 * @version 1.0 03/15/12
	 */
	private class LazilyLoadedCollectionIterator implements Iterator<E> {

		private static final int PAGE_SIZE = 100;

		private int mOffset = 0;
		private Collection<E> mCurrentCollection = new ArrayList<E>();
		private Iterator<E> mCurrentIterator = mCurrentCollection.iterator();

		@Override
		public boolean hasNext() {
			boolean onLastPage = mOffset >= mSize;
			return mCurrentIterator.hasNext() || !onLastPage;
		}

		@Override
		public E next() {
			if (!mCurrentIterator.hasNext()) {
				nextIterator();
			}
			return mCurrentIterator.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("remove not implemented");
		}

		private void nextIterator() {
			mCurrentCollection = loadPage(mOffset, PAGE_SIZE);
			mOffset += mCurrentCollection.size();
			mCurrentIterator = mCurrentCollection.iterator();
		}

	}
}
