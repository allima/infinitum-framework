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

package com.clarionmedia.infinitum.orm.sqlite.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.internal.Pair;
import com.clarionmedia.infinitum.logging.Logger;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.relationship.OneToOneRelationship;
import com.clarionmedia.infinitum.orm.sqlite.SqliteHelperFactory;
import com.clarionmedia.infinitum.orm.sqlite.SqliteModelFactory;
import com.clarionmedia.infinitum.orm.sqlite.SqliteUtil;
import com.clarionmedia.infinitum.reflection.ClassReflector;
import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class SqliteTemplateTest {
	
	private static final int FOO_MODEL_HASH = 42;
	private static final int BAR_MODEL_HASH = 38;
	private static final int BAZ_MODEL_HASH = 176;
	private static final long FOO_MODEL_ID = 120;
	private static final long BAR_MODEL_ID = 183;
	private static final long BAZ_MODEL_ID = 211;
	private static final String FOO_MODEL_TABLE = "foo";
	private static final String BAR_MODEL_TABLE = "bar";
	private static final String WHERE_CLAUSE = "where clause";
	
	@Mock
	private InfinitumContext mockInfinitumContext;
	
	@Mock
	private SqliteSession mockSqliteSession;
	
	@Mock
	private PersistencePolicy mockPersistencePolicy;
	
	@Mock
	private Logger mockLogger;
	
	@Mock
	private Context mockContext;
	
	@Mock
	private Map<Integer, Object> mockSessionCache;
	
	@Mock
	private Criteria<FooModel> mockFooCriteria;
	
	@Mock
	private Cursor mockCursor;
	
	@Mock
	private SqliteMapper mockSqliteMapper;
	
	@Mock
	private SQLiteDatabase mockSqliteDatabase;

	@Mock
	private SqliteDbHelper mockDbHelper;
	
	@Mock
	private SQLiteDatabase mockSqliteDb;
	
	@Mock
	private Stack<Boolean> mockTransactionStack;
	
	@Mock
	private SqliteHelperFactory mockHelperFactory;
	
	@Mock
	private SqliteBuilder mockSqliteBuilder;
	
	@Mock
	private SqliteCriteria<FooModel> mockCriteria;
	
	@Mock
	private SqliteModelMap mockFooModelMap;
	
	@Mock
	private SqliteModelMap mockBarModelMap;
	
	@Mock
	private ContentValues mockContentValues;
	
	@Mock
	private ClassReflector mockClassReflector;
	
	@Mock
	private SqliteUtil mockSqliteUtil;
	
	@Mock
	private SqliteModelFactory mockSqliteModelFactory;
	
	private Field mockFooPkField;
	private Field mockBarPkField;
	private FooModel foo;
	private BarModel bar;
	
	@SuppressWarnings("deprecation")
	@InjectMocks
	private SqliteTemplate sqliteTemplate = new SqliteTemplate();
	
	@Before
	public void setup() throws SecurityException, NoSuchFieldException {
		MockitoAnnotations.initMocks(this);
		foo = new FooModel();
		bar = new BarModel();
		mockFooPkField = FooModel.class.getField("id");
		mockBarPkField = BarModel.class.getField("id");
		when(mockInfinitumContext.getPersistencePolicy()).thenReturn(mockPersistencePolicy);
		when(mockHelperFactory.createSqliteDbHelper(mockInfinitumContext, mockSqliteMapper)).thenReturn(mockDbHelper);
		when(mockHelperFactory.createSqliteModelFactory(mockSqliteSession, mockSqliteMapper)).thenReturn(mockSqliteModelFactory);
		when(mockDbHelper.getWritableDatabase()).thenReturn(mockSqliteDb);
		when(mockFooModelMap.getContentValues()).thenReturn(mockContentValues);
		when(mockBarModelMap.getContentValues()).thenReturn(mockContentValues);
		when(mockContentValues.size()).thenReturn(3);
		when(mockPersistencePolicy.getModelTableName(FooModel.class)).thenReturn(FOO_MODEL_TABLE);
		when(mockPersistencePolicy.getModelTableName(BarModel.class)).thenReturn(BAR_MODEL_TABLE);
		when(mockPersistencePolicy.getPrimaryKeyField(FooModel.class)).thenReturn(mockFooPkField);
		when(mockPersistencePolicy.getPrimaryKeyField(BarModel.class)).thenReturn(mockBarPkField);
		when(mockSqliteMapper.mapModel(foo)).thenReturn(mockFooModelMap);
		when(mockSqliteMapper.mapModel(bar)).thenReturn(mockBarModelMap);
		when(mockPersistencePolicy.computeModelHash(foo)).thenReturn(FOO_MODEL_HASH);
		when(mockPersistencePolicy.computeModelHash(bar)).thenReturn(BAR_MODEL_HASH);
		when(mockPersistencePolicy.getPrimaryKey(foo)).thenReturn(FOO_MODEL_ID);
		when(mockPersistencePolicy.getFieldColumnName(mockFooPkField)).thenReturn("id");
		sqliteTemplate.open();
		sqliteTemplate.setAutocommit(true);
	}
	
	@After
	public void tearDown() {
		sqliteTemplate.close();
	}

	@Test
	public void testOpen() {
		// Run
		sqliteTemplate.open();
		
		// Verify
		verify(mockHelperFactory, times(2)).createSqliteDbHelper(mockInfinitumContext, mockSqliteMapper);
		verify(mockDbHelper, times(2)).getWritableDatabase();
	}
	
	@Test
	public void testCreateCriteria() {
		// Setup
		when(mockHelperFactory.createCriteria(mockSqliteSession, FooModel.class, mockSqliteBuilder, mockSqliteMapper))
			.thenReturn(mockCriteria);
		
		// Run
		Criteria<FooModel> actualCriteria = sqliteTemplate.createCriteria(FooModel.class);
		
		// Verify
		verify(mockHelperFactory).createCriteria(mockSqliteSession, FooModel.class, mockSqliteBuilder, mockSqliteMapper);
		assertEquals("Criteria returned from createCriteria should be equal to the expected Criteria", mockCriteria, actualCriteria);
	}
	
	@Test
	public void testClose() {
		/// Run
		sqliteTemplate.close();
		
		// Verify
		verify(mockDbHelper).close();
	}
	
	@Test
	public void testBeginTransaction_autocommitEnabled() {
	    // Setup
		sqliteTemplate.setAutocommit(true);
		
		// Run
		sqliteTemplate.beginTransaction();
		
		// Verify
		verify(mockSqliteDb, times(0)).beginTransaction();
		verify(mockTransactionStack, times(0)).push(true);
	}
	
	@Test
	public void testBeginTransaction_autocommitDisabled() {
		// Setup
		sqliteTemplate.setAutocommit(false);
				
		// Run
		sqliteTemplate.beginTransaction();
		
		// Verify
		verify(mockSqliteDb).beginTransaction();
		verify(mockTransactionStack).push(true);
	}
	
	@Test
	public void testCommit_transactionClosed() {
		// Setup
		when(mockTransactionStack.size()).thenReturn(0);
		
		// Run
		sqliteTemplate.commit();
		
		// Verify
		verify(mockSqliteDb, times(0)).setTransactionSuccessful();
		verify(mockSqliteDb, times(0)).endTransaction();
		verify(mockTransactionStack, times(0)).pop();
	}
	
	@Test
	public void testCommit_transactionOpen() {
		// Setup
		when(mockTransactionStack.size()).thenReturn(1);
		
		// Run
		sqliteTemplate.commit();
		
		// Verify
		verify(mockSqliteDb).setTransactionSuccessful();
		verify(mockSqliteDb).endTransaction();
		verify(mockTransactionStack).pop();
	}
	
	@Test
	public void testIsTransactionOpen() {
		// Setup
		when(mockTransactionStack.size()).thenReturn(1);
		
		// Run
		boolean actual = sqliteTemplate.isTransactionOpen();
		
		// Verify
		verify(mockTransactionStack).size();
		assertEquals("A transaction should be open", true, actual);
	}
	
	@Test
	public void testRollback_transactionClosed() {
		// Setup
		when(mockTransactionStack.size()).thenReturn(0);
		
		// Run
		sqliteTemplate.rollback();
		
		// Verify
		verify(mockSqliteDb, times(0)).endTransaction();
		verify(mockTransactionStack, times(0)).pop();
	}
	
	@Test
	public void testRollback_transactionOpen() {
		// Setup
		when(mockTransactionStack.size()).thenReturn(1);
		
		// Run
		sqliteTemplate.rollback();
		
		// Verify
		verify(mockSqliteDb).endTransaction();
		verify(mockTransactionStack).pop();
	}
	
	@Test(expected = InfinitumRuntimeException.class)
	public void testSave_transientModelThrowsException() {
		// Setup
		when(mockPersistencePolicy.isPersistent(FooModel.class)).thenReturn(false);
		
		// Run
		sqliteTemplate.save(foo);
		
		// Verify
		assertTrue("Saving a transient model should have thrown an exception", false);
	}
	
	@Test(expected = InfinitumRuntimeException.class)
	public void testSave_autocommitDisabled_noTransactionThrowsException() {
		// Setup
		sqliteTemplate.setAutocommit(false);
		when(mockPersistencePolicy.isPersistent(FooModel.class)).thenReturn(false);
		when(mockTransactionStack.size()).thenReturn(0);
		
		// Run
		sqliteTemplate.save(foo);
		
		// Verify
		assertTrue("Saving with autocommit disabled and no transaction should have thrown an exception", false);
	}
	
	@Test
	public void testSave_noRelationships_success() {
		// Setup
		when(mockPersistencePolicy.isPersistent(FooModel.class)).thenReturn(true);
		when(mockFooModelMap.getOneToOneRelationships()).thenReturn(new ArrayList<Pair<OneToOneRelationship, Object>>());
		when(mockSqliteDb.insert(FOO_MODEL_TABLE, null, mockContentValues)).thenReturn(FOO_MODEL_ID);
		when(mockPersistencePolicy.isCascading(FooModel.class)).thenReturn(false);
		
		// Run
		long actualId = sqliteTemplate.save(foo);
		
		// Verify
		verify(mockPersistencePolicy).isPersistent(FooModel.class);
		verify(mockSqliteMapper).mapModel(foo);
		verify(mockFooModelMap).getOneToOneRelationships();
		verify(mockSqliteDb).insert(FOO_MODEL_TABLE, null, mockContentValues);
		verify(mockPersistencePolicy).isCascading(FooModel.class);
	    assertEquals("ID returned from save should be equal to the expected ID", FOO_MODEL_ID, actualId);
	}
	
	@Test
	public void testSave_noRelationships_fail() {
		// Setup
		when(mockPersistencePolicy.isPersistent(FooModel.class)).thenReturn(true);
		when(mockFooModelMap.getOneToOneRelationships()).thenReturn(new ArrayList<Pair<OneToOneRelationship, Object>>());
		when(mockSqliteDb.insert(FOO_MODEL_TABLE, null, mockContentValues)).thenReturn((long) -1);
		when(mockPersistencePolicy.isCascading(FooModel.class)).thenReturn(false);
		
		// Run
		long actualId = sqliteTemplate.save(foo);
		
		// Verify
		verify(mockPersistencePolicy).isPersistent(FooModel.class);
		verify(mockSqliteMapper).mapModel(foo);
		verify(mockFooModelMap).getOneToOneRelationships();
		verify(mockSqliteDb).insert(FOO_MODEL_TABLE, null, mockContentValues);
		verify(mockPersistencePolicy, times(0)).isCascading(FooModel.class);
	    assertEquals("ID returned from save should be -1", -1, actualId);
	}
	
	@Test
	public void testSave_oneToOneRelationship_updateRelated_success() {
		// Setup
		OneToOneRelationship rel = new OneToOneRelationship();
		List<Pair<OneToOneRelationship, Object>> fooRelationships = new ArrayList<Pair<OneToOneRelationship, Object>>();
		fooRelationships.add(new Pair<OneToOneRelationship, Object>(rel, bar));
		List<Pair<OneToOneRelationship, Object>> barRelationships = new ArrayList<Pair<OneToOneRelationship, Object>>();
		barRelationships.add(new Pair<OneToOneRelationship, Object>(rel, foo));
		when(mockPersistencePolicy.isPersistent(FooModel.class)).thenReturn(true);
		when(mockPersistencePolicy.isPKNullOrZero(foo)).thenReturn(false);
		when(mockFooModelMap.getOneToOneRelationships()).thenReturn(fooRelationships);
		when(mockClassReflector.isNull(bar)).thenReturn(false);
		when(mockSqliteUtil.getWhereClause(bar, mockSqliteMapper)).thenReturn(WHERE_CLAUSE);
		when(mockPersistencePolicy.isPKNullOrZero(bar)).thenReturn(false);
		when(mockBarModelMap.getOneToOneRelationships()).thenReturn(barRelationships);
		when(mockClassReflector.isNull(foo)).thenReturn(false);
		when(mockPersistencePolicy.findRelationshipField(BarModel.class, rel)).thenReturn(mockFooPkField);
		when(mockSqliteDb.update(BAR_MODEL_TABLE, mockContentValues, WHERE_CLAUSE, null)).thenReturn(1);
		when(mockPersistencePolicy.isCascading(BarModel.class)).thenReturn(false);
		when(mockSqliteDb.insert(FOO_MODEL_TABLE, null, mockContentValues)).thenReturn(FOO_MODEL_ID);
		when(mockPersistencePolicy.isCascading(FooModel.class)).thenReturn(false);
		
		// Run
		long actualId = sqliteTemplate.save(foo);
		
		// Verify
		verify(mockPersistencePolicy).isPersistent(FooModel.class);
		verify(mockSqliteMapper).mapModel(foo);
		verify(mockPersistencePolicy, times(2)).computeModelHash(foo);
		verify(mockPersistencePolicy).isPKNullOrZero(foo);
		verify(mockFooModelMap).getOneToOneRelationships();
		verify(mockClassReflector).isNull(bar);
		verify(mockSqliteMapper).mapModel(bar);
		verify(mockSqliteUtil).getWhereClause(bar, mockSqliteMapper);
		verify(mockPersistencePolicy).computeModelHash(bar);
		verify(mockBarModelMap).getOneToOneRelationships();
		verify(mockClassReflector).isNull(foo);
		verify(mockPersistencePolicy).getPrimaryKey(foo);
		verify(mockPersistencePolicy).findRelationshipField(BarModel.class, rel);
		verify(mockPersistencePolicy).getFieldColumnName(mockFooPkField);
		verify(mockSqliteDb).update(BAR_MODEL_TABLE, mockContentValues, WHERE_CLAUSE, null);
		verify(mockPersistencePolicy).isCascading(BarModel.class);
		verify(mockSqliteDb).insert(FOO_MODEL_TABLE, null, mockContentValues);
		verify(mockPersistencePolicy).isCascading(FooModel.class);
	    assertEquals("ID assigned to model should be equal to the expected ID", FOO_MODEL_ID, actualId);
	}
	
	@Test
	public void testSave_oneToOneRelationship_updateRelated_fail() {
		// Setup
		OneToOneRelationship rel = new OneToOneRelationship();
		List<Pair<OneToOneRelationship, Object>> fooRelationships = new ArrayList<Pair<OneToOneRelationship, Object>>();
		fooRelationships.add(new Pair<OneToOneRelationship, Object>(rel, bar));
		List<Pair<OneToOneRelationship, Object>> barRelationships = new ArrayList<Pair<OneToOneRelationship, Object>>();
		barRelationships.add(new Pair<OneToOneRelationship, Object>(rel, foo));
		when(mockPersistencePolicy.isPersistent(FooModel.class)).thenReturn(true);
		when(mockPersistencePolicy.isPKNullOrZero(foo)).thenReturn(false);
		when(mockFooModelMap.getOneToOneRelationships()).thenReturn(fooRelationships);
		when(mockClassReflector.isNull(bar)).thenReturn(false);
		when(mockSqliteUtil.getWhereClause(bar, mockSqliteMapper)).thenReturn(WHERE_CLAUSE);
		when(mockPersistencePolicy.isPKNullOrZero(bar)).thenReturn(false);
		when(mockBarModelMap.getOneToOneRelationships()).thenReturn(barRelationships);
		when(mockClassReflector.isNull(foo)).thenReturn(false);
		when(mockPersistencePolicy.findRelationshipField(BarModel.class, rel)).thenReturn(mockFooPkField);
		when(mockSqliteDb.update(BAR_MODEL_TABLE, mockContentValues, WHERE_CLAUSE, null)).thenReturn(1);
		when(mockPersistencePolicy.isCascading(BarModel.class)).thenReturn(false);
		when(mockSqliteDb.insert(FOO_MODEL_TABLE, null, mockContentValues)).thenReturn((long) -1);
		when(mockPersistencePolicy.isCascading(FooModel.class)).thenReturn(false);
		
		// Run
		long actualId = sqliteTemplate.save(foo);
		
		// Verify
		verify(mockPersistencePolicy).isPersistent(FooModel.class);
		verify(mockSqliteMapper).mapModel(foo);
		verify(mockPersistencePolicy, times(2)).computeModelHash(foo);
		verify(mockPersistencePolicy).isPKNullOrZero(foo);
		verify(mockFooModelMap).getOneToOneRelationships();
		verify(mockClassReflector).isNull(bar);
		verify(mockSqliteMapper).mapModel(bar);
		verify(mockSqliteUtil).getWhereClause(bar, mockSqliteMapper);
		verify(mockPersistencePolicy).computeModelHash(bar);
		verify(mockBarModelMap).getOneToOneRelationships();
		verify(mockClassReflector).isNull(foo);
		verify(mockPersistencePolicy).getPrimaryKey(foo);
		verify(mockPersistencePolicy).findRelationshipField(BarModel.class, rel);
		verify(mockPersistencePolicy).getFieldColumnName(mockFooPkField);
		verify(mockSqliteDb).update(BAR_MODEL_TABLE, mockContentValues, WHERE_CLAUSE, null);
		verify(mockPersistencePolicy).isCascading(BarModel.class);
		verify(mockSqliteDb).insert(FOO_MODEL_TABLE, null, mockContentValues);
		verify(mockPersistencePolicy, times(0)).isCascading(FooModel.class);
		 assertEquals("ID returned from save should be -1", -1, actualId);
	}
	
//	@Test
//	public void testSave_oneToOneRelationship_saveRelated_success() {
//		// Setup
//		OneToOneRelationship rel = new OneToOneRelationship();
//		List<Pair<OneToOneRelationship, Object>> fooRelationships = new ArrayList<Pair<OneToOneRelationship, Object>>();
//		fooRelationships.add(new Pair<OneToOneRelationship, Object>(rel, bar));
//		List<Pair<OneToOneRelationship, Object>> barRelationships = new ArrayList<Pair<OneToOneRelationship, Object>>();
//		barRelationships.add(new Pair<OneToOneRelationship, Object>(rel, foo));
//		when(mockPersistencePolicy.isPersistent(FooModel.class)).thenReturn(true);
//		when(mockPersistencePolicy.isPKNullOrZero(foo)).thenReturn(false);
//		when(mockFooModelMap.getOneToOneRelationships()).thenReturn(fooRelationships);
//		when(mockClassReflector.isNull(bar)).thenReturn(false);
//		when(mockSqliteUtil.getWhereClause(bar, mockSqliteMapper)).thenReturn(WHERE_CLAUSE);
//		when(mockPersistencePolicy.isPKNullOrZero(bar)).thenReturn(false);
//		when(mockBarModelMap.getOneToOneRelationships()).thenReturn(barRelationships);
//		when(mockClassReflector.isNull(foo)).thenReturn(false);
//		when(mockPersistencePolicy.findRelationshipField(BarModel.class, rel)).thenReturn(mockFooPkField);
//		when(mockSqliteDb.update(BAR_MODEL_TABLE, mockContentValues, WHERE_CLAUSE, null)).thenReturn(-1);
//		when(mockPersistencePolicy.isCascading(BarModel.class)).thenReturn(false);
//		when(mockSqliteDb.insert(FOO_MODEL_TABLE, null, mockContentValues)).thenReturn(FOO_MODEL_ID);
//		when(mockPersistencePolicy.isCascading(FooModel.class)).thenReturn(false);
//		
//		// Run
//		long actualId = sqliteTemplate.save(foo);
//		
//		// Verify
//		verify(mockPersistencePolicy).isPersistent(FooModel.class);
//		verify(mockSqliteMapper).mapModel(foo);
//		verify(mockPersistencePolicy, times(2)).computeModelHash(foo);
//		verify(mockPersistencePolicy).isPKNullOrZero(foo);
//		verify(mockFooModelMap).getOneToOneRelationships();
//		verify(mockClassReflector).isNull(bar);
//		verify(mockSqliteMapper, times(2)).mapModel(bar);
//		verify(mockSqliteUtil).getWhereClause(bar, mockSqliteMapper);
//		verify(mockPersistencePolicy, times(2)).computeModelHash(bar);
//		verify(mockBarModelMap).getOneToOneRelationships();
//		verify(mockClassReflector).isNull(foo);
//		verify(mockPersistencePolicy).getPrimaryKey(foo);
//		verify(mockPersistencePolicy).findRelationshipField(BarModel.class, rel);
//		verify(mockPersistencePolicy).getFieldColumnName(mockFooPkField);
//		verify(mockSqliteDb).update(BAR_MODEL_TABLE, mockContentValues, WHERE_CLAUSE, null);
//		verify(mockPersistencePolicy).isCascading(BarModel.class);
//		verify(mockSqliteDb).insert(FOO_MODEL_TABLE, null, mockContentValues);
//		verify(mockPersistencePolicy).isCascading(FooModel.class);
//	    assertEquals("ID assigned to model should be equal to the expected ID", FOO_MODEL_ID, actualId);
//	}
	
	private static class FooModel {
		public long id;
	}
	
	private static class BarModel {
		public long id;
	}
	
    private static class BazModel {
	}

}
