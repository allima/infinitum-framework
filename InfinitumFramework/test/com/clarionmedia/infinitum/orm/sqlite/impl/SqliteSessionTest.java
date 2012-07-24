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
	
	private static final int MODEL_HASH = 42;
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
		when(mockPersistencePolicy.computeModelHash(foo)).thenReturn(MODEL_HASH);
		
		// Run
		long actualId = sqliteSession.save(foo);
		
		// Verify
		verify(mockSqliteTemplate).save(foo);
		verify(mockInfinitumContext).isCacheRecyclable();
		verify(mockPersistencePolicy).computeModelHash(foo);
		verify(mockSessionCache).put(MODEL_HASH, foo);
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
		assertEquals("Returned ID should be -1", -1, actualId);
	}
	
	private static class FooModel {
		
	}

}
