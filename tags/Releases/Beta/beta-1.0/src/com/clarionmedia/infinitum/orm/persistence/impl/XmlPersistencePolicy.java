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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;

import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.internal.StringUtil;
import com.clarionmedia.infinitum.orm.exception.InvalidMapFileException;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.relationship.ManyToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.ManyToOneRelationship;
import com.clarionmedia.infinitum.orm.relationship.ModelRelationship;
import com.clarionmedia.infinitum.orm.relationship.ModelRelationship.RelationType;
import com.clarionmedia.infinitum.orm.relationship.OneToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToOneRelationship;

/**
 * <p>
 * This class provides runtime resolution for model persistence through XML map
 * files ({@code imf.xml}). Each persistent entity should have an
 * {@code imf.xml} file associated with it and placed in res/raw. If an entity
 * has no such file, it is marked as transient.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 05/09/12
 * @since 1.0
 * @see AnnotationsPersistencePolicy
 */
public class XmlPersistencePolicy extends PersistencePolicy {
	
	// TODO This code is disgusting and I am ashamed of it
	// This should be re-implemented sometime...
	// Maybe use Simple Framework for XML deserialization?

	private InfinitumContext mContext;

	// This Map caches the resource ID for each persistent class's map file
	private Map<Class<?>, Integer> mResourceCache;

	// This Map caches the table name for each persistent class
	private Map<Class<?>, String> mTableCache;

	// This Map caches the cascade value for each persistent class
	private Map<Class<?>, Cascade> mCascadeCache;

	// This Map caches the autoincrement value for each persistent class's
	// primary key
	private Map<Field, Boolean> mAutoincrementCache;

	// This Map caches whether a Field is a relationship
	private Map<Field, Boolean> mRelationshipCheckCache;

	/**
	 * Constructs a new {@code XmlPersistencePolicy} with the given
	 * {@link InfinitumContext}.
	 * 
	 * @param context
	 *            the {@code InfinitumContext} to use for this policy
	 */
	public XmlPersistencePolicy(InfinitumContext context) {
		super(context);
		mContext = context;
		mResourceCache = new HashMap<Class<?>, Integer>();
		mTableCache = new HashMap<Class<?>, String>();
		mCascadeCache = new HashMap<Class<?>, Cascade>();
		mAutoincrementCache = new HashMap<Field, Boolean>();
		mRelationshipCheckCache = new HashMap<Field, Boolean>();
	}

	@Override
	public boolean isPersistent(Class<?> c) {
		XmlPullParser parser = loadXmlMapFile(c);
		return parser != null;
	}

	@Override
	public String getModelTableName(Class<?> c) {
		if (mTableCache.containsKey(c))
			return mTableCache.get(c);
		String table;
		if (!isPersistent(c))
			throw new IllegalArgumentException("Class '" + c.getName() + "' is transient.");
		XmlPullParser parser = loadXmlMapFile(c);
		try {
			int code = parser.getEventType();
			while (code != XmlPullParser.END_DOCUMENT) {
				if (code == XmlPullParser.START_TAG
						&& parser.getName().equalsIgnoreCase(mPropLoader.getPersistenceValue("ELEM_CLASS"))) {
					String name = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_NAME"));
					if (name == null)
						throw new InvalidMapFileException("'" + c.getName() + "' map file does not specify class name.");
					table = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_TABLE"));
					if (table == null) {
						if (name.contains("."))
							table = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
						else
							table = name.toLowerCase();
					}
					mTableCache.put(c, table);
					return table;
				}
				code = parser.next();
			}
		} catch (XmlPullParserException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		} catch (IOException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		}
		throw new InvalidMapFileException("'" + c.getName() + "' map file does not specify class.");
	}

