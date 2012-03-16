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

import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.criteria.CriteriaQuery;
import com.clarionmedia.infinitum.orm.criteria.GenCriteria;
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;
import com.clarionmedia.infinitum.orm.relationship.ManyToManyRelationship;
import com.clarionmedia.infinitum.orm.relationship.OneToManyRelationship;
import com.clarionmedia.infinitum.orm.sqlite.SqliteDbHelper;

/**
 * <p>
 * {@code SqlBuilder} is used to dynamically construct SQL strings for table
 * generation and queries at runtime.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/13/12
 */
public interface SqlBuilder {

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
	 * @throws InfinitumConfigurationException
	 *             if domain classes have not been properly configured in
	 *             {@code infinitum.cfg.xml}
	 */
	int createTables(SqliteDbHelper dbHelper)
			throws ModelConfigurationException;

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
	String createModelTableString(Class<?> c)
			throws ModelConfigurationException;

	/**
	 * Generates a SQL query {@link String} from the given {@link GenCriteria}.
	 * 
	 * @param criteria
	 *            the {@code Criteria} to build the SQL query from
	 * @return SQL query
	 */
	String createQuery(CriteriaQuery criteria);

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
	String createManyToManyJoinQuery(ManyToManyRelationship rel,
			Serializable id, Class<?> direction)
			throws InfinitumRuntimeException;

	/**
	 * Generates a {@link StringBuilder} consisting of the initial segment of a
	 * SQL query for deleting stale many-to-many relationships.
	 * 
	 * <p>
	 * For example:
	 * <code>DELETE FROM foo_bar WHERE foo_id = 42 AND bar_id NOT IN (</code>.
	 * The <code>NOT IN</code> clause is intended to be populated with the IDs
	 * of entities which are currently related to the {@code Foo} entity with ID
	 * 42, typically using
	 * {@link SqlBuilder#addPrimaryKeyToQuery(Object, StringBuilder, String)} .
	 * Thus, the completed query will be used to clear relationships which no
	 * longer exist.
	 * </p>
	 * 
	 * @param rel
	 *            the {@link ManyToManyRelationship} for this relationship query
	 * @param model
	 *            the model containing the relationship
	 * @return {@code StringBuilder} containing the initial query segment
	 */
	StringBuilder createInitialStaleRelationshipQuery(
			ManyToManyRelationship rel, Object model);

	/**
	 * Generates a {@link StringBuilder} consisting of the initial segment of a
	 * SQL query for updating the foreign keys in a one-to-many relationship.
	 * 
	 * @param rel
	 *            the {@link OneToManyRelationship} for this relationship query
	 * @param model
	 *            the model containing the relationship
	 * @return {@code StringBuilder} containing the initial query segment
	 */
	StringBuilder createInitialUpdateForeignKeyQuery(OneToManyRelationship rel,
			Object model);

	/**
	 * Adds the given {@code Object's} primary key to the specified query. This
	 * method is the counterpart to
	 * {@link SqlBuilder#createInitialStaleRelationshipQuery(ManyToManyRelationship, Object)}
	 * and
	 * {@link SqlBuilder#createInitialUpdateForeignKeyQuery(OneToManyRelationship, Object)}
	 * as it is used to populate the {@code IN} or {@code NOT IN} clause of the
	 * query.
	 * 
	 * @param obj
	 *            the {@code Object} whose primary key will be added to the
	 *            query
	 * @param sb
	 *            the {@code StringBuilder} containing the query
	 * @param prefix
	 *            key prefix, usually "" or ", "
	 */
	void addPrimaryKeyToQuery(Object obj, StringBuilder sb, String prefix);

	/**
	 * Generates a SQL query {@link String} for deleting relationships from a
	 * many-to-many table.
	 * 
	 * @param obj
	 *            owner of the relationship to be deleted
	 * @param rel
	 *            the relationship type
	 * @return SQL query
	 */
	String createManyToManyDeleteQuery(Object obj, ManyToManyRelationship rel);

	String createUpdateQuery(Object model, Object related, String column);

}
