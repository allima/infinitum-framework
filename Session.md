**Contents**


# Introduction #

A `Session` represents the lifecycle of an Infinitum persistence service and acts as an interface to a configured application datastore. All database interaction should go through the `Session`, which also provides an API for creating [Criteria](Criteria.md) instances.

The `Session` interface has two primary implementations, [SqliteSession](http://code.google.com/p/infinitum-framework/wiki/Session#SqliteSession) and [RestfulSession](http://code.google.com/p/infinitum-framework/wiki/Session#RestfulSession). The former is used to interact with a SQLite database, while the latter is used to communicate with a RESTful web service. Both are used to perform datastore CRUD operations using model objects. Note that, although both implement the same interface, some operations may not be supported by one or the other implementation. For instance, `SqliteSession` supports transactions, but `RestfulSession` has no notion of them.

`Session` instances should be acquired from an InfinitumContext. When a `Session` is acquired, it must be opened before any transactions take place. Subsequently, it should also be closed to close the persistence service and clean up any resources.

In order to keep track of transient and persistent entities as well as minimize unnecessary datastore calls, `Session` implements a `Session` cache. This is implemented as a least-recently-used cache, meaning whenever the cache reaches its maximum capacity, the least-recently-used object will be evicted. If memory is critical, the cache can be explicitly recycled by invoking `recycleCache()` on the `Session`, which will evict all entries. Additionally, the cache size can be modified by calling `setCacheSize(int)`. It's worth noting that the `Session` cache's lifecycle spans the `Session` scope. This means that the cache is cleared when the `Session` closes.

# Acquiring a Session #

The code below shows how a `SqliteSession` is acquired.

```
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    Session session = ContextFactory.newInstance().configure(this).getSession(DataSource.Sqlite);
    session.open(); // open for transactions
    // database transactions
    session.close(); // transactions may no longer be executed
}
```

Likewise, a `RestfulSession` is acquired using its respective `DataSource`:

```
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    Session session = ContextFactory.newInstance().configure(this).getSession(DataSource.Rest);
    session.open(); // open for transactions
    // REST transactions
    session.close(); // transactions may no longer be executed
}
```

# Runtime Configuration #

A `Session`'s configuration is pulled from an application's `InfinitumContext`; however, its configuration may also be modified at runtime. For example, autocommit may be enabled or the cache size may be altered.

```
session.setAutocommit(true);
session.setCacheSize(500);
```

# Basic CRUD Operations #

`Session` provides methods to easily save, update, or delete domain objects or collections of domain objects, making database persistence transparent. These methods are exemplified below.

## Persisting Domain Objects ##

```
// Save a single domain object
session.open();
session.save(new Foo());
session.close();

// Save a collection of domain objects
List<Foo> foos = getFooList();
session.open();
session.saveAll(foos);
session.close();
```

## Updating Domain Objects ##

```
// Update a single domain object
Foo foo = getFoo();
session.open();
session.update(foo);
session.close();

// Save or update a single domain object
Foo foo = getFoo();
session.open();
session.saveOrUpdate(foo);
session.close();

// Save or update a collection of domain objects
List<Foo> foos = getFooList();
session.open();
session.saveOrUpdateAll(foos);
session.close();
```

## Deleting Domain Objects ##

```
// Delete a single domain object
Foo foo = getFoo();
session.open();
session.delete(foo);
session.close();

// Delete a collection of domain objects
List<Foo> foos = getFooList();
session.open();
session.deleteAll(foos);
session.close();
```

## Loading Domain Objects ##

```
// Load a single domain object by its ID
session.open();
Foo foo = session.load(Foo.class, 42);
session.close();
```

The `Criteria` API can be used to execute more advanced queries.

# Registering TypeAdapters #

In order to provide ORM support for unknown data types, Infinitum uses a TypeAdapter. A `TypeAdapter` can be registered with a `Session` for a certain class, allowing fields of that class to be mapped to a database table or web service model. There are various derivatives of the `TypeAdapter` interface in order to accommodate ORM support for SQLite and web services, such as [SqliteTypeAdapter](http://code.google.com/p/infinitum-framework/wiki/TypeAdapter#SqliteTypeAdapter), [RestfulPairsTypeAdapter](http://code.google.com/p/infinitum-framework/wiki/TypeAdapter#RestfulPairsTypeAdapter), [RestfulJsonTypeAdapter](http://code.google.com/p/infinitum-framework/wiki/TypeAdapter#RestfulJsonTypeAdapter), and [RestfulXmlTypeAdapter](http://code.google.com/p/infinitum-framework/wiki/TypeAdapter#RestfulXmlTypeAdapter).

Registering a `TypeAdapter` for a class which already has a `TypeAdapter` registered for it will result in the previous `TypeAdapter` being overridden. The example below, although slightly contrived, shows how a [JodaTime](http://joda-time.sourceforge.net/) `DateTime` object might be mapped to a SQLite column and vice versa using a `SqliteTypeAdapter`.

## TypeAdapter Example ##

```
// Store DateTimes as strings
session.registerTypeAdapter(DateTime.class, new SqliteTypeAdapter<DateTime>(SqliteDataType.TEXT) {
    @Override
    public void mapToObject(ResultSet result, int index, Field field, Object model) throws IllegalArgumentException, IllegalAccessException {
        field.set(model, result.getString(index));
    }
    @Override
    public void mapToColumn(DateTime value, String column, ContentValues values) {
	String dateStr = value.getYear() + "-" + value.getMonthOfYear() + "-" + value.getDayOfMonth();
	values.put(column, dateStr);
    }
    @Override
    public void mapObjectToColumn(Object value, String column, ContentValues values) {
        DateTime dt = (DateTime) value;
	String dateStr = dt.getYear() + "-" + dt.getMonthOfYear() + "-" + dt.getDayOfMonth();
	values.put(column, dateStr);
    }
});
```

# SqliteSession #

`SqliteSession` is an implementation of the [Session](Session.md) interface for interacting with a SQLite database. Among other things, it provides support for transactions, autocommit, and [Criteria](Criteria.md) queries.

## Criteria Queries ##

Further supporting the notion of persistence transparency, `SqliteSession` provides supoort for a Criteria API which is used to construct object-oriented database queries, removing the need for SQL. `Criteria` queries are used to query for a particular persistent class, and they allow for compile-time type checking, which means no casting is necessary. The code below details how to create a `Criteria` and execute a simple query for a domain object `Foo` by one of its fields `mId`.

### Criteria Query Example ###

```
session.open();
Foo foo = session.createCriteria(Foo.class).add(Conditions.eq("mId", 42)).unique();
session.close();
```

## Raw Queries ##

`SqliteSession` also provides API support for executing raw SQL queries. The `execute` method can be used to execute SQL non-queries, i.e. queries that do not return a result.

```
session.execute("delete from foo");
```


## Autocommit- and Transaction- based Sessions ##

An Infinitum `Session` can be configured to be in one of two modes: autocommit or transactional (`RestfulSession` does not support transactions). In either case, `open()` must be invoked before database operations are made and `close()` invoked when a `Session` should be released. The difference between the two lies in how transactions are committed. With autocommit, transactions are committed implicitly as soon as they are executed. A transactional `Session` relies on transactions to handle commits explicitly, meaning a transaction must be opened within a `Session` and then explicitly committed. This method also allows for transactions to be rolled back. An example of both strategies is given below. Note that autocommit can be enabled or disabled in `infinitum.cfg.xml`, but is explicitly set in the examples to better illustrate what's going on.

### Autocommit Example ###

```
session.setAutocommit(true);
session.open();
session.save(new Foo()); // Foo is persisted
session.close();
```

### Transactional Example ###

```
session.setAutocommit(false);
session.open();
session.beginTransaction();
session.save(new Foo());
session.commit(); // Foo is persisted
session.close();
```

To rollback a transaction, `rollback()` is invoked:

```
session.setAutocommit(false);
session.open();
session.beginTransaction();
session.save(new Foo());
session.rollback(); // Foo is NOT persisted
session.close();
```

Transactions can be nested. When the outer transaction is ended all of the work done in that transaction and all of the nested transactions will be committed or rolled back.

# RestfulSession #

`RestfulSession` is another implementation of the `Session` interface, providing an API for communicating with a RESTful web service using domain objects. `RestfulSession` differs from RestfulClient in that it uses domain objects to communicate with a web service and is integrated into the ORM just like `SqliteSession`, while `RestfulClient` must be provided with RESTful URIs and request data. `RestfulSession` is geared towards communicating with a repository web service, and `RestfulClient` is designed to simplify communication with any RESTful service in general, external API or otherwise. As with `SqliteSession`, the `RestfulSession` must be configured in [InfinitumCfgXml](http://code.google.com/p/infinitum-framework/wiki/InfinitumCfgXml#RESTful_Configuration).

Infinitum provides two implementations of `RestfulSession` called `RestfulJsonSession` and `RestfulXmlSession`, which can be extended or re-implemented for specific business needs. In order to remove implementation dependency from code, the `RestfulSession` implementation can be specified in `infinitum.cfg.xml` by referencing a bean which extends `RestfulSession`. This `Session` implementation is then acquired from the `InfinitumContext` as usual.

```
<rest ref="restClient">
...
</rest>

<beans>
    <bean id="restClient" src="com.example.rest.MyRestfulSession" />
</beans>
```

If no implementation is referenced in the configuration, `InfinitumContext` will use the framework's `RestfulJsonSession`.

## Deserializers ##

The `Session` interface exposes the method `registerDeserializer`, which `RestfulSession` makes use of. Deserializers are used to tell the framework how to interpret web service responses. Two implementations of the interface exist, JsonDeserializer and XmlDeserializer.

```
RestfulSession restSession = context.getSession(DataSource.Rest).registerDeserializer(Foo.class, new JsonDeserializer<Foo>() {
    @Override
    public Bar deserializeObject(String json) {
        // deserialization logic
    }
    @Override
    public List<Foo> deserializeObjects(String json) {
        // deserialization logic
    }
});
```