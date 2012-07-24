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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import android.content.Context;

import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.logging.Logger;
import com.clarionmedia.infinitum.orm.Session;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class SqliteSessionTest {
	
	private static final int FOO_MODEL_HASH = 42;
	private static final int BAR_MODEL_HASH = 38;
	private static final int BAZ_MODEL_HASH = 176;
	private static final long MODEL_ID = 120;
	
	@SuppressWarnings("deprecation")
	@InjectMocks
	private SqliteSession sqliteSession = new SqliteSession();
	
	@Mock
	private SqliteTemplate mockSqliteTemplate;
	
	@Mock
	private PersistencePolicy mockPersistencePolicy;
	
	@Mock
	private InfinitumContext mockInfinitumContext;
	
	@Mock
	private Logger mockLogger;
	
	@Mock
	private Context mockContext;
	
	@Mock
	private Map<Integer, Object> mockSessionCache;
	
	@Mock
	private Criteria<FooModel> mockFooCriteria;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		sqliteSession.open();
	}
	
	@After
	public void tearDown() {
		sqliteSession.close();
	}
	
	@Test
	public void testOpen() {
		// Run
		Session session = sqliteSession.open();
		
		// Verify
		verify(mockSqliteTemplate, times(2)).open();
		assertEquals("Session returned from open should be the same Session instance", session, sqliteSession);
	}
	
	@Test
	public void testClose() {
		/// Run
		Session session = sqliteSession.close();
		
		// Verify
		verify(mockSqliteTemplate).close();
		assertEquals("Session returned from close should be the same Session instance", session, sqliteSession);
	}
	
	@Test
	public void testIsOpen() {
		// Setup
		when(mockSqliteTemplate.isOpen()).thenReturn(true);
		
		// Run
		boolean isOpen = sqliteSession.isOpen();
		
		// Verify
		verify(mockSqliteTemplate).isOpen();
		assertTrue("Session should be open", isOpen);
	}
	
	@Test
	public void testRecycleCache() {
		// Run
		Session session = sqliteSession.recycleCache();
		
		// Verify
		verify(mockSessionCache).clear();
		assertEquals("Session returned from recycleCache should be the same Session instance", session, sqliteSession);
	}
	
	@Test
	public void testCreateCriteria() {
		// Setup
		when(mockSqliteTemplate.createCriteria(FooModel.class)).thenReturn(mockFooCriteria);
		
		// Run
		Criteria<FooModel> actualCriteria = sqliteSession.createCriteria(FooModel.class);
		
		// Verify
		verify(mockSqliteTemplate).createCriteria(FooModel.class);
		assertEquals("Criteria returned from createCriteria should be the same Criteria instance returned from mockSqliteTemplate", mockFooCriteria, actualCriteria);
	}
	
	@Test
	public void testSave_success() {
		// Setup
		FooModel foo = new FooModel();
		when(mockSqliteTemplate.save(foo)).thenReturn(MODEL_ID);
		when(mockPersistencePolicy.computeModelHash(foo)).thenReturn(FOO_MODEL_HASH);
		
		// Run
		long actualId = sqliteSession.save(foo);
		
		// Verify
		verify(mockSqliteTemplate).save(foo);
		verify(mockInfinitumContext).isCacheRecyclable();
		verify(mockPersistencePolicy).computeModelHash(foo);
		verify(mockSessionCache).put(FOO_MODEL_HASH, foo);
		assertEquals("Returned ID should be equal to the model ID", MODEL_ID, actualId);
	}
	
	@Test
	public void testSave_fail() {
		// Setup
		FooModel foo = new FooModel();
		when(mockSqliteTemplate.save(foo)).thenReturn((long) -1);
		
		// Run
		long actualId = sqliteSession.save(foo);
		
		// Verify
		verify(mockSqliteTemplate).save(foo);
		verify(mockPersistencePolicy, times(0)).computeModelHash(foo);
		verify(mockSessionCache, times(0)).put(FOO_MODEL_HASH, foo);
		assertEquals("Returned ID should be -1", -1, actualId);
	}
	
	@Test
	public void testUpdate_success() {
		// Setup
		FooModel foo = new FooModel();
		when(mockSqliteTemplate.update(foo)).thenReturn(true);
		when(mockPersistencePolicy.computeModelHash(foo)).thenReturn(FOO_MODEL_HASH);
		
		// Run
		boolean success = sqliteSession.update(foo);
		
		// Verify
		verify(mockSqliteTemplate).update(foo);
		verify(mockPersistencePolicy).computeModelHash(foo);
		verify(mockSessionCache).put(FOO_MODEL_HASH, foo);
		assertEquals("Save should have returned successfully", true, success);
	}
	
	@Test
	public void testUpdate_fail() {
		// Setup
		FooModel foo = new FooModel();
		when(mockSqliteTemplate.update(foo)).thenReturn(false);
		
		// Run
		boolean success = sqliteSession.update(foo);
		
		// Verify
		verify(mockSqliteTemplate).update(foo);
		verify(mockPersistencePolicy, times(0)).computeModelHash(foo);
		verify(mockSessionCache, times(0)).put(FOO_MODEL_HASH, foo);
		assertEquals("Save should have returned unsuccessfully", false, success);
	}
	
	@Test
	public void testDelete_success() {
		// Setup
		FooModel foo = new FooModel();
		when(mockSqliteTemplate.delete(foo)).thenReturn(true);
		when(mockPersistencePolicy.computeModelHash(foo)).thenReturn(FOO_MODEL_HASH);
		
		// Run
		boolean success = sqliteSession.delete(foo);
		
		// Verify
		verify(mockSqliteTemplate).delete(foo);
		verify(mockPersistencePolicy).computeModelHash(foo);
		verify(mockSessionCache).remove(FOO_MODEL_HASH);
		assertEquals("Delete should have returned successfully", true, success);
	}
	
	@Test
	public void testDelete_fail() {
		// Setup
		FooModel foo = new FooModel();
		when(mockSqliteTemplate.delete(foo)).thenReturn(false);
		
		// Run
		boolean success = sqliteSession.delete(foo);
		
		// Verify
		verify(mockSqliteTemplate).delete(foo);
		verify(mockPersistencePolicy, times(0)).computeModelHash(foo);
		verify(mockSessionCache, times(0)).remove(FOO_MODEL_HASH);
		assertEquals("Delete should have returned unsuccessfully", false, success);
	}
	
	@Test
	public void testSaveOrUpdate_success() {
		// Setup
		FooModel foo = new FooModel();
		when(mockSqliteTemplate.saveOrUpdate(foo)).thenReturn(MODEL_ID);
		when(mockPersistencePolicy.computeModelHash(foo)).thenReturn(FOO_MODEL_HASH);
		
		// Run
		long actualId = sqliteSession.saveOrUpdate(foo);
		
		// Verify
		verify(mockSqliteTemplate).saveOrUpdate(foo);
		verify(mockPersistencePolicy).computeModelHash(foo);
		verify(mockSessionCache).put(FOO_MODEL_HASH, foo);
		assertEquals("Returned ID should be equal to the model ID", MODEL_ID, actualId);
	}
	
	@Test
	public void testSaveOrUpdate_fail() {
		// Setup
		FooModel foo = new FooModel();
		when(mockSqliteTemplate.saveOrUpdate(foo)).thenReturn((long) -1);
		
		// Run
		long actualId = sqliteSession.saveOrUpdate(foo);
		
		// Verify
		verify(mockSqliteTemplate).saveOrUpdate(foo);
		verify(mockPersistencePolicy, times(0)).computeModelHash(foo);
		verify(mockSessionCache, times(0)).put(FOO_MODEL_HASH, foo);
		assertEquals("Returned ID should be -1", -1, actualId);
	}
	
	@Test
	public void testSaveOrUpdateAll() {
		// Setup
		FooModel foo = new FooModel();
		BarModel bar = new BarModel();
		BazModel baz = new BazModel();
		List<Object> models = new ArrayList<Object>();
		models.add(foo);
		models.add(bar);
		models.add(baz);
		when(mockSqliteTemplate.saveOrUpdate(foo)).thenReturn(MODEL_ID);
		when(mockSqliteTemplate.saveOrUpdate(bar)).thenReturn((long) -1);
		when(mockSqliteTemplate.saveOrUpdate(baz)).thenReturn((long) 0);
		when(mockPersistencePolicy.computeModelHash(foo)).thenReturn(FOO_MODEL_HASH);
		when(mockPersistencePolicy.computeModelHash(baz)).thenReturn(BAZ_MODEL_HASH);
		
		// Run
		int actualResults = sqliteSession.saveOrUpdateAll(models);
		
		// Verify
		verify(mockSqliteTemplate).saveOrUpdate(foo);
		verify(mockSqliteTemplate).saveOrUpdate(bar);
		verify(mockSqliteTemplate).saveOrUpdate(baz);
		verify(mockPersistencePolicy).computeModelHash(foo);
		verify(mockPersistencePolicy).computeModelHash(baz);
		verify(mockPersistencePolicy, times(0)).computeModelHash(bar);
		verify(mockSessionCache).put(FOO_MODEL_HASH, foo);
		verify(mockSessionCache).put(BAZ_MODEL_HASH, baz);
		assertEquals("Number of items saved or updated should be 2", 2, actualResults);
	}
	
	private static class FooModel {
		
	}
	
	private static class BarModel {
		
	}
	
    private static class BazModel {
		
	}

}