	@Override
	public List<Field> getPersistentFields(Class<?> c) {
		if (mPersistenceCache.containsKey(c))
			return mPersistenceCache.get(c);
		if (!isPersistent(c))
			throw new IllegalArgumentException("Class '" + c.getName() + "' is transient.");
		List<Field> ret = new ArrayList<Field>();
		List<Field> fields = mClassReflector.getAllFields(c);
		for (Field f : fields) {
			if (Modifier.isStatic(f.getModifiers()) || mTypePolicy.isDomainProxy(f.getDeclaringClass()))
				continue;
			if (isFieldPrimaryKey(f)) {
				ret.add(f);
				continue;
			}
			XmlPullParser parser = loadXmlMapFile(c);
			try {
				int code = parser.getEventType();
				while (code != XmlPullParser.END_DOCUMENT) {
					if (code == XmlPullParser.START_TAG
							&& parser.getName().equalsIgnoreCase(mPropLoader.getPersistenceValue("ELEM_PROPERTY"))) {
						String name = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_NAME"));
						if (name == null)
							throw new InvalidMapFileException("'" + c.getName()
									+ "' map file does not specify property name.");
						if (name.equals(f.getName()))
							ret.add(f);
					} else if (code == XmlPullParser.START_TAG
							&& (parser.getName().equalsIgnoreCase(mPropLoader.getPersistenceValue("ELEM_MTM"))
									|| parser.getName().equalsIgnoreCase(mPropLoader.getPersistenceValue("ELEM_OTM"))
									|| parser.getName().equalsIgnoreCase(mPropLoader.getPersistenceValue("ELEM_MTO")) || parser
									.getName().equalsIgnoreCase(mPropLoader.getPersistenceValue("ELEM_OTO")))) {
						String field = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_FIELD"));
						if (field == null)
							throw new InvalidMapFileException("'" + c.getName()
									+ "' map file does not specify relation field.");
						if (field.equals(f.getName()))
							ret.add(f);
					}
					code = parser.next();
				}
			} catch (XmlPullParserException e) {
				mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
			} catch (IOException e) {
				mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
			}
		}
		mPersistenceCache.put(c, ret);
		return ret;
	}

	@Override
	public Field getPrimaryKeyField(Class<?> c) throws ModelConfigurationException {
		if (mPrimaryKeyCache.containsKey(c))
			return mPrimaryKeyCache.get(c);
		if (!isPersistent(c))
			throw new IllegalArgumentException("Class '" + c.getName() + "' is transient.");
		XmlPullParser parser = loadXmlMapFile(c);
		try {
			int code = parser.getEventType();
			while (code != XmlPullParser.END_DOCUMENT) {
				if (code == XmlPullParser.START_TAG
						&& parser.getName().equalsIgnoreCase(mPropLoader.getPersistenceValue("ELEM_PRIMARY_KEY"))) {
					String name = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_NAME"));
					if (name == null)
						throw new InvalidMapFileException("'" + c.getName()
								+ "' map file does not specify primary key name.");
					Field f = mClassReflector.getField(c, name);
					if (f == null)
						throw new InvalidMapFileException("'" + c.getName()
								+ "' map file specifies a primary key which does not exist.");
					f.setAccessible(true);
					mPrimaryKeyCache.put(c, f);
					return f;
				}
				code = parser.next();
			}
		} catch (XmlPullParserException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		} catch (IOException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		}
		if (c.getSuperclass() != null) {
			Field f = getPrimaryKeyField(c.getSuperclass());
			mPrimaryKeyCache.put(c, f);
			return f;
		}
		throw new InvalidMapFileException("'" + c.getName() + "' map file does not specify primary key.");
	}

