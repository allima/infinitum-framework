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

package com.clarionmedia.infinitum.internal.caching;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Provides a time-expiration cache where cached values are asynchronously
 * evicted after a given timeout. Entries can be cached with their own
 * expiration time or rely on a default cache timeout.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 08/14/12
 * @since 1.0
 */
public class ExpirableCache<K, V> {

	private static final long DEFAULT_EXPIRATION_TIMEOUT = 60;

	private final Map<K, V> mCache;
	private final Map<K, Long> mTimeoutCache;
	private final long mDefaultExpirationTimeout;
	private final ExecutorService mThreadPool;

	/**
	 * Creates a new {@code ExpirableCache} using the default expiration timeout
	 * of 60 seconds.
	 */
	public ExpirableCache() {
		this(DEFAULT_EXPIRATION_TIMEOUT);
	}

	/**
	 * Creates a new {@code ExpirableCache} with with the given default
	 * expiration time.
	 * 
	 * @param defaultExpiration
	 *            the default expiration time in seconds
	 */
	public ExpirableCache(final long defaultExpiration) {
		if (defaultExpiration <= 0)
			throw new IllegalArgumentException("Cache expiration timeout must be greater than 0.");
		mCache = Collections.synchronizedMap(new HashMap<K, V>());
		mTimeoutCache = Collections.synchronizedMap(new HashMap<K, Long>());
		mDefaultExpirationTimeout = defaultExpiration;
		mThreadPool = Executors.newFixedThreadPool(256);
		Executors.newScheduledThreadPool(2).scheduleWithFixedDelay(
				new Runnable() {
					@Override
					public void run() {
						for (final K key : mTimeoutCache.keySet()) {
							if (System.currentTimeMillis() > mTimeoutCache.get(key))
								mThreadPool.execute(evictFromCache(key));
						}
					}
				}, mDefaultExpirationTimeout / 2, mDefaultExpirationTimeout, TimeUnit.SECONDS);
	}
	
	/**
	 * Returns the default expiration timeout for the cache.
	 * 
	 * @return default expiration timeout in seconds
	 */
	public long getDefaultExpirationTimeout() {
		return mDefaultExpirationTimeout;
	}

	/**
	 * Caches the given {@link Object} using the default expiration timeout.
	 * 
	 * @param key
	 *            the cache entry's key
	 * @param object
	 *            the {@code Object} to cache
	 */
	public void put(final K key, final V object) {
		put(key, object, mDefaultExpirationTimeout);
	}

	/**
	 * Caches the given {@link Object} using the given expiration timeout.
	 * 
	 * @param key
	 *            the cache entry's key
	 * @param object
	 *            the {@code Object} to cache
	 * @param expirationTimeout
	 *            the cache entry's expiration timeout in seconds
	 */
	public void put(final K key, final V object, final long expirationTimeout) {
		mCache.put(key, object);
		mTimeoutCache.put(key, System.currentTimeMillis() + expirationTimeout * 1000);
	}

	/**
	 * Returns the cache entry identified by the given key.
	 * 
	 * @param key
	 *            the key of the {@link Object} to retrieve
	 * @return the cached {@code Object} with the given key or {@code null} if
	 *         no entry exists (or if it expired)
	 */
	public V get(final K key) {
		final Long expireTime = mTimeoutCache.get(key);
		if (expireTime == null)
			return null;
		if (System.currentTimeMillis() > expireTime) {
			mThreadPool.execute(evictFromCache(key));
			return null;
		}
		return mCache.get(key);
	}

	/**
	 * Returns the cache entry identified by the given key.
	 * 
	 * @param key
	 *            the key of the {@link Object} to retrieve
	 * @param type
	 *            the type of the cache entry to retrieve
	 * @return the cached {@code Object} with the given key or {@code null} if
	 *         no entry exists (or if it expired)
	 */
	@SuppressWarnings("unchecked")
	public <R extends V> R get(final K key, final Class<R> type) {
		return (R) get(key);
	}

	/**
	 * Returns a {@link Runnable} that evicts the {@link Object} with the given
	 * key from the cache.
	 * 
	 * @param key
	 *            the key of the {@code Object} to evict
	 */
	private final Runnable evictFromCache(final K key) {
		return new Runnable() {
			public void run() {
				mCache.remove(key);
				mTimeoutCache.remove(key);
			}
		};
	}
	
}