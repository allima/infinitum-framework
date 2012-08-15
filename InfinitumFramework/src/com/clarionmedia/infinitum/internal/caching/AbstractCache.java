package com.clarionmedia.infinitum.internal.caching;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

/**
 * <p>
 * A simple 2-level cache consisting of a small and fast in-memory cache (1st
 * level cache) and an (optional) slower but bigger disk cache (2nd level
 * cache). For disk caching, either the application's cache directory or the SD
 * card can be used. Please note that in the case of the app cache dir, Android
 * may at any point decide to wipe that entire directory if it runs low on
 * internal storage. The SD card cache <i>must</i> be managed by the
 * application, e.g. by calling {@link #wipe} whenever the app quits.
 * </p>
 * <p>
 * When pulling from the cache, it will first attempt to load the data from
 * memory. If that fails, it will try to load it from disk (assuming disk
 * caching is enabled). If that succeeds, the data will be put in the in-memory
 * cache and returned (read-through). Otherwise it's a cache miss.
 * </p>
 * <p>
 * Pushes to the cache are always write-through (i.e. the data will be stored
 * both on disk, if disk caching is enabled, and in memory).
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 08/15/12
 * @since 1.0
 */
public abstract class AbstractCache<K, V> implements Map<K, V> {

	public static final int DISK_CACHE_INTERNAL = 0;
	public static final int DISK_CACHE_SDCARD = 1;
	
	private static final String LOG_TAG = "Inf Caching";
	
	protected String mDiskCacheDirectory;
	private boolean mIsDiskCacheEnabled;
	private ExpirableCache<K, V> mCache;
	private String mName;
	private long mDefaultExpirationTimeout;

	/**
	 * Creates a new cache instance.
	 * 
	 * @param name
	 *            a human readable identifier for this cache. Note that this
	 *            value will be used to derive a directory name if the disk
	 *            cache is enabled, so don't get too creative here (camel case
	 *            names work great)
	 * @param initialCapacity
	 *            the initial element size of the cache
	 * @param defaultExpiration
	 *            the default expiration timeout in seconds
	 */
	public AbstractCache(String name, int initialCapacity, long defaultExpiration) {
		mName = name;
		mDefaultExpirationTimeout = defaultExpiration;
		mCache = new ExpirableCache<K, V>(mDefaultExpirationTimeout);
	}

	/**
	 * Sanitize disk cache. Remove files which are older than
	 * expirationInMinutes.
	 */
	private void sanitizeDiskCache() {
		File[] cachedFiles = new File(mDiskCacheDirectory).listFiles();
		if (cachedFiles == null) {
			return;
		}
		for (File f : cachedFiles) {
			// if file older than expirationInMinutes, remove it
			long lastModified = f.lastModified();
			Date now = new Date();
			long ageInMinutes = ((now.getTime() - lastModified) / (1000 * 60));

			if (ageInMinutes >= mDefaultExpirationTimeout) {
				Log.d(mName, "DISK cache expiration for file " + f.toString());
				f.delete();
			}
		}
	}

	/**
	 * Enable caching to the phone's internal storage or SD card.
	 * 
	 * @param context
	 *            the current context
	 * @param storageDevice
	 *            where to store the cached files, either
	 *            {@link #DISK_CACHE_INTERNAL} or {@link #DISK_CACHE_SDCARD})
	 * @return
	 */
	public boolean enableDiskCache(Context context, int storageDevice) {
		Context appContext = context.getApplicationContext();

		String rootDir = null;
		if (storageDevice == DISK_CACHE_SDCARD
				&& Environment.MEDIA_MOUNTED.equals(Environment
						.getExternalStorageState())) {
			// SD-card available
			rootDir = Environment.getExternalStorageDirectory()
					.getAbsolutePath()
					+ "/Android/data/"
					+ appContext.getPackageName() + "/cache";
		} else {
			File internalCacheDir = appContext.getCacheDir();
			// apparently on some configurations this can come back as null
			if (internalCacheDir == null) {
				return (mIsDiskCacheEnabled = false);
			}
			rootDir = internalCacheDir.getAbsolutePath();
		}

		setRootDir(rootDir);

		File outFile = new File(mDiskCacheDirectory);
		if (outFile.mkdirs()) {
			File nomedia = new File(mDiskCacheDirectory, ".nomedia");
			try {
				nomedia.createNewFile();
			} catch (IOException e) {
				Log.e(LOG_TAG, "Failed creating .nomedia file");
			}
		}

		mIsDiskCacheEnabled = outFile.exists();

		if (!mIsDiskCacheEnabled) {
			Log.w(LOG_TAG, "Failed creating disk cache directory "
					+ mDiskCacheDirectory);
		} else {
			Log.d(mName, "enabled write through to " + mDiskCacheDirectory);

			// sanitize disk cache
			Log.d(mName, "sanitize DISK cache");
			sanitizeDiskCache();
		}

		return mIsDiskCacheEnabled;
	}

	private void setRootDir(String rootDir) {
		this.mDiskCacheDirectory = rootDir + "/infinitum/" + mName.replaceAll("\\s", "");
	}

	/**
	 * Only meaningful if disk caching is enabled. See {@link #enableDiskCache}.
	 * 
	 * @return the full absolute path to the directory where files are cached,
	 *         if the disk cache is enabled, otherwise null
	 */
	public String getDiskCacheDirectory() {
		return mDiskCacheDirectory;
	}

	/**
	 * Only meaningful if disk caching is enabled. See {@link #enableDiskCache}.
	 * Turns a cache key into the file name that will be used to persist the
	 * value to disk. Subclasses must implement this.
	 * 
	 * @param key
	 *            the cache key
	 * @return the file name
	 */
	public abstract String getFileNameForKey(K key);

	/**
	 * Only meaningful if disk caching is enabled. See {@link #enableDiskCache}.
	 * Restores a value previously persisted to the disk cache.
	 * 
	 * @param file
	 *            the file holding the cached value
	 * @return the cached value
	 * @throws IOException
	 */
	protected abstract V readValueFromDisk(File file) throws IOException;

	/**
	 * Only meaningful if disk caching is enabled. See {@link #enableDiskCache}.
	 * Persists a value to the disk cache.
	 * 
	 * @param ostream
	 *            the file output stream (buffered).
	 * @param value
	 *            the cache value to persist
	 * @throws IOException
	 */
	protected abstract void writeValueToDisk(File file, V value)
			throws IOException;

	private void cacheToDisk(K key, V value) {
		File file = new File(mDiskCacheDirectory + "/" + getFileNameForKey(key));
		try {
			file.createNewFile();
			file.deleteOnExit();

			writeValueToDisk(file, value);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private File getFileForKey(K key) {
		return new File(mDiskCacheDirectory + "/" + getFileNameForKey(key));
	}

	/**
	 * Reads a value from the cache by probing the in-memory cache, and if
	 * enabled and the in-memory probe was a miss, the disk cache.
	 * 
	 * @param elementKey
	 *            the cache key
	 * @return the cached value, or null if element was not cached
	 */
	@SuppressWarnings("unchecked")
	public synchronized V get(Object elementKey) {
		K key = (K) elementKey;
		V value = mCache.get(key);
		if (value != null) {
			// memory hit
			Log.d(mName, "MEM cache hit for " + key.toString());
			return value;
		}

		// memory miss, try reading from disk
		File file = getFileForKey(key);
		if (file.exists()) {
			// if file older than expirationInMinutes, remove it
			long lastModified = file.lastModified();
			Date now = new Date();
			long ageInMinutes = ((now.getTime() - lastModified) / (1000 * 60));

			if (ageInMinutes >= mDefaultExpirationTimeout) {
				Log.d(mName, "DISK cache expiration for file " + file.toString());
				file.delete();
				return null;
			}

			// disk hit
			Log.d(mName, "DISK cache hit for " + key.toString());
			try {
				value = readValueFromDisk(file);
			} catch (IOException e) {
				// treat decoding errors as a cache miss
				e.printStackTrace();
				return null;
			}
			if (value == null) {
				return null;
			}
			mCache.put(key, value);
			return value;
		}

		// cache miss
		return null;
	}

	/**
	 * Writes an element to the cache. NOTE: If disk caching is enabled, this
	 * will write through to the disk, which may introduce a performance
	 * penalty.
	 */
	public synchronized V put(K key, V value) {
		if (mIsDiskCacheEnabled) {
			cacheToDisk(key, value);
		}

		return mCache.put(key, value);
	}

	public synchronized void putAll(Map<? extends K, ? extends V> t) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Checks if a value is present in the cache. If the disk cached is enabled,
	 * this will also check whether the value has been persisted to disk.
	 * 
	 * @param key
	 *            the cache key
	 * @return true if the value is cached in memory or on disk, false otherwise
	 */
	@SuppressWarnings("unchecked")
	public synchronized boolean containsKey(Object key) {
		return mCache.containsKey(key) || (mIsDiskCacheEnabled && getFileForKey((K) key).exists());
	}

	/**
	 * Checks if a value is present in the in-memory cache. This method ignores
	 * the disk cache.
	 * 
	 * @param key
	 *            the cache key
	 * @return true if the value is currently hold in memory, false otherwise
	 */
	public synchronized boolean containsKeyInMemory(Object key) {
		return mCache.containsKey(key);
	}

	/**
	 * Checks if the given value is currently hold in memory.
	 */
	public synchronized boolean containsValue(Object value) {
		return mCache.containsValue(value);
	}

	@SuppressWarnings("unchecked")
	public synchronized V remove(Object key) {
		V value = removeKey(key);

		if (mIsDiskCacheEnabled) {
			File cachedValue = getFileForKey((K) key);
			if (cachedValue.exists()) {
				cachedValue.delete();
			}
		}

		return value;
	}

	// Forced key expiration
	public V removeKey(Object key) {
		return mCache.remove(key);
	}

	public Set<K> keySet() {
		return mCache.keySet();
	}

	public Set<Map.Entry<K, V>> entrySet() {
		return mCache.entrySet();
	}

	public synchronized int size() {
		return mCache.size();
	}

	public synchronized boolean isEmpty() {
		return mCache.isEmpty();
	}

	public boolean isDiskCacheEnabled() {
		return mIsDiskCacheEnabled;
	}

	/**
	 * 
	 * @param rootDir
	 *            a folder name to enable caching or null to disable it.
	 */
	public void setDiskCacheEnabled(String rootDir) {
		if (rootDir != null && rootDir.length() > 0) {
			setRootDir(rootDir);
			this.mIsDiskCacheEnabled = true;
		} else {
			this.mIsDiskCacheEnabled = false;
		}
	}

	public synchronized void clear() {
		mCache.clear();

		if (mIsDiskCacheEnabled) {
			File[] cachedFiles = new File(mDiskCacheDirectory).listFiles();
			if (cachedFiles == null) {
				return;
			}
			for (File f : cachedFiles) {
				f.delete();
			}
		}

		Log.d(LOG_TAG, "Cache cleared");
	}

	public Collection<V> values() {
		return mCache.values();
	}
}