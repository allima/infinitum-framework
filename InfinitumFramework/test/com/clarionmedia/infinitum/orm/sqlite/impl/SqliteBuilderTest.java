package com.clarionmedia.infinitum.orm.sqlite.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import android.database.sqlite.SQLiteDatabase;

import com.clarionmedia.infinitum.context.InfinitumContext;
import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;
import com.clarionmedia.infinitum.exception.InfinitumRuntimeException;
import com.clarionmedia.infinitum.orm.criteria.Criteria;
import com.clarionmedia.infinitum.orm.criteria.criterion.Criterion;
import com.clarionmedia.infinitum.orm.persistence.PersistencePolicy;
import com.clarionmedia.infinitum.orm.persistence.TypeResolutionPolicy.SqliteDataType;
import com.clarionmedia.infinitum.orm.relationship.ManyToManyRelationship;
import com.clarionmedia.infinitum.reflection.PackageReflector;
import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class SqliteBuilderTest {
	
	private static final String MODEL_TABLE_1 = "table1";
	private static final String MODEL_TABLE_2 = "table2";
	private static final String MTM_TABLE = "join_table";
	private static final String CRITERION_A_SQL = "foo = bar";
	private static final String CRITERION_B_SQL = "baz = 42";
	private static final String MOCK_TYPE_A = "A";
	private static final String MOCK_TYPE_B = "B";
	private static final String MOCK_TYPE_C = "C";
	
	@Mock
	private SqliteMapper mockSqliteMapper;
	
	@Mock
	private PersistencePolicy mockPersistencePolicy;
	
	@Mock
	private PackageReflector mockPackageReflector;
	
	@Mock
	private InfinitumContext mockInfinitumContext;
	
	@Mock
	private SqliteDbHelper mockDbHelper;
	
	@Mock
	private SQLiteDatabase mockSqliteDb;
	
	@Mock
	private ManyToManyRelationship mockManyToManyRelationship;
	
	@Mock
	private Criteria<?> mockCriteria;
	
	@Mock
	private Criterion mockCriterionA;
	
	@Mock
	private Criterion mockCriterionB;
	
	@InjectMocks
	private SqliteBuilder sqliteBuilder = new SqliteBuilder();
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		List<String> mockDomainTypes = new ArrayList<String>();
		mockDomainTypes.add(MOCK_TYPE_A);
		mockDomainTypes.add(MOCK_TYPE_B);
		mockDomainTypes.add(MOCK_TYPE_C);
		when(mockInfinitumContext.getDomainTypes()).thenReturn(mockDomainTypes);
		when(mockDbHelper.getDatabase()).thenReturn(mockSqliteDb);
		when(mockPersistencePolicy.isPersistent(Long.class)).thenReturn(true);
		when(mockPersistencePolicy.isPersistent(Integer.class)).thenReturn(true);
		when(mockPersistencePolicy.getPersistentFields(Long.class)).thenReturn(Arrays.asList(ArrayList.class.getDeclaredFields()));
		when(mockPersistencePolicy.getPersistentFields(Integer.class)).thenReturn(Arrays.asList(ArrayList.class.getDeclaredFields()));
		Field field = ArrayList.class.getDeclaredFields()[0];
		when(mockManyToManyRelationship.getTableName()).thenReturn(MTM_TABLE);
		when(mockManyToManyRelationship.getFirstField()).thenReturn(field);
		when(mockManyToManyRelationship.getSecondField()).thenReturn(field);
		when(mockSqliteMapper.getSqliteDataType(field)).thenReturn(SqliteDataType.TEXT);
		when(mockCriterionA.toSql(mockCriteria)).thenReturn(CRITERION_A_SQL);
		when(mockCriterionB.toSql(mockCriteria)).thenReturn(CRITERION_B_SQL);
		doReturn(Integer.class).when(mockManyToManyRelationship).getFirstType();
		doReturn(Long.class).when(mockManyToManyRelationship).getSecondType();
		doReturn(Integer.class).when(mockPackageReflector).getClass(any(String.class));
		doReturn(Long.class).when(mockPackageReflector).getClass(any(String.class));
	}
	
	@Test
	public void testCreateTables_success() {
		// Run
		int result = sqliteBuilder.createTables(mockDbHelper);
		
		// Verify
		verify(mockSqliteDb, times(3)).execSQL(any(String.class));
		assertEquals("Returned value should be 3", 3, result);
	}
	
	@Test(expected = InfinitumConfigurationException.class)
	public void testCreateTables_fail() {
		// Setup
		doReturn(null).when(mockPackageReflector).getClass(any(String.class));
		
		// Run
		int result = sqliteBuilder.createTables(mockDbHelper);
		
		// Verify
		verify(mockSqliteDb, times(0)).execSQL(any(String.class));
		assertEquals("Returned value should be 0", 0, result);
	}
	
	@Test
	public void testCreateTables_withManyToMany_success() {
		// Setup
		Map<Field, ManyToManyRelationship> mockCache = new HashMap<Field, ManyToManyRelationship>();
		Field field = ArrayList.class.getDeclaredFields()[0];
		mockCache.put(field, mockManyToManyRelationship);
		when(mockPersistencePolicy.getManyToManyCache()).thenReturn(mockCache);
		
		// Run
		int result = sqliteBuilder.createTables(mockDbHelper);
		
		// Verify
		verify(mockSqliteDb, times(4)).execSQL(any(String.class));
		assertEquals("Returned value should be 4", 4, result);
	}
	
	@Test
	public void testDropTables_success() {
		// Run
		int result = sqliteBuilder.dropTables(mockDbHelper);
		
		// Verify
		verify(mockSqliteDb, times(3)).execSQL(any(String.class));
		assertEquals("Returned value should be 3", 3, result);
	}
	
	@Test(expected = InfinitumConfigurationException.class)
	public void testDropTables_fail() {
		// Setup
		doReturn(null).when(mockPackageReflector).getClass(any(String.class));
		
		// Run
		int result = sqliteBuilder.dropTables(mockDbHelper);
		
		// Verify
		verify(mockSqliteDb, times(0)).execSQL(any(String.class));
		assertEquals("Returned value should be 0", 0, result);
	}
	
	@Test
	public void testCreateQuery_singleCriterion_noLimitOrOffset() {
		// Setup
		doReturn(Object.class).when(mockCriteria).getEntityClass();
		List<Criterion> mockCriterionList = new ArrayList<Criterion>();
		mockCriterionList.add(mockCriterionA);
		when(mockCriteria.getCriterion()).thenReturn(mockCriterionList);
		when(mockCriteria.getLimit()).thenReturn(0);
		when(mockCriteria.getOffset()).thenReturn(0);
		when(mockPersistencePolicy.getModelTableName(Object.class)).thenReturn(MODEL_TABLE_1);
		
		// Run
		String expected = "SELECT * FROM " + MODEL_TABLE_1 + " WHERE " + CRITERION_A_SQL;
		String actual = sqliteBuilder.createQuery(mockCriteria);
		
		// Verify
		verify(mockCriteria).getEntityClass();
		verify(mockCriteria).getCriterion();
		verify(mockCriteria).getLimit();
		verify(mockCriteria).getOffset();
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}
	
	@Test
	public void testCreateQuery_singleCriterion_limitAndOffset() {
		// Setup
		doReturn(Object.class).when(mockCriteria).getEntityClass();
		List<Criterion> mockCriterionList = new ArrayList<Criterion>();
		mockCriterionList.add(mockCriterionA);
		when(mockCriteria.getCriterion()).thenReturn(mockCriterionList);
		when(mockCriteria.getLimit()).thenReturn(10);
		when(mockCriteria.getOffset()).thenReturn(5);
		when(mockPersistencePolicy.getModelTableName(Object.class)).thenReturn(MODEL_TABLE_1);
		
		// Run
		String expected = "SELECT * FROM " + MODEL_TABLE_1 + " WHERE " + CRITERION_A_SQL + " LIMIT 10 OFFSET 5";
		String actual = sqliteBuilder.createQuery(mockCriteria);
		
		// Verify
		verify(mockCriteria).getEntityClass();
		verify(mockCriteria).getCriterion();
		verify(mockCriteria, times(2)).getLimit();
		verify(mockCriteria, times(2)).getOffset();
		verify(mockCriterionA).toSql(mockCriteria);
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}
	
	@Test
	public void testCreateQuery_multipleCriterion_noLimitOrOffset() {
		// Setup
		doReturn(Object.class).when(mockCriteria).getEntityClass();
		List<Criterion> mockCriterionList = new ArrayList<Criterion>();
		mockCriterionList.add(mockCriterionA);
		mockCriterionList.add(mockCriterionB);
		when(mockCriteria.getCriterion()).thenReturn(mockCriterionList);
		when(mockCriteria.getLimit()).thenReturn(0);
		when(mockCriteria.getOffset()).thenReturn(0);
		when(mockPersistencePolicy.getModelTableName(Object.class)).thenReturn(MODEL_TABLE_1);
		
		// Run
		String expected = "SELECT * FROM " + MODEL_TABLE_1 + " WHERE " + CRITERION_A_SQL + " AND " + CRITERION_B_SQL;
		String actual = sqliteBuilder.createQuery(mockCriteria);
		
		// Verify
		verify(mockCriteria).getEntityClass();
		verify(mockCriteria).getCriterion();
		verify(mockCriteria).getLimit();
		verify(mockCriteria).getOffset();
		verify(mockCriterionA).toSql(mockCriteria);
		verify(mockCriterionB).toSql(mockCriteria);
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}
	
	@Test
	public void testCreateQuery_multipleCriterion_limitAndOffset() {
		// Setup
		doReturn(Object.class).when(mockCriteria).getEntityClass();
		List<Criterion> mockCriterionList = new ArrayList<Criterion>();
		mockCriterionList.add(mockCriterionA);
		mockCriterionList.add(mockCriterionB);
		when(mockCriteria.getCriterion()).thenReturn(mockCriterionList);
		when(mockCriteria.getLimit()).thenReturn(10);
		when(mockCriteria.getOffset()).thenReturn(5);
		when(mockPersistencePolicy.getModelTableName(Object.class)).thenReturn(MODEL_TABLE_1);
		
		// Run
		String expected = "SELECT * FROM " + MODEL_TABLE_1 + " WHERE " + CRITERION_A_SQL + " AND " + CRITERION_B_SQL + " LIMIT 10 OFFSET 5";
		String actual = sqliteBuilder.createQuery(mockCriteria);
		
		// Verify
		verify(mockCriteria).getEntityClass();
		verify(mockCriteria).getCriterion();
		verify(mockCriteria, times(2)).getLimit();
		verify(mockCriteria, times(2)).getOffset();
		verify(mockCriterionA).toSql(mockCriteria);
		verify(mockCriterionB).toSql(mockCriteria);
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}
	
	@Test
	public void testCreateCountQuery_singleCriterion_noLimitOrOffset() {
		// Setup
		doReturn(Object.class).when(mockCriteria).getEntityClass();
		List<Criterion> mockCriterionList = new ArrayList<Criterion>();
		mockCriterionList.add(mockCriterionA);
		when(mockCriteria.getCriterion()).thenReturn(mockCriterionList);
		when(mockCriteria.getLimit()).thenReturn(0);
		when(mockCriteria.getOffset()).thenReturn(0);
		when(mockPersistencePolicy.getModelTableName(Object.class)).thenReturn(MODEL_TABLE_1);
		
		// Run
		String expected = "SELECT count(*) FROM " + MODEL_TABLE_1 + " WHERE " + CRITERION_A_SQL;
		String actual = sqliteBuilder.createCountQuery(mockCriteria);
		
		// Verify
		verify(mockCriteria).getEntityClass();
		verify(mockCriteria).getCriterion();
		verify(mockCriteria).getLimit();
		verify(mockCriteria).getOffset();
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}
	
	@Test
	public void testCreateCountQuery_singleCriterion_limitAndOffset() {
		// Setup
		doReturn(Object.class).when(mockCriteria).getEntityClass();
		List<Criterion> mockCriterionList = new ArrayList<Criterion>();
		mockCriterionList.add(mockCriterionA);
		when(mockCriteria.getCriterion()).thenReturn(mockCriterionList);
		when(mockCriteria.getLimit()).thenReturn(10);
		when(mockCriteria.getOffset()).thenReturn(5);
		when(mockPersistencePolicy.getModelTableName(Object.class)).thenReturn(MODEL_TABLE_1);
		
		// Run
		String expected = "SELECT count(*) FROM " + MODEL_TABLE_1 + " WHERE " + CRITERION_A_SQL + " LIMIT 10 OFFSET 5";
		String actual = sqliteBuilder.createCountQuery(mockCriteria);
		
		// Verify
		verify(mockCriteria).getEntityClass();
		verify(mockCriteria).getCriterion();
		verify(mockCriteria, times(2)).getLimit();
		verify(mockCriteria, times(2)).getOffset();
		verify(mockCriterionA).toSql(mockCriteria);
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}
	
	@Test
	public void testCreateCountQuery_multipleCriterion_noLimitOrOffset() {
		// Setup
		doReturn(Object.class).when(mockCriteria).getEntityClass();
		List<Criterion> mockCriterionList = new ArrayList<Criterion>();
		mockCriterionList.add(mockCriterionA);
		mockCriterionList.add(mockCriterionB);
		when(mockCriteria.getCriterion()).thenReturn(mockCriterionList);
		when(mockCriteria.getLimit()).thenReturn(0);
		when(mockCriteria.getOffset()).thenReturn(0);
		when(mockPersistencePolicy.getModelTableName(Object.class)).thenReturn(MODEL_TABLE_1);
		
		// Run
		String expected = "SELECT count(*) FROM " + MODEL_TABLE_1 + " WHERE " + CRITERION_A_SQL + " AND " + CRITERION_B_SQL;
		String actual = sqliteBuilder.createCountQuery(mockCriteria);
		
		// Verify
		verify(mockCriteria).getEntityClass();
		verify(mockCriteria).getCriterion();
		verify(mockCriteria).getLimit();
		verify(mockCriteria).getOffset();
		verify(mockCriterionA).toSql(mockCriteria);
		verify(mockCriterionB).toSql(mockCriteria);
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}
	
	@Test
	public void testCreateCountQuery_multipleCriterion_limitAndOffset() {
		// Setup
		doReturn(Object.class).when(mockCriteria).getEntityClass();
		List<Criterion> mockCriterionList = new ArrayList<Criterion>();
		mockCriterionList.add(mockCriterionA);
		mockCriterionList.add(mockCriterionB);
		when(mockCriteria.getCriterion()).thenReturn(mockCriterionList);
		when(mockCriteria.getLimit()).thenReturn(10);
		when(mockCriteria.getOffset()).thenReturn(5);
		when(mockPersistencePolicy.getModelTableName(Object.class)).thenReturn(MODEL_TABLE_1);
		
		// Run
		String expected = "SELECT count(*) FROM " + MODEL_TABLE_1 + " WHERE " + CRITERION_A_SQL + " AND " + CRITERION_B_SQL + " LIMIT 10 OFFSET 5";
		String actual = sqliteBuilder.createCountQuery(mockCriteria);
		
		// Verify
		verify(mockCriteria).getEntityClass();
		verify(mockCriteria).getCriterion();
		verify(mockCriteria, times(2)).getLimit();
		verify(mockCriteria, times(2)).getOffset();
		verify(mockCriterionA).toSql(mockCriteria);
		verify(mockCriterionB).toSql(mockCriteria);
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}
	
	@Test
	public void testCreateManyToManyJoinQuery_firstType_success() {
		// Setup
		ManyToManyRelationship mockRelationship = mock(ManyToManyRelationship.class);
		doReturn(Integer.class).when(mockRelationship).getFirstType();
		doReturn(Long.class).when(mockRelationship).getSecondType();
		Field field = ArrayList.class.getDeclaredFields()[0];
		when(mockRelationship.getFirstField()).thenReturn(field);
		when(mockRelationship.getSecondField()).thenReturn(field);
		when(mockRelationship.contains(Integer.class)).thenReturn(true);
		when(mockRelationship.getTableName()).thenReturn(MTM_TABLE);
		final String COL_NAME = "col";
		when(mockPersistencePolicy.getFieldColumnName(field)).thenReturn(COL_NAME);
		Serializable id = 42;
		when(mockSqliteMapper.getSqliteDataType(id)).thenReturn(SqliteDataType.INTEGER);
		when(mockPersistencePolicy.getModelTableName(Integer.class)).thenReturn(MODEL_TABLE_1);
		when(mockPersistencePolicy.getModelTableName(Long.class)).thenReturn(MODEL_TABLE_2);
		
		// Run
		String expected = "SELECT x.* FROM table1 x, table2 y, join_table z WHERE z.table1_col = x.col AND z.table2_col = y.col AND y.col = 42";
		String actual = sqliteBuilder.createManyToManyJoinQuery(mockRelationship, id, Integer.class);
		
		// Verify
		verify(mockRelationship).contains(Integer.class);
		verify(mockRelationship, times(4)).getFirstType();
		verify(mockRelationship, times(3)).getSecondType();
		verify(mockRelationship, times(2)).getFirstField();
		verify(mockRelationship, times(3)).getSecondField();
		verify(mockRelationship).getTableName();
		verify(mockPersistencePolicy, times(2)).getModelTableName(mockRelationship.getFirstType());
		verify(mockPersistencePolicy, times(2)).getModelTableName(mockRelationship.getSecondType());
		verify(mockPersistencePolicy, times(5)).getFieldColumnName(field);
		verify(mockSqliteMapper).getSqliteDataType(id);
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}
	
	@Test
	public void testCreateManyToManyJoinQuery_secondType_success() {
		// Setup
		ManyToManyRelationship mockRelationship = mock(ManyToManyRelationship.class);
		doReturn(Integer.class).when(mockRelationship).getFirstType();
		doReturn(Long.class).when(mockRelationship).getSecondType();
		Field field = ArrayList.class.getDeclaredFields()[0];
		when(mockRelationship.getFirstField()).thenReturn(field);
		when(mockRelationship.getSecondField()).thenReturn(field);
		when(mockRelationship.contains(Long.class)).thenReturn(true);
		when(mockRelationship.getTableName()).thenReturn(MTM_TABLE);
		final String COL_NAME = "col";
		when(mockPersistencePolicy.getFieldColumnName(field)).thenReturn(COL_NAME);
		Serializable id = 42;
		when(mockSqliteMapper.getSqliteDataType(id)).thenReturn(SqliteDataType.INTEGER);
		when(mockPersistencePolicy.getModelTableName(Integer.class)).thenReturn(MODEL_TABLE_1);
		when(mockPersistencePolicy.getModelTableName(Long.class)).thenReturn(MODEL_TABLE_2);
		
		// Run
		String expected = "SELECT x.* FROM table1 y, table2 x, join_table z WHERE z.table2_col = x.col AND z.table1_col = y.col AND y.col = 42";
		String actual = sqliteBuilder.createManyToManyJoinQuery(mockRelationship, id, Long.class);
		
		// Verify
		verify(mockRelationship).contains(Long.class);
		verify(mockRelationship, times(4)).getFirstType();
		verify(mockRelationship, times(3)).getSecondType();
		verify(mockRelationship, times(3)).getFirstField();
		verify(mockRelationship, times(2)).getSecondField();
		verify(mockRelationship).getTableName();
		verify(mockPersistencePolicy, times(2)).getModelTableName(mockRelationship.getFirstType());
		verify(mockPersistencePolicy, times(2)).getModelTableName(mockRelationship.getSecondType());
		verify(mockPersistencePolicy, times(5)).getFieldColumnName(field);
		verify(mockSqliteMapper).getSqliteDataType(id);
		assertEquals("Returned SQL query should match expected value", expected, actual);
	}
	
	@Test(expected = InfinitumRuntimeException.class)
	public void testCreateManyToManyJoinQuery_fail() {
		// Setup
		ManyToManyRelationship mockRelationship = mock(ManyToManyRelationship.class);
		doReturn(Integer.class).when(mockRelationship).getFirstType();
		doReturn(Long.class).when(mockRelationship).getSecondType();
		Field field = ArrayList.class.getDeclaredFields()[0];
		when(mockRelationship.getFirstField()).thenReturn(field);
		when(mockRelationship.getSecondField()).thenReturn(field);
		when(mockRelationship.contains(Integer.class)).thenReturn(false);
		when(mockRelationship.getTableName()).thenReturn(MTM_TABLE);
		final String COL_NAME = "col";
		when(mockPersistencePolicy.getFieldColumnName(field)).thenReturn(COL_NAME);
		Serializable id = 42;
		when(mockSqliteMapper.getSqliteDataType(id)).thenReturn(SqliteDataType.INTEGER);
		when(mockPersistencePolicy.getModelTableName(Integer.class)).thenReturn(MODEL_TABLE_1);
		when(mockPersistencePolicy.getModelTableName(Long.class)).thenReturn(MODEL_TABLE_2);
		
		// Run
		sqliteBuilder.createManyToManyJoinQuery(mockRelationship, id, Integer.class);
	}
	
	@Test
	public void testCreateDeleteStaleRelationshipQuery_singleStale() {
		// Setup
		List<Serializable> staleKeys = new ArrayList<Serializable>();
		Serializable id = 42;
		Serializable entityPk = 100;
		staleKeys.add(id);
		final String COL_NAME = "id";
		Object entity = new Object();
		when(mockPersistencePolicy.getModelTableName(mockManyToManyRelationship.getFirstType())).thenReturn(MODEL_TABLE_1);
		when(mockPersistencePolicy.getModelTableName(mockManyToManyRelationship.getSecondType())).thenReturn(MODEL_TABLE_2);
		when(mockPersistencePolicy.getFieldColumnName(mockManyToManyRelationship.getFirstField())).thenReturn(COL_NAME);
		when(mockPersistencePolicy.getFieldColumnName(mockManyToManyRelationship.getSecondField())).thenReturn(COL_NAME);
		when(mockPersistencePolicy.getPrimaryKey(entity)).thenReturn(entityPk);
		when(mockSqliteMapper.getSqliteDataType(id)).thenReturn(SqliteDataType.INTEGER);
		
		// Run
		String expected = "DELETE FROM " + MTM_TABLE + " WHERE " + MODEL_TABLE_2 + "_" + COL_NAME + " = '100' AND " + MODEL_TABLE_1 + "_id NOT IN (" + 42 + ")";
		String actual = sqliteBuilder.createDeleteStaleRelationshipQuery(mockManyToManyRelationship, entity, staleKeys);
		
		// Verify
		assertEquals("Returned SQL query should match expected value", expected, actual);
		// TODO finish verifying
	}

}