	@Override
	public String getFieldColumnName(Field f) {
		if (mColumnCache.containsKey(f))
			return mColumnCache.get(f);
		Class<?> c = f.getDeclaringClass();
		if (!isPersistent(c))
			throw new IllegalArgumentException("Class '" + c.getName() + "' is transient.");
		XmlPullParser parser = loadXmlMapFile(c);
		try {
			int code = parser.getEventType();
			while (code != XmlPullParser.END_DOCUMENT) {
				if (isFieldPrimaryKey(f)) {
					if (code == XmlPullParser.START_TAG
							&& parser.getName().equalsIgnoreCase(mPropLoader.getPersistenceValue("ELEM_PRIMARY_KEY"))) {
						String name = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_NAME"));
						if (name == null)
							throw new InvalidMapFileException("'" + c.getName()
									+ "' map file does not specify property name.");
						if (f.getName().equals(name)) {
							String column = parser.getAttributeValue(null,
									mPropLoader.getPersistenceValue("ATTR_COLUMN"));
							if (column == null)
								column = StringUtil.formatFieldName(name);
							mColumnCache.put(f, column);
							return column;
						}
					}
				}
				if (code == XmlPullParser.START_TAG
						&& parser.getName().equalsIgnoreCase(mPropLoader.getPersistenceValue("ELEM_PROPERTY"))) {
					String name = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_NAME"));
					if (name == null)
						throw new InvalidMapFileException("'" + c.getName()
								+ "' map file does not specify property name.");
					if (f.getName().equals(name)) {
						String column = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_COLUMN"));
						if (column == null)
							column = StringUtil.formatFieldName(name);
						mColumnCache.put(f, column);
						return column;
					}
				}
				code = parser.next();
			}
		} catch (XmlPullParserException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		} catch (IOException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		}
		throw new InvalidMapFileException("'" + c.getName() + "' map file does not specify property '" + f.getName()
				+ "'.");
	}

	@Override
	public boolean isFieldPrimaryKey(Field f) {
		Class<?> c = f.getDeclaringClass();
		if (!isPersistent(c))
			throw new IllegalArgumentException("Class '" + c.getName() + "' is transient.");
		return f.equals(getPrimaryKeyField(f.getDeclaringClass()));
	}

	@Override
	public boolean isPrimaryKeyAutoIncrement(Field f) throws InfinitumRuntimeException {
		if (mAutoincrementCache.containsKey(f))
			return mAutoincrementCache.get(f);
		Class<?> c = f.getDeclaringClass();
		if (!isPersistent(c))
			throw new IllegalArgumentException("Class '" + c.getName() + "' is transient.");
		XmlPullParser parser = loadXmlMapFile(c);
		try {
			int code = parser.getEventType();
			while (code != XmlPullParser.END_DOCUMENT) {
				if (code == XmlPullParser.START_TAG
						&& parser.getName().equalsIgnoreCase(mPropLoader.getPersistenceValue("ELEM_PRIMARY_KEY"))) {
					String name = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_NAME"));
					if (name == null)
						throw new InvalidMapFileException("'" + c.getName()
								+ "' map file does not specify primary key name.");
					if (!name.equals(f.getName()))
						continue;
					String type = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_TYPE"));
					if (type == null)
						throw new InvalidMapFileException("'" + c.getName()
								+ "' map file does not specify primary key type.");
					String autoincrement = parser.getAttributeValue(null,
							mPropLoader.getPersistenceValue("ATTR_AUTOINCREMENT"));
					if (type.equalsIgnoreCase("int") || type.equalsIgnoreCase("integer")
							|| type.equalsIgnoreCase("long")) {
						if (autoincrement == null) {
							mAutoincrementCache.put(f, true);
							return true;
						} else {
							boolean isAutoIncr = Boolean.parseBoolean(autoincrement);
							mAutoincrementCache.put(f, isAutoIncr);
							return isAutoIncr;
						}
					} else {
						mAutoincrementCache.put(f, false);
						return false;
					}
				}
				code = parser.next();
			}
		} catch (XmlPullParserException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		} catch (IOException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		}
		throw new InvalidMapFileException("'" + c.getName() + "' map file does not specify primary key.");
	}

