**Contents**


# Introduction #

`SqliteSession` is an implementation of the [Session](Session.md) interface for interacting with a SQLite database. Among other things, it provides support for transactions, autocommit, and [Criteria](Criteria.md) queries.

# Criteria Queries #

Further supporting the notion of persistence transparency, `SqliteSession` provides supoort for a Criteria API which is used to construct object-oriented database queries, removing the need for SQL. `Criteria` queries are used to query for a particular persistent class, and they allow for compile-time type checking, which means no casting is necessary. The code below details how to create a `Criteria` and execute a simple query for a domain object `Foo` by one of its fields `mId`.

## Criteria Query Example ##

```
session.open();
Foo foo = session.createCriteria(Foo.class).add(Conditions.eq("mId", 42)).unique();
session.close();
```

# Raw Queries #

`SqliteSession` also provides API support for executing raw SQL queries. The `execute` method can be used to execute SQL non-queries, i.e. queries that do not return a result.

```
session.execute("delete from foo");
```