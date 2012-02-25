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

package com.clarionmedia.infinitum.orm.sql;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;

import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.ManyToManyRelationship;
import com.clarionmedia.infinitum.orm.OrmConstants;
import com.clarionmedia.infinitum.orm.annotation.ManyToMany;
import com.clarionmedia.infinitum.orm.criteria.CriteriaQuery;
import com.clarionmedia.infinitum.orm.criteria.GenCriteria;
import com.clarionmedia.infinitum.orm.criteria.criterion.Criterion;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.persistence.PersistenceResolution;
import com.clarionmedia.infinitum.orm.persistence.TypeResolution;
import com.clarionmedia.infinitum.orm.sqlite.SqliteDbHelper;
import com.clarionmedia.infinitum.reflection.PackageReflector;

/**
 * <p>
 * {@code SqlBuilder} is used to dynamically construct SQL strings for table
 * generation and queries.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/13/12
 */
public class SqlBuilder {

	// TODO: this class currently doesn't handle reserved keywords.
	// See: http://www.sqlite.org/lang_keywords.html

	private static final String CREATE_TABLE = "CREATE TABLE";
	private static final String NOT_NULL = "NOT NULL";
	private static final String PRIMARY_KEY = "PRIMARY KEY";
	private static final String AUTO_INCREMENT = "AUTOINCREMENT";
	private static final String UNIQUE = "UNIQUE";

	/**
	 * Creates the model tables for the application in the SQLite database as
	 * configured in <code>infinitum.cfg.xml</code> and returns the numbers of
	 * tables created.
	 * 
	 * @param dbHelper
	 *            the <code>SqliteDbHelper</code> encapsulating the
	 *            <code>ApplicationContext</code> for this application
	 * @return number of tables created
	 * @throws ModelConfigurationException
	 *             if table(s) cannot be created due to a misconfigured model.
	 *             For example, a model that does not contain any persistent
	 *             <code>Fields</code>
	 */
	public static int createTables(SqliteDbHelper dbHelper) throws ModelConfigurationException {
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

	/**
	 * Generates a SQL query {@link String} from the given {@link GenCriteria}.
	 * 
	 * @param criteria
	 *            the {@code Criteria} to build the SQL query from
	 * @return SQL query
	 */
	public static String createQuery(CriteriaQuery criteria) {
		Class<?> c = criteria.getEntityClass();
		StringBuilder query = new StringBuilder(SqlConstants.SELECT_ALL_FROM)
				.append(PersistenceResolution.getModelTableName(c)).append(' ').append(SqlConstants.WHERE).append(' ');
		String prefix = "";
		for (Criterion criterion : criteria.getCriterion()) {
			query.append(prefix);
			prefix = SqlConstants.AND;
			query.append(criterion.toSql(criteria));
		}
		if (criteria.getLimit() > 0)
			query.append(' ').append(SqlConstants.LIMIT).append(' ').append(criteria.getLimit());
		if (criteria.getOffset() > 0)
			query.append(' ').append(SqlConstants.OFFSET).append(' ').append(criteria.getOffset());
		return query.toString();
	}

	/**
	 * Generates a SQL query {@link String} from the given
	 * {@link ManyToManyRelationship} which retrieves rows of the given
	 * direction type which are associated with the given ID.
	 * 
	 * <p>
	 * For example, assume you have the models {@code Foo} and {@code Bar} which
	 * have a many-to-many association and are mapped to the tables {@code foo}
	 * and {@code bar} respectively with the relationships being stored in
	 * {@code foobar}. Calling {@code createManyToManyJoinQuery(rel, 42,
	 * Bar.class)} would generate a query that would retrieve all records of
	 * {@code Bar} associated with the instance of {@code Foo} which has an ID
	 * of 42.
	 * </p>
	 * 
	 * @param rel
	 *            the {@link ManyToManyRelationship} containing the association
	 *            being queried
	 * @param id
	 *            the ID in which the associated records are linked with
	 * @param direction
	 *            the direction the relationship is being queried in, returning
	 *            records of this {@link Class}
	 * @return SQL query
	 * @throws InfinitumRuntimeException
	 *             if the direction {@code Class} is not a part of the given
	 *             {@code ManyToManyRelationship}
	 */
	public static String createManyToManyJoinQuery(ManyToManyRelationship rel, Serializable id, Class<?> direction)
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

	/**
	 * Generates the create table SQL statement for the specified
	 * <code>Class</code>. If the <code>Class</code> does not contain any
	 * persistent <code>Fields</code>, a {@link ModelConfigurationException}
	 * will be thrown. If the <code>Class</code> itself is marked as transient,
	 * this method will return null.
	 * 
	 * @param c
	 *            the <code>Class</code> to generate the create table SQL
	 *            statement for
	 * @return create table SQL statement
	 * @throws ModelConfigurationException
	 */
	private static String createModelTableString(Class<?> c) throws ModelConfigurationException {
		if (!PersistenceResolution.isPersistent(c))
			return null;
		StringBuilder sb = new StringBuilder(CREATE_TABLE).append(' ')
				.append(PersistenceResolution.getModelTableName(c)).append(" (");
		appendColumns(c, sb);
		appendUniqueColumns(c, sb);
		sb.append(')');
		return sb.toString();
	}

	private static String createManyToManyTableString(ManyToManyRelationship rel) throws ModelConfigurationException {
		if (!PersistenceResolution.isPersistent(rel.getFirstType())
				|| !PersistenceResolution.isPersistent(rel.getSecondType()))
			return null;
		StringBuilder sb = new StringBuilder(CREATE_TABLE).append(' ').append(rel.getTableName()).append(" (");
		Field first = rel.getFirstField();
		if (first == null)
			throw new ModelConfigurationException(String.format(OrmConstants.MM_RELATIONSHIP_ERROR, rel.getFirstType()
					.getName(), rel.getSecondType().getName()));
		Field second = rel.getSecondField();
		if (second == null)
			throw new ModelConfigurationException(String.format(OrmConstants.MM_RELATIONSHIP_ERROR, rel.getFirstType()
					.getName(), rel.getSecondType().getName()));
		sb.append(PersistenceResolution.getModelTableName(rel.getFirstType())).append('_')
				.append(PersistenceResolution.getFieldColumnName(first)).append(' ')
				.append(TypeResolution.getSqliteDataType(first).toString()).append(' ').append(NOT_NULL).append(", ")
				.append(PersistenceResolution.getModelTableName(rel.getSecondType())).append('_')
				.append(PersistenceResolution.getFieldColumnName(second)).append(' ')
				.append(TypeResolution.getSqliteDataType(second).toString()).append(' ').append(NOT_NULL).append(')');
		return sb.toString();
	}

	private static void appendColumns(Class<?> c, StringBuilder sb) throws ModelConfigurationException {
		List<Field> fields = PersistenceResolution.getPersistentFields(c);

		// Throw a runtime exception if there are no persistent fields
		if (fields.size() == 0)
			throw new ModelConfigurationException(String.format(OrmConstants.NO_PERSISTENT_FIELDS, c.getName()));

		String prefix = "";
		for (Field f : fields) {
			if (f.isAnnotationPresent(ManyToMany.class))
				continue;
			sb.append(prefix);
			prefix = ", ";

			// Append column name and data type, e.g. "foo INTEGER"
			sb.append(PersistenceResolution.getFieldColumnName(f)).append(' ')
					.append(TypeResolution.getSqliteDataType(f).toString());

			// Check if the column is a PRIMARY KEY
			if (PersistenceResolution.isFieldPrimaryKey(f)) {
				sb.append(" ").append(PRIMARY_KEY);
				if (PersistenceResolution.isPrimaryKeyAutoIncrement(f))
					sb.append(" ").append(AUTO_INCREMENT);
			}

			// Check if the column is NOT NULL
			if (!PersistenceResolution.isFieldNullable(f))
				sb.append(" ").append(NOT_NULL);
		}
	}

	private static void appendUniqueColumns(Class<?> c, StringBuilder sb) {
		List<Field> fields = PersistenceResolution.getUniqueFields(c);

		// Append any unique constraints, e.g. UNIQUE(foo, bar)
		if (fields.size() > 0) {
			sb.append(", ").append(UNIQUE).append('(');
			String prefix = "";
			for (Field f : fields) {
				sb.append(prefix);
				prefix = ", ";
				sb.append(PersistenceResolution.getFieldColumnName(f));
			}
			sb.append(')');
		}
	}
}
