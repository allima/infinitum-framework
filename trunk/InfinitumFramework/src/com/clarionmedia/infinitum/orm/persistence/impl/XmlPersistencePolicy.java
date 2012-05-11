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

package com.clarionmedia.infinitum.orm.persistence.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.XmlResourceParser;

import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.exception.InvalidMapFileException;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.PersistenceConstants;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.persistence.TypeResolution;
import com.clarionmedia.infinitum.orm.relationship.ManyToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.ModelRelationship;
import com.clarionmedia.infinitum.reflection.ClassReflector;

/**
 * <p>
 * This class provides runtime resolution for model persistence through XML map
 * files ({@code imf.xml}). Each persistent entity should have an
 * {@code imf.xml} file associated with it and placed in res/xml. If an entity
 * has no such file, it is marked as transient.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 05/09/12
 */
public class XmlPersistencePolicy extends PersistencePolicy {

	private Context mContext;
	
	// This Map caches the resource ID for each persistent class's map file
	private Map<Class<?>, Integer> mResourceCache;
	
	// This Map caches the table name for each persistent class
	private Map<Class<?>, String> mTableCache;
	
	// This Map caches the cascade value for each persistent class
	private Map<Class<?>, Boolean> mCascadeCache;

	/**
	 * Constructs a new {@code XmlPersistencePolicy} with the given
	 * {@link Context}.
	 * 
	 * @param context
	 *            the {@code Context} to use for this policy
	 */
	public XmlPersistencePolicy(Context context) {
		super();
		mContext = context;
		mResourceCache = new HashMap<Class<?>, Integer>();
		mTableCache = new HashMap<Class<?>, String>();
		mCascadeCache = new HashMap<Class<?>, Boolean>();
	}

	@Override
	public boolean isPersistent(Class<?> c) {
		XmlResourceParser parser = loadXmlMapFile(c);
		return parser != null;
	}