	@Override
	public boolean isFieldNullable(Field f) {
		if (mFieldNullableCache.containsKey(f))
			return mFieldNullableCache.get(f);
		if (isFieldPrimaryKey(f)) {
			mFieldNullableCache.put(f, false);
			return false;
		}
		Class<?> c = f.getDeclaringClass();
		if (!isPersistent(c))
			throw new IllegalArgumentException("Class '" + c.getName() + "' is transient.");
		XmlPullParser parser = loadXmlMapFile(c);
		try {
			int code = parser.getEventType();
			while (code != XmlPullParser.END_DOCUMENT) {
				if (code == XmlPullParser.START_TAG
						&& parser.getName().equalsIgnoreCase(mPropLoader.getPersistenceValue("ELEM_PROPERTY"))) {
					String name = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_NAME"));
					if (name == null)
						throw new InvalidMapFileException("'" + c.getName()
								+ "' map file does not specify property name.");
					if (name.equals(f.getName())) {
						String notNull = parser.getAttributeValue(null,
								mPropLoader.getPersistenceValue("ATTR_NOT_NULL"));
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
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		} catch (IOException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		}
		throw new InvalidMapFileException("'" + c.getName() + "' map file does not specify the property '"
				+ f.getName() + "'.");
	}

	@Override
	public boolean isFieldUnique(Field f) {
		if (mFieldUniqueCache.containsKey(f))
			return mFieldUniqueCache.get(f);
		if (isFieldPrimaryKey(f)) {
			mFieldUniqueCache.put(f, true);
			return true;
		}
		Class<?> c = f.getDeclaringClass();
		if (!isPersistent(c))
			throw new IllegalArgumentException("Class '" + c.getName() + "' is transient.");
		XmlPullParser parser = loadXmlMapFile(c);
		try {
			int code = parser.getEventType();
			while (code != XmlPullParser.END_DOCUMENT) {
				if (code == XmlPullParser.START_TAG
						&& parser.getName().equalsIgnoreCase(mPropLoader.getPersistenceValue("ELEM_PROPERTY"))) {
					String name = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_NAME"));
					if (name == null)
						throw new InvalidMapFileException("'" + c.getName()
								+ "' map file does not specify property name.");
					if (name.equals(f.getName())) {
						String unique = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_UNIQUE"));
						if (unique == null) {
							mFieldUniqueCache.put(f, false);
							return false;
						} else {
							boolean isUnique = Boolean.parseBoolean(unique);
							mFieldUniqueCache.put(f, isUnique);
							return isUnique;
						}
					}
				} else if (code == XmlPullParser.START_TAG
						&& parser.getName().equalsIgnoreCase(mPropLoader.getPersistenceValue("ELEM_OTO"))) {
					String field = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_FIELD"));
					String owner = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_OWNER"));
					if (field.equals(f.getName()) && owner.equals(f.getDeclaringClass().getName())) {
						mFieldUniqueCache.put(f, true);
						return true;
					}
				}
				code = parser.next();
			}
		} catch (XmlPullParserException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		} catch (IOException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		}
		throw new InvalidMapFileException("'" + c.getName() + "' map file does not specify the property '"
				+ f.getName() + "'.");
	}

	@Override
	public Set<ManyToManyRelationship> getManyToManyRelationships(Class<?> c) {
		if (!isPersistent(c))
			throw new IllegalArgumentException("Class '" + c.getName() + "' is transient.");
		Set<ManyToManyRelationship> ret = new HashSet<ManyToManyRelationship>();
		for (ManyToManyRelationship r : mManyToManyCache.values()) {
			if (r.contains(c))
				ret.add(r);
		}
		if (ret.size() > 0)
			return ret;
		List<Field> fields = getPersistentFields(c);
		for (Field f : fields) {
			ManyToManyRelationship rel = getManyToManyRelationship(f);
			if (rel == null)
				continue;
			mManyToManyCache.put(f, rel);
			ret.add(rel);
		}
		return ret;
	}

	@Override
	public Cascade getCascadeMode(Class<?> c) {
		if (mCascadeCache.containsKey(c))
			return mCascadeCache.get(c);
		if (!isPersistent(c))
			throw new IllegalArgumentException("Class '" + c.getName() + "' is transient.");
		XmlPullParser parser = loadXmlMapFile(c);
		try {
			int code = parser.getEventType();
			while (code != XmlPullParser.END_DOCUMENT) {
				if (code == XmlPullParser.START_TAG
						&& parser.getName().equalsIgnoreCase(mPropLoader.getPersistenceValue("ELEM_CLASS"))) {
					String name = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_NAME"));
					if (name == null)
						throw new InvalidMapFileException("'" + c.getName() + "' map file does not specify class name.");
					String cascade = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_CASCADE"));
					if (cascade == null) {
						mCascadeCache.put(c, Cascade.All);
						return Cascade.All;
					} else {
						Cascade mode;
						if (cascade.equalsIgnoreCase("none"))
							mode = Cascade.None;
						else if (cascade.equalsIgnoreCase("keys"))
							mode = Cascade.Keys;
						else
							mode = Cascade.All;
						mCascadeCache.put(c, mode);
						return mode;
					}
				}
				code = parser.next();
			}
		} catch (XmlPullParserException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		} catch (IOException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		}
		throw new InvalidMapFileException("'" + c.getName() + "' map file does not specify class name.");
	}

	@Override
	public boolean isRelationship(Field f) {
		if (mRelationshipCheckCache.containsKey(f))
			return mRelationshipCheckCache.get(f);
		Class<?> c = f.getDeclaringClass();
		if (!isPersistent(c))
			throw new IllegalArgumentException("Class '" + c.getName() + "' is transient.");
		XmlPullParser parser = loadXmlMapFile(c);
		try {
			int code = parser.getEventType();
			while (code != XmlPullParser.END_DOCUMENT) {
				if (code == XmlPullParser.START_TAG
						&& (parser.getName().equalsIgnoreCase(mPropLoader.getPersistenceValue("ELEM_MTM"))
								|| parser.getName().equalsIgnoreCase(mPropLoader.getPersistenceValue("ELEM_OTM"))
								|| parser.getName().equalsIgnoreCase(mPropLoader.getPersistenceValue("ELEM_MTO")) || parser
								.getName().equalsIgnoreCase(mPropLoader.getPersistenceValue("ELEM_OTO")))) {
					String field = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_FIELD"));
					if (field == null)
						throw new InvalidMapFileException("'" + c.getName() + "' map file does not specify field name.");
					if (f.getName().equals(field)) {
						mRelationshipCheckCache.put(f, true);
						return true;
					}
				}
				code = parser.next();
			}
		} catch (XmlPullParserException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		} catch (IOException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		}
		mRelationshipCheckCache.put(f, false);
		return false;
	}

	@Override
	public boolean isManyToManyRelationship(Field f) {
		if (mManyToManyCache.containsKey(f))
			return true;
		ModelRelationship rel = getRelationship(f);
		if (rel == null)
			return false;
		return rel.getRelationType() == RelationType.ManyToMany;
	}
	
	@Override
	public boolean isOneToOneRelationship(Field f) {
		if (mOneToOneCache.containsKey(f))
			return true;
		ModelRelationship rel = getRelationship(f);
		if (rel == null)
			return false;
		return rel.getRelationType() == RelationType.OneToOne;
	}

	@Override
	public boolean isToOneRelationship(Field f) {
		if (mManyToOneCache.containsKey(f) || mOneToOneCache.containsKey(f))
			return true;
		ModelRelationship rel = getRelationship(f);
		if (rel == null)
			return false;
		return rel.getRelationType() == RelationType.ManyToOne || rel.getRelationType() == RelationType.OneToOne;
	}

	@Override
	public ModelRelationship getRelationship(Field f) {
		ModelRelationship ret = getManyToManyRelationship(f);
		if (ret != null)
			return ret;
		ret = getManyToOneRelationship(f);
		if (ret != null)
			return ret;
		ret = getOneToManyRelationship(f);
		if (ret != null)
			return ret;
		return getOneToOneRelationship(f);
	}

	@Override
	public Field findRelationshipField(Class<?> c, ModelRelationship rel) {
		for (Field f : getPersistentFields(c)) {
			f.setAccessible(true);
			if (!isRelationship(f))
				continue;
			switch (rel.getRelationType()) {
			case ManyToMany:
				if (rel.equals(mManyToManyCache.get(f)))
					return f;
				break;
			case ManyToOne:
				if (rel.equals(mManyToOneCache.get(f)))
					return f;
				break;
			case OneToMany:
				if (rel.equals(mOneToManyCache.get(f)))
					return f;
				break;
			case OneToOne:
				if (rel.equals(mOneToOneCache.get(f)))
					return f;
			}
		}
		return null;
	}

	@Override
	public boolean isLazy(Class<?> c) {
		if (mLazyLoadingCache.containsKey(c))
			return mLazyLoadingCache.get(c);
		if (!isPersistent(c))
			throw new IllegalArgumentException("Class '" + c.getName() + "' is transient.");
		XmlPullParser parser = loadXmlMapFile(c);
		try {
			int code = parser.getEventType();
			while (code != XmlPullParser.END_DOCUMENT) {
				if (code == XmlPullParser.START_TAG
						&& parser.getName().equalsIgnoreCase(mPropLoader.getPersistenceValue("ELEM_CLASS"))) {
					String name = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_NAME"));
					if (name == null)
						throw new InvalidMapFileException("'" + c.getName() + "' map file does not specify class name.");
					String lazy = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_LAZY"));
					if (lazy == null) {
						mLazyLoadingCache.put(c, true);
						return true;
					} else {
						boolean isLazy = Boolean.parseBoolean(lazy);
						mLazyLoadingCache.put(c, isLazy);
						return isLazy;
					}
				}
				code = parser.next();
			}
		} catch (XmlPullParserException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		} catch (IOException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		}
		throw new InvalidMapFileException("'" + c.getName() + "' map file does not specify class name.");
	}

	@Override
	public String getRestEndpoint(Class<?> c) throws IllegalArgumentException {
		if (mRestEndpointCache.containsKey(c))
			return mRestEndpointCache.get(c);
		if (!isPersistent(c))
			throw new IllegalArgumentException("Class '" + c.getName() + "' is transient.");
		XmlPullParser parser = loadXmlMapFile(c);
		try {
			int code = parser.getEventType();
			while (code != XmlPullParser.END_DOCUMENT) {
				if (code == XmlPullParser.START_TAG
						&& parser.getName().equalsIgnoreCase(mPropLoader.getPersistenceValue("ELEM_CLASS"))) {
					String name = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_NAME"));
					if (name == null)
						throw new InvalidMapFileException("'" + c.getName() + "' map file does not specify class name.");
					String res = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_REST"));
					if (res == null) {
						if (name.contains("."))
							res = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
						else
							res = name.toLowerCase();
					}
					mRestEndpointCache.put(c, res);
					return res;
				}
				code = parser.next();
			}
		} catch (XmlPullParserException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		} catch (IOException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		}
		throw new InvalidMapFileException("'" + c.getName() + "' map file does not specify class.");
	}

	@Override
	public String getEndpointFieldName(Field f) throws IllegalArgumentException {
		if (mRestFieldCache.containsKey(f))
			return mRestFieldCache.get(f);
		Class<?> c = f.getDeclaringClass();
		if (!isPersistent(c))
			throw new IllegalArgumentException("Class '" + c.getName() + "' is transient.");
		XmlPullParser parser = loadXmlMapFile(c);
		try {
			int code = parser.getEventType();
			while (code != XmlPullParser.END_DOCUMENT) {
				if (isFieldPrimaryKey(f)) {
					if (code == XmlPullParser.START_TAG
							&& parser.getName().equalsIgnoreCase(mPropLoader.getPersistenceValue("ELEM_PRIMARY_KEY"))) {
						String name = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_NAME"));
						if (name == null)
							throw new InvalidMapFileException("'" + c.getName()
									+ "' map file does not specify property name.");
						if (f.getName().equals(name)) {
							String rest = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_REST"));
							if (rest == null)
								rest = StringUtil.formatFieldName(name);
							mRestFieldCache.put(f, rest);
							return rest;
						}
					}
				}
				if (code == XmlPullParser.START_TAG
						&& parser.getName().equalsIgnoreCase(mPropLoader.getPersistenceValue("ELEM_PROPERTY"))) {
					String name = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_NAME"));
					if (name == null)
						throw new InvalidMapFileException("'" + c.getName()
								+ "' map file does not specify property name.");
					if (f.getName().equals(name)) {
						String rest = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_REST"));
						if (rest == null)
							rest = StringUtil.formatFieldName(name);
						mRestFieldCache.put(f, rest);
						return rest;
					}
				}
				code = parser.next();
			}
		} catch (XmlPullParserException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		} catch (IOException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		}
		throw new InvalidMapFileException("'" + c.getName() + "' map file does not specify property '" + f.getName()
				+ "'.");
	}

	private XmlPullParser loadXmlMapFile(Class<?> c) {
		Resources res = mContext.getAndroidContext().getResources();
		int id = 0;
		if (!mResourceCache.containsKey(c)) {
			String fileName = c.getSimpleName().toLowerCase();
			id = res.getIdentifier(fileName, "raw", mContext.getAndroidContext().getPackageName());
			mResourceCache.put(c, id);
		}
		try {
			XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
			parser.setInput(res.openRawResource(mResourceCache.get(c)), "UTF-8");
			return parser;
		} catch (NotFoundException e) {
			return null;
		} catch (XmlPullParserException e) {
			return null;
		}
	}

	private ManyToManyRelationship getManyToManyRelationship(Field f) {
		if (mManyToManyCache.containsKey(f))
			return mManyToManyCache.get(f);
		Class<?> c = f.getDeclaringClass();
		XmlPullParser parser = loadXmlMapFile(c);
		try {
			int code = parser.getEventType();
			while (code != XmlPullParser.END_DOCUMENT) {
				if (code == XmlPullParser.START_TAG
						&& parser.getName().equalsIgnoreCase(mPropLoader.getPersistenceValue("ELEM_MTM"))) {
					String field = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_FIELD"));
					if (field == null)
						throw new InvalidMapFileException("'" + c.getName()
								+ "' map file does not specify relation field.");
					if (f.getName().equals(field)) {
						ManyToManyRelationship ret = new ManyToManyRelationship();
						ret.setFirstType(c);
						String name = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_NAME"));
						if (name == null)
							throw new InvalidMapFileException("'" + c.getName()
									+ "' map file does not specify relation name.");
						ret.setName(name);
						String className = parser
								.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_CLASS"));
						if (className == null)
							throw new InvalidMapFileException("'" + c.getName()
									+ "' map file does not specify relation class.");
						ret.setSecondType(Class.forName(className));
						String foreignField = parser.getAttributeValue(null,
								mPropLoader.getPersistenceValue("ATTR_FOREIGN_FIELD"));
						if (foreignField == null)
							throw new InvalidMapFileException("'" + c.getName()
									+ "' map file does not specify relation foreign field.");
						ret.setSecondFieldName(foreignField);
						String keyField = parser.getAttributeValue(null,
								mPropLoader.getPersistenceValue("ATTR_KEY_FIELD"));
						if (keyField == null)
							throw new InvalidMapFileException("'" + c.getName()
									+ "' map file does not specify relation key field.");
						ret.setFirstFieldName(keyField);
						String table = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_TABLE"));
						if (table == null)
							throw new InvalidMapFileException("'" + c.getName()
									+ "' map file does not specify relation table.");
						ret.setTableName(table);
						mManyToManyCache.put(f, ret);
						return ret;
					}
				}
				code = parser.next();
			}
		} catch (XmlPullParserException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		} catch (IOException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		} catch (ClassNotFoundException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "' (could not resolve class)", e);
		}
		return null;
	}

	private ManyToOneRelationship getManyToOneRelationship(Field f) {
		if (mManyToOneCache.containsKey(f))
			return mManyToOneCache.get(f);
		Class<?> c = f.getDeclaringClass();
		XmlPullParser parser = loadXmlMapFile(c);
		try {
			int code = parser.getEventType();
			while (code != XmlPullParser.END_DOCUMENT) {
				if (code == XmlPullParser.START_TAG
						&& parser.getName().equalsIgnoreCase(mPropLoader.getPersistenceValue("ELEM_MTO"))) {
					String field = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_FIELD"));
					if (field == null)
						throw new InvalidMapFileException("'" + c.getName()
								+ "' map file does not specify relation field.");
					if (f.getName().equals(field)) {
						ManyToOneRelationship ret = new ManyToOneRelationship();
						ret.setFirstType(c);
						String name = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_NAME"));
						if (name == null)
							throw new InvalidMapFileException("'" + c.getName()
									+ "' map file does not specify relation name.");
						ret.setName(name);
						String className = parser
								.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_CLASS"));
						if (className == null)
							throw new InvalidMapFileException("'" + c.getName()
									+ "' map file does not specify relation class.");
						ret.setSecondType(Class.forName(className));
						String column = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_COLUMN"));
						if (column == null)
							throw new InvalidMapFileException("'" + c.getName()
									+ "' map file does not specify relation column.");
						ret.setColumn(column);
						mManyToOneCache.put(f, ret);
						return ret;
					}
				}
				code = parser.next();
			}
		} catch (XmlPullParserException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		} catch (IOException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		} catch (ClassNotFoundException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "' (could not resolve class)", e);
		}
		return null;
	}

	private OneToManyRelationship getOneToManyRelationship(Field f) {
		if (mOneToManyCache.containsKey(f))
			return mOneToManyCache.get(f);
		Class<?> c = f.getDeclaringClass();
		XmlPullParser parser = loadXmlMapFile(c);
		try {
			int code = parser.getEventType();
			while (code != XmlPullParser.END_DOCUMENT) {
				if (code == XmlPullParser.START_TAG
						&& parser.getName().equalsIgnoreCase(mPropLoader.getPersistenceValue("ELEM_OTM"))) {
					String field = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_FIELD"));
					if (field == null)
						throw new InvalidMapFileException("'" + c.getName()
								+ "' map file does not specify relation field.");
					if (f.getName().equals(field)) {
						OneToManyRelationship ret = new OneToManyRelationship();
						ret.setFirstType(c);
						ret.setOneType(c);
						String name = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_NAME"));
						if (name == null)
							throw new InvalidMapFileException("'" + c.getName()
									+ "' map file does not specify relation name.");
						ret.setName(name);
						String className = parser
								.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_CLASS"));
						if (className == null)
							throw new InvalidMapFileException("'" + c.getName()
									+ "' map file does not specify relation class.");
						Class<?> t = Class.forName(className);
						ret.setSecondType(t);
						ret.setManyType(t);
						String column = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_COLUMN"));
						if (column == null)
							throw new InvalidMapFileException("'" + c.getName()
									+ "' map file does not specify relation column.");
						ret.setColumn(column);
						mOneToManyCache.put(f, ret);
						return ret;
					}
				}
				code = parser.next();
			}
		} catch (XmlPullParserException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		} catch (IOException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		} catch (ClassNotFoundException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "' (could not resolve class)", e);
		}
		return null;
	}

