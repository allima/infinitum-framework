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

package com.clarionmedia.infinitum.orm.sqlite;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;

import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.ManyToManyRelationship;
import com.clarionmedia.infinitum.orm.OneToManyRelationship;
import com.clarionmedia.infinitum.orm.OrmConstants;
import com.clarionmedia.infinitum.orm.annotation.ManyToMany;
import com.clarionmedia.infinitum.orm.criteria.CriteriaQuery;
import com.clarionmedia.infinitum.orm.criteria.criterion.Criterion;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.PersistenceResolution;
import com.clarionmedia.infinitum.orm.persistence.TypeResolution;
import com.clarionmedia.infinitum.orm.persistence.TypeResolution.SqliteDataType;
import com.clarionmedia.infinitum.orm.sql.SqlBuilder;
import com.clarionmedia.infinitum.orm.sql.SqlConstants;
import com.clarionmedia.infinitum.reflection.PackageReflector;

/**
 * <p>
 * Implementation of {@link SqlBuilder} for interacting with a local SQLite
 * database.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/03/12
 */
public class SqliteBuilder implements SqlBuilder {

	// TODO: this class currently doesn't handle reserved keywords.
	// See: http://www.sqlite.org/lang_keywords.html

	@Override
	public int createTables(SqliteDbHelper dbHelper) throws ModelConfigurationException {
		int count = 0;
		SQLiteDatabase db = dbHelper.getDatabase();
		for (String m : dbHelper.getApplicationContext().getDomainModels()) {
			Class<?> c = PackageReflector.getClass(m);
			String sql = createModelTableString(c);
			if (sql != null) {
				db.execSQL(sql);
				count++;
			}
			PersistenceResolution.getManyToManyRelationships(c);
		}
		for (ManyToManyRelationship r : PersistenceResolution.getManyToManyCache()) {
			String sql = createManyToManyTableString(r);
			if (sql != null) {
				db.execSQL(sql);
				count++;
			}
		}
		return count;
	}

	@Override
	public String createModelTableString(Class<?> c) throws ModelConfigurationException {
		if (!PersistenceResolution.isPersistent(c))
			return null;
		StringBuilder sb = new StringBuilder(SqlConstants.CREATE_TABLE).append(' ')
				.append(PersistenceResolution.getModelTableName(c)).append(" (");
		appendColumns(c, sb);
		appendUniqueColumns(c, sb);
		sb.append(')');
		return sb.toString();
	}

	@Override
	public String createQuery(CriteriaQuery criteria) {
		Class<?> c = criteria.getEntityClass();
		StringBuilder query = new StringBuilder(SqlConstants.SELECT_ALL_FROM).append(PersistenceResolution
				.getModelTableName(c));
		String prefix = " WHERE ";
		for (Criterion criterion : criteria.getCriterion()) {
			query.append(prefix);
			prefix = ' ' + SqlConstants.AND + ' ';
			query.append(criterion.toSql(criteria));
		}
		if (criteria.getLimit() > 0)
			query.append(' ').append(SqlConstants.LIMIT).append(' ').append(criteria.getLimit());
		if (criteria.getOffset() > 0)
			query.append(' ').append(SqlConstants.OFFSET).append(' ').append(criteria.getOffset());
		return query.toString();
	}

	@Override
	public String createManyToManyJoinQuery(ManyToManyRelationship rel, Serializable id, Class<?> direction)
			throws InfinitumRuntimeException {
		if (!rel.contains(direction))
			throw new InfinitumRuntimeException(String.format(
					"'%s' is not a valid direction for relationship '%s'<=>'%s'.", direction.getName(), rel
							.getFirstType().getName(), rel.getSecondType().getName()));
		StringBuilder query = new StringBuilder(String.format(SqlConstants.ALIASED_SELECT_ALL_FROM, 'x')).append(
				PersistenceResolution.getModelTableName(rel.getFirstType())).append(' ');
		if (direction == rel.getFirstType())
			query.append("x, ");
		else
			query.append("y, ");
		query.append(PersistenceResolution.getModelTableName(rel.getSecondType())).append(' ');
		if (direction == rel.getSecondType())
			query.append("x, ");
		else
			query.append("y, ");
		query.append(rel.getTableName()).append(" z ").append(SqlConstants.WHERE).append(' ').append("z.");
		if (direction == rel.getFirstType())
			query.append(PersistenceResolution.getModelTableName(rel.getFirstType())).append('_')
					.append(PersistenceResolution.getFieldColumnName(rel.getFirstField())).append(" = ").append("x.")
					.append(PersistenceResolution.getFieldColumnName(rel.getFirstField())).append(' ')
					.append(SqlConstants.AND).append(" z.")
					.append(PersistenceResolution.getModelTableName(rel.getSecondType())).append('_')
					.append(PersistenceResolution.getFieldColumnName(rel.getSecondField())).append(" = ").append("y.")
					.append(PersistenceResolution.getFieldColumnName(rel.getSecondField())).append(' ')
					.append(SqlConstants.AND).append(" y.")
					.append(PersistenceResolution.getFieldColumnName(rel.getSecondField())).append(" = ").append(id);
		else
			query.append(PersistenceResolution.getModelTableName(rel.getSecondType())).append('_')
					.append(PersistenceResolution.getFieldColumnName(rel.getSecondField())).append(" = ").append("x.")
					.append(PersistenceResolution.getFieldColumnName(rel.getSecondField())).append(' ')
					.append(SqlConstants.AND).append(" z.")
					.append(PersistenceResolution.getModelTableName(rel.getFirstType())).append('_')
					.append(PersistenceResolution.getFieldColumnName(rel.getFirstField())).append(" = ").append("y.")
					.append(PersistenceResolution.getFieldColumnName(rel.getFirstField())).append(' ')
					.append(SqlConstants.AND).append(" y.")
					.append(PersistenceResolution.getFieldColumnName(rel.getFirstField())).append(" = ").append(id);
		return query.toString();
	}

	@Override
	public StringBuilder createInitialStaleRelationshipQuery(ManyToManyRelationship rel, Object model) {
		Field pkField = PersistenceResolution.getPrimaryKeyField(model.getClass());
		pkField.setAccessible(true);
		Object pk = PersistenceResolution.getPrimaryKey(model);
		StringBuilder ret = new StringBuilder(SqlConstants.DELETE_FROM).append(rel.getTableName()).append(' ')
				.append(SqlConstants.WHERE).append(' ');
		Field col;
		if (model.getClass() == rel.getFirstType()) {
			ret.append(
					PersistenceResolution.getModelTableName(rel.getFirstType()) + '_'
							+ PersistenceResolution.getFieldColumnName(rel.getFirstField())).append(" = ");
			col = rel.getFirstField();
		} else {
			ret.append(
					PersistenceResolution.getModelTableName(rel.getSecondType()) + '_'
							+ PersistenceResolution.getFieldColumnName(rel.getSecondField())).append(" = ");
			col = rel.getSecondField();
		}
		switch (TypeResolution.getSqliteDataType(col)) {
		case TEXT:
			ret.append("'").append(ret.append(pk)).append("'");
			break;
		default:
			ret.append(pk);
		}
		ret.append(' ').append(SqlConstants.AND).append(' ');
		if (model.getClass() == rel.getFirstType())
			ret.append(PersistenceResolution.getModelTableName(rel.getSecondType()) + '_'
					+ PersistenceResolution.getFieldColumnName(rel.getSecondField()));
		else
			ret.append(PersistenceResolution.getModelTableName(rel.getFirstType()) + '_'
					+ PersistenceResolution.getFieldColumnName(rel.getFirstField()));
		return ret.append(' ').append(SqlConstants.NOT_IN).append(" (");
	}
	
	@Override
	public StringBuilder createInitialUpdateForeignKeyQuery(
			OneToManyRelationship rel, Object model) {
		StringBuilder ret = new StringBuilder(SqlConstants.UPDATE).append(' ')
					.append(PersistenceResolution.getModelTableName(rel.getManyType()))
					.append(' ').append(SqlConstants.SET).append(' ').append(rel.getColumn())
					.append(" = ");
		Field pkField = PersistenceResolution.getPrimaryKeyField(model.getClass());
		pkField.setAccessible(true);
		Object pk = PersistenceResolution.getPrimaryKey(model);
		switch (TypeResolution.getSqliteDataType(pkField)) {
		case TEXT:
			ret.append("'").append(ret.append(pk)).append("'");
			break;
		default:
			ret.append(pk);
		}
		ret.append(' ').append(SqlConstants.WHERE).append(' ');
		pkField = PersistenceResolution.getPrimaryKeyField(rel.getManyType());
		return ret.append(PersistenceResolution.getFieldColumnName(pkField))
		.append(' ').append(SqlConstants.IN).append(" (");
	}

	@Override
	public void addPrimaryKeyToQuery(Object o, StringBuilder sb, String prefix) {
		sb.append(prefix);
		Field pkField = PersistenceResolution.getPrimaryKeyField(o.getClass());
		Object pk = PersistenceResolution.getPrimaryKey(o);
		switch (TypeResolution.getSqliteDataType(pkField)) {
		case TEXT:
			sb.append("'").append(pk).append("'");
			break;
		default:
			sb.append(pk);
		}
	}

	@Override
	public String createManyToManyDeleteQuery(Object obj, ManyToManyRelationship rel) {
		StringBuilder query = new StringBuilder(String.format(SqlConstants.DELETE_FROM_WHERE, rel.getTableName()));
		if (obj.getClass() == rel.getFirstType())
			query.append(PersistenceResolution.getModelTableName(rel.getFirstType()) + '_'
					+ PersistenceResolution.getFieldColumnName(rel.getFirstField()));
		else
			query.append(PersistenceResolution.getModelTableName(rel.getSecondType()) + '_'
					+ PersistenceResolution.getFieldColumnName(rel.getSecondField()));
		query.append(" = ");
		Field pkField = PersistenceResolution.getPrimaryKeyField(obj.getClass());
		pkField.setAccessible(true);
		Object pk = null;
		try {
			pk = pkField.get(obj);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		switch (TypeResolution.getSqliteDataType(pkField)) {
		case TEXT:
			query.append("'").append(pk).append("'");
			break;
		default:
			query.append(pk);
		}
		return query.toString();
	}

	private String createManyToManyTableString(ManyToManyRelationship rel) throws ModelConfigurationException {
		if (!PersistenceResolution.isPersistent(rel.getFirstType())
				|| !PersistenceResolution.isPersistent(rel.getSecondType()))
			return null;
		StringBuilder sb = new StringBuilder(SqlConstants.CREATE_TABLE).append(' ').append(rel.getTableName())
				.append(" (");
		Field first = rel.getFirstField();
		if (first == null)
			throw new ModelConfigurationException(String.format(OrmConstants.MM_RELATIONSHIP_ERROR, rel.getFirstType()
					.getName(), rel.getSecondType().getName()));
		Field second = rel.getSecondField();
		if (second == null)
			throw new ModelConfigurationException(String.format(OrmConstants.MM_RELATIONSHIP_ERROR, rel.getFirstType()
					.getName(), rel.getSecondType().getName()));
		String firstCol = PersistenceResolution.getModelTableName(rel.getFirstType()) + '_'
				+ PersistenceResolution.getFieldColumnName(first);
		String secondCol = PersistenceResolution.getModelTableName(rel.getSecondType()) + '_'
				+ PersistenceResolution.getFieldColumnName(second);
		sb.append(firstCol).append(' ').append(TypeResolution.getSqliteDataType(first).toString()).append(' ')
				.append(", ").append(secondCol).append(' ').append(TypeResolution.getSqliteDataType(second).toString())
				.append(", ").append(SqlConstants.PRIMARY_KEY).append('(').append(firstCol).append(", ")
				.append(secondCol).append("))");
		return sb.toString();
	}

	private void appendColumns(Class<?> c, StringBuilder sb) throws ModelConfigurationException {
		List<Field> fields = PersistenceResolution.getPersistentFields(c);

		// Throw a runtime exception if there are no persistent fields
		if (fields.size() == 0)
			throw new ModelConfigurationException(String.format(OrmConstants.NO_PERSISTENT_FIELDS, c.getName()));

		String prefix = "";
		for (Field f : fields) {
			if (f.isAnnotationPresent(ManyToMany.class))
				continue;
			SqliteDataType type = TypeResolution.getSqliteDataType(f);
			if (type == null)
				continue;
			sb.append(prefix);
			prefix = ", ";

			// Append column name and data type, e.g. "foo INTEGER"
			sb.append(PersistenceResolution.getFieldColumnName(f)).append(' ').append(type.toString());

			// Check if the column is a PRIMARY KEY
			if (PersistenceResolution.isFieldPrimaryKey(f)) {
				sb.append(" ").append(SqlConstants.PRIMARY_KEY);
				if (PersistenceResolution.isPrimaryKeyAutoIncrement(f))
					sb.append(" ").append(SqlConstants.AUTO_INCREMENT);
			}

			// Check if the column is NOT NULL
			if (!PersistenceResolution.isFieldNullable(f))
				sb.append(" ").append(SqlConstants.NOT_NULL);
		}
	}

	private void appendUniqueColumns(Class<?> c, StringBuilder sb) {
		List<Field> fields = PersistenceResolution.getUniqueFields(c);

		// Append any unique constraints, e.g. UNIQUE(foo, bar)
		if (fields.size() > 0) {
			sb.append(", ").append(SqlConstants.UNIQUE).append('(');
			String prefix = "";
			for (Field f : fields) {
				sb.append(prefix);
				prefix = ", ";
				sb.append(PersistenceResolution.getFieldColumnName(f));
			}
			sb.append(')');
		}
	}

	@Override
	public String createUpdateQuery(Object model, Object related, String column) {
		Object pk = PersistenceResolution.getPrimaryKey(related);
		 StringBuilder update = new StringBuilder("UPDATE ").append(PersistenceResolution.getModelTableName(model.getClass()));
		    update.append(" SET ").append(column).append(" = ");
		    switch (TypeResolution.getSqliteDataType(PersistenceResolution.getPrimaryKeyField(related.getClass()))) {
		    case TEXT:
		    	update.append("'").append(pk).append("'");
		    	break;
		    default:
		    	update.append(pk);
		    }
		    update.append(" WHERE ");
		    update.append(PersistenceResolution.getFieldColumnName(PersistenceResolution.getPrimaryKeyField(model.getClass())));
		    update.append(" = ");
		    pk = PersistenceResolution.getPrimaryKey(model);
			switch (TypeResolution.getSqliteDataType(PersistenceResolution.getPrimaryKeyField(model.getClass()))) {
			case TEXT:
				update.append("'").append(pk).append("'");
				break;
			default:
				update.append(pk);
			}
			return update.toString();
	}
}