	@Override
	public String getModelTableName(Class<?> c) throws IllegalArgumentException {
		if (mTableCache.containsKey(c))
			return mTableCache.get(c);
		String table;
		if (!isPersistent(c))
			throw new IllegalArgumentException("Class '" + c.getName()
					+ "' is transient.");
		XmlResourceParser parser = loadXmlMapFile(c);
		try {
			int code = parser.getEventType();
			while (code != XmlPullParser.END_DOCUMENT) {
				if (code == XmlPullParser.START_TAG
						&& parser.getName().equalsIgnoreCase(
								PersistenceConstants.ELEMENT_CLASS)) {
					String name = parser.getAttributeValue(null,
							PersistenceConstants.ATTR_NAME);
					if (name == null)
						throw new InvalidMapFileException("'" + c.getName()
								+ "' map file does not specify class name.");
					table = parser.getAttributeValue(null,
							PersistenceConstants.ATTR_TABLE);
					if (table == null) {
						if (name.contains("."))
							table = name.substring(name.lastIndexOf(".") + 1)
									.toLowerCase();
						else
							table = name.toLowerCase();
					}
					mTableCache.put(c, table);
					return table;
				}
				code = parser.next();
			}
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new InvalidMapFileException("'" + c.getName()
				+ "' map file does not specify class name.");
	}

	@Override
	public List<Field> getPersistentFields(Class<?> c) {
		if (mPersistenceCache.containsKey(c))
			return mPersistenceCache.get(c);
		List<Field> ret = new ArrayList<Field>();
		List<Field> fields = ClassReflector.getAllFields(c);
		for (Field f : fields) {
			if (Modifier.isStatic(f.getModifiers())
					|| TypeResolution.isDomainProxy(f.getDeclaringClass()))
				continue;
			if (isFieldPrimaryKey(f)) {
				ret.add(f);
				continue;
			}
			XmlResourceParser parser = loadXmlMapFile(c);
			try {
				int code = parser.getEventType();
				while (code != XmlPullParser.END_DOCUMENT) {
					if (code == XmlPullParser.START_TAG
							&& parser.getName().equalsIgnoreCase(
									PersistenceConstants.ELEMENT_PROPERTY)) {
						String name = parser.getAttributeValue(null,
								PersistenceConstants.ATTR_NAME);
						if (name == null)
							throw new InvalidMapFileException(
									"'"
											+ c.getName()
											+ "' map file does not specify property name.");
						if (name.equals(f.getName()))
							ret.add(f);
					}
					code = parser.next();
				}
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mPersistenceCache.put(c, ret);
		return ret;
	}

	@Override
	public Field findPersistentField(Class<?> c, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field getPrimaryKeyField(Class<?> c)
			throws ModelConfigurationException {
		if (mPrimaryKeyCache.containsKey(c))
			return mPrimaryKeyCache.get(c);
		if (!isPersistent(c))
			throw new IllegalArgumentException("Class '" + c.getName()
					+ "' is transient.");
		XmlResourceParser parser = loadXmlMapFile(c);
		try {
			int code = parser.getEventType();
			while (code != XmlPullParser.END_DOCUMENT) {
				if (code == XmlPullParser.START_TAG
						&& parser.getName().equalsIgnoreCase(
								PersistenceConstants.ELEMENT_PRIMARY_KEY)) {
					String name = parser.getAttributeValue(null,
							PersistenceConstants.ATTR_NAME);
					if (name == null)
						throw new InvalidMapFileException(
								"'"
										+ c.getName()
										+ "' map file does not specify primary key name.");
					Field f = ClassReflector.getField(c, name);
					if (f == null)
						throw new InvalidMapFileException(
								"'"
										+ c.getName()
										+ "' map file specifies a primary key which does not exist.");
					f.setAccessible(true);
					mPrimaryKeyCache.put(c, f);
					return f;
				}
				code = parser.next();
			}
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (c.getSuperclass() != null) {
			Field f = getPrimaryKeyField(c.getSuperclass());
			mPrimaryKeyCache.put(c, f);
			return f;
		}
		throw new InvalidMapFileException("'" + c.getName()
				+ "' map file does not specify primary key.");
	}

	@Override
	public List<Field> getUniqueFields(Class<?> c) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFieldColumnName(Field f) {
		if (mColumnCache.containsKey(f))
			return mColumnCache.get(f);
		Class<?> c = f.getDeclaringClass();
		XmlResourceParser parser = loadXmlMapFile(c);
		try {
			int code = parser.getEventType();
			while (code != XmlPullParser.END_DOCUMENT) {
				if (isFieldPrimaryKey(f)) {
					if (code == XmlPullParser.START_TAG
							&& parser.getName().equalsIgnoreCase(
									PersistenceConstants.ELEMENT_PRIMARY_KEY)) {
						String name = parser.getAttributeValue(null,
								PersistenceConstants.ATTR_NAME);
						if (name == null)
							throw new InvalidMapFileException(
									"'"
											+ c.getName()
											+ "' map file does not specify property name.");
						if (f.getName().equals(name)) {
							String column = parser.getAttributeValue(null,
									PersistenceConstants.ATTR_COLUMN);
							if (column == null) {
								if (name.startsWith("m") && name.length() > 1)
									column = name.substring(1).toLowerCase();
								else
									column = name.toLowerCase();
							}
							mColumnCache.put(f, column);
							return column;
						}
					}
				}
				if (code == XmlPullParser.START_TAG
						&& parser.getName().equalsIgnoreCase(
								PersistenceConstants.ELEMENT_PROPERTY)) {
					String name = parser.getAttributeValue(null,
							PersistenceConstants.ATTR_NAME);
					if (name == null)
						throw new InvalidMapFileException("'" + c.getName()
								+ "' map file does not specify property name.");
					if (f.getName().equals(name)) {
						String column = parser.getAttributeValue(null,
								PersistenceConstants.ATTR_COLUMN);
						if (column == null) {
							if (name.startsWith("m") && name.length() > 1)
								column = name.substring(1).toLowerCase();
							else
								column = name.toLowerCase();
						}
						mColumnCache.put(f, column);
						return column;
					}
				}
				code = parser.next();
			}
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new InvalidMapFileException("'" + c.getName()
				+ "' map file does not specify property '" + f.getName() + "'.");
	}

	@Override
	public boolean isFieldPrimaryKey(Field f) {
		return f.equals(getPrimaryKeyField(f.getDeclaringClass()));
	}

	@Override
	public boolean isPrimaryKeyAutoIncrement(Field f)
			throws InfinitumRuntimeException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFieldNullable(Field f) {
		if (mFieldNullableCache.containsKey(f))
			return mFieldNullableCache.get(f);
		Class<?> c = f.getDeclaringClass();
		XmlResourceParser parser = loadXmlMapFile(c);
		try {
			int code = parser.getEventType();
			while (code != XmlPullParser.END_DOCUMENT) {
				if (code == XmlPullParser.START_TAG
						&& parser.getName().equalsIgnoreCase(
								PersistenceConstants.ELEMENT_PROPERTY)) {
					String name = parser.getAttributeValue(null,
							PersistenceConstants.ATTR_NAME);
					if (name == null)
						throw new InvalidMapFileException("'" + c.getName() + "' map file does not specify property name.");
					if (name.equals(f.getName())) {
						String notNull = parser.getAttributeValue(null, PersistenceConstants.ATTR_NOT_NULL);
						if (notNull == null) {
							mFieldNullableCache.put(f, true);
							return true;
						} else {
							boolean nullable = Boolean.parseBoolean(notNull);
							mFieldNullableCache.put(f, nullable);
							return nullable;
						}
					}
				}
				code = parser.next();
			}
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new InvalidMapFileException("'" + c.getName()
				+ "' map file does not specify the property '" + f.getName() + "'.");
	}

	@Override
	public boolean isFieldUnique(Field f) {
		if (mFieldUniqueCache.containsKey(f))
			return mFieldUniqueCache.get(f);
		Class<?> c = f.getDeclaringClass();
		XmlResourceParser parser = loadXmlMapFile(c);
		try {
			int code = parser.getEventType();
			while (code != XmlPullParser.END_DOCUMENT) {
				if (code == XmlPullParser.START_TAG
						&& parser.getName().equalsIgnoreCase(
								PersistenceConstants.ELEMENT_PROPERTY)) {
					String name = parser.getAttributeValue(null,
							PersistenceConstants.ATTR_NAME);
					if (name == null)
						throw new InvalidMapFileException("'" + c.getName() + "' map file does not specify property name.");
					if (name.equals(f.getName())) {
						String unique = parser.getAttributeValue(null, PersistenceConstants.ATTR_UNIQUE);
						if (unique == null) {
							mFieldUniqueCache.put(f, false);
							return false;
						} else {
							boolean isUnique = Boolean.parseBoolean(unique);
							mFieldUniqueCache.put(f, isUnique);
							return isUnique;
						}
					}
				}
				code = parser.next();
			}
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new InvalidMapFileException("'" + c.getName()
				+ "' map file does not specify the property '" + f.getName() + "'.");
	}

	@Override
	public Set<ManyToManyRelationship> getManyToManyRelationships(Class<?> c) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCascading(Class<?> c) {
		if (mCascadeCache.containsKey(c))
			return mCascadeCache.get(c);
		if (!isPersistent(c))
			throw new IllegalArgumentException("Class '" + c.getName()
					+ "' is transient.");
		XmlResourceParser parser = loadXmlMapFile(c);
		try {
			int code = parser.getEventType();
			while (code != XmlPullParser.END_DOCUMENT) {
				if (code == XmlPullParser.START_TAG
						&& parser.getName().equalsIgnoreCase(
								PersistenceConstants.ELEMENT_CLASS)) {
					String name = parser.getAttributeValue(null,
							PersistenceConstants.ATTR_NAME);
					if (name == null)
						throw new InvalidMapFileException("'" + c.getName()
								+ "' map file does not specify class name.");
					String cascade = parser.getAttributeValue(null,
							PersistenceConstants.ATTR_CASCADE);
					if (cascade == null) {
						mCascadeCache.put(c, true);
						return true;
					} else {
						boolean isCascade = Boolean.parseBoolean(cascade);
						mCascadeCache.put(c, isCascade);
						return isCascade;
					}
				}
				code = parser.next();
			}
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new InvalidMapFileException("'" + c.getName()
				+ "' map file does not specify class name.");
	}

	@Override
	public boolean isRelationship(Field f) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ModelRelationship getRelationship(Field f) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field findRelationshipField(Class<?> c, ModelRelationship rel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLazy(Class<?> c) {
		if (mLazyLoadingCache.containsKey(c))
			return mLazyLoadingCache.get(c);
		if (!isPersistent(c))
			throw new IllegalArgumentException("Class '" + c.getName()
					+ "' is transient.");
		XmlResourceParser parser = loadXmlMapFile(c);
		try {
			int code = parser.getEventType();
			while (code != XmlPullParser.END_DOCUMENT) {
				if (code == XmlPullParser.START_TAG
						&& parser.getName().equalsIgnoreCase(
								PersistenceConstants.ELEMENT_CLASS)) {
					String name = parser.getAttributeValue(null,
							PersistenceConstants.ATTR_NAME);
					if (name == null)
						throw new InvalidMapFileException("'" + c.getName()
								+ "' map file does not specify class name.");
					String lazy = parser.getAttributeValue(null,
							PersistenceConstants.ATTR_LAZY);
					if (lazy == null) {
						mLazyLoadingCache.put(c, true);
						return true;
					} else {
						boolean isLazy = Boolean.parseBoolean(lazy);
						mCascadeCache.put(c, isLazy);
						return isLazy;
					}
				}
				code = parser.next();
			}
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new InvalidMapFileException("'" + c.getName()
				+ "' map file does not specify class name.");
	}

	@Override
	public String getRestfulResource(Class<?> c)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getResourceFieldName(Field f) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	private XmlResourceParser loadXmlMapFile(Class<?> c) {
		Resources res = mContext.getResources();
		if (!mResourceCache.containsKey(c)) {
			String fileName = c.getSimpleName().toLowerCase();
			int id = res.getIdentifier(fileName, "xml",
					mContext.getPackageName());
			mResourceCache.put(c, id);
		}
		try {
			return res.getXml(mResourceCache.get(c));
		} catch (NotFoundException e) {
			return null;
		}
	}

}