	private OneToOneRelationship getOneToOneRelationship(Field f) {
		if (mOneToOneCache.containsKey(f))
			return mOneToOneCache.get(f);
		Class<?> c = f.getDeclaringClass();
		XmlPullParser parser = loadXmlMapFile(c);
		try {
			int code = parser.getEventType();
			while (code != XmlPullParser.END_DOCUMENT) {
				if (code == XmlPullParser.START_TAG
						&& parser.getName().equalsIgnoreCase(mPropLoader.getPersistenceValue("ELEM_OTO"))) {
					String field = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_FIELD"));
					if (field == null)
						throw new InvalidMapFileException("'" + c.getName()
								+ "' map file does not specify relation field.");
					if (f.getName().equals(field)) {
						OneToOneRelationship ret = new OneToOneRelationship();
						ret.setFirstType(c);
						String name = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_NAME"));
						if (name == null)
							throw new InvalidMapFileException("'" + c.getName()
									+ "' map file does not specify relation name.");
						ret.setName(name);
						String className = parser
								.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_CLASS"));
						if (className == null)
							throw new InvalidMapFileException("'" + c.getName()
									+ "' map file does not specify relation class.");
						ret.setSecondType(Class.forName(className));
						String column = parser.getAttributeValue(null, mPropLoader.getPersistenceValue("ATTR_COLUMN"));
						if (column == null)
							throw new InvalidMapFileException("'" + c.getName()
									+ "' map file does not specify relation column.");
						ret.setColumn(column);
						mOneToOneCache.put(f, ret);
						return ret;
					}
				}
				code = parser.next();
			}
		} catch (XmlPullParserException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		} catch (IOException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "'", e);
		} catch (ClassNotFoundException e) {
			mLogger.error("Unable to parse map file for '" + c.getName() + "' (could not resolve class)", e);
		}
		return null;
	}

}