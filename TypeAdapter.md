**Contents**


# Introduction #

`TypeAdapter`s allow the Infinitum ORM to support unknown data types by, in essence, telling the framework how to map them. `TypeAdapter` is an interface that provides a method `mapToObject(ResultSet result, int index, Field field, Object model)` which is used to map a column value to a domain object.

The ResultSet encapsulates a SQL query result set, while `index` represents the column index in the `ResultSet` in which the value to map resides. The `Field` is the class field to set in the object. This allows for the value to be set by reflection, meaning the object type does not need to be known at compile time. Lastly, `model` is the domain object for which the `Field` is being set for.

# SqliteTypeAdapter #

`SqliteTypeAdapter` is an abstract implementation of `TypeAdapter` which facilitates the mapping of Java data types to columns in a SQLite database and vice versa. This class has two methods which must be implemented in addition to `TypeAdapter`s `mapToObject` method. The first is `mapToColumn(T value, String column, ContentValues values)`. This method makes use of the class's generic parameter which represents the type being mapped; that is, `value` is the object being mapped to a column. The second parameter, `column`, is the name of the column which `value` is mapped to. It's used as a key for putting the value in the third parameter, `values`, which is a `ContentValues` object storing the object-table mapping data. Below is an example implementation of this method for a `SQLiteTypeAdapter` which maps [JodaTime](http://joda-time.sourceforge.net/) `DateTime` objects in a rather trivial manner.

```
@Override
public void mapToColumn(DateTime value, String column, ContentValues values) {
    String dateStr = value.getYear() + "-" + value.getMonthOfYear() + "-" + value.getDayOfMonth();
    values.put(column, dateStr);
}
```

The second method that must be implemented is similar to the first: `mapObjectToColumn(Object value, String column, ContentValues values)`. It does not use the generic parameter and is needed for internal compatibility reasons. Behaviorally, it is identical to `mapToColumn`. The only difference is that the object `value` must be casted to the data type being mapped. However, **it is important to note that this cast _is safe_**, meaning `value` is guaranteed to be of the same type as the generic parameter provided to the class. Continuing on with the `DateTime` adapter example, an implementation of `mapObjectToColumn` is seen below.

```
@Override
public void mapObjectToColumn(Object value, String column, ContentValues values) {
    DateTime dt = (DateTime) value; // guaranteed safe cast
    String dateStr = dt.getYear() + "-" + dt.getMonthOfYear() + "-" + dt.getDayOfMonth();
    values.put(column, dateStr);
}
```

`SqliteTypeAdapter`'s constructor takes a single argument, a `SqliteDataType`, which is either NULL, INTEGER, REAL, TEXT, or BLOB. This is the SQLite type in which the object will be stored in the database as.

## Example Implementation ##

Below is a complete implementation for the `DateTime` `SqliteTypeAdapter`.

```
// Store DateTimes as strings
TypeAdapter<DateTime> adapter = new SqliteTypeAdapter<DateTime>(SqliteDataType.TEXT) {
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
};
```

# RestfulPairsTypeAdapter #

Infinitum's RESTful ORM provides several abstract `TypeAdapter` implementations, the first of which is called `RestfulPairsTypeAdapter`. It allows for the mapping of Java data types to model fields in a RESTful web service through name-value pairs. Its implementation is very similar to that of `SqliteTypeAdapter`'s, but there is one key difference. `RestfulPairTypeAdapter`'s two methods are `mapToPair(T value, String field, List<NameValuePair> pairs)` and `mapObjectToPair(Object value, String field, List<NameValuePair> pairs)`. The first, `mapToPair`, is the analog to `SqliteTypeAdapter`'s `mapToColumn`, where `value` is the object being mapped. The second parameter, `field`, is the name of the field in the web service the object maps to, i.e. the _name_ in the name-value pair. The last parameter, `pairs`, contains all of the name-value pairs for the model, which are used in HTTP POST/PUT requests made to the web service. Below is an example of this method.

```
@Override
public void mapToPair(DateTime value, String field, List<NameValuePair> pairs) {
    String dateStr = value.getYear() + "-" + value.getMonthOfYear() + "-" + value.getDayOfMonth();
    pairs.add(new BasicNameValuePair(field, dateStr));
}
```

The second method, `mapObjectToPair`, is the analog to `SqliteTypeAdapter`'s `mapObjectToColumn`. Following suit, it is identical to `mapToPair` except for the first parameter. Once again, this object can be safely casted to the specified generic type.

```
@Override
public void mapObjectToPair(Object value, String field, List<NameValuePair> pairs) {
    DateTime dt = (DateTime) value; // guaranteed safe cast
    String dateStr = dt.getYear() + "-" + dt.getMonthOfYear() + "-" + dt.getDayOfMonth();
    pairs.add(new BasicNameValuePair(field, dateStr));
}
```

Unlike `SqliteTypeAdapter`, `RestfulPairsTypeAdapter`'s constructor takes no arguments.

## Example Implementation ##

Below is a complete implementation for the `DateTime` `RestfulPairsTypeAdapter`.

```
// Store DateTimes as strings
TypeAdapter<DateTime> adapter = new RestfulPairsTypeAdapter<DateTime>() {
    @Override
    public void mapToObject(ResultSet result, int index, Field field, Object model) throws IllegalArgumentException, IllegalAccessException {
        field.set(model, result.getString(index));
    }
    @Override
    public void mapToPair(DateTime value, String field, List<NameValuePair> pairs) {
        String dateStr = value.getYear() + "-" + value.getMonthOfYear() + "-" + value.getDayOfMonth();
        pairs.add(new BasicNameValuePair(field, dateStr));
    }
    @Override
    public void mapObjectToPair(Object value, String field, List<NameValuePair> pairs) {
        DateTime dt = (DateTime) value;
        String dateStr = dt.getYear() + "-" + dt.getMonthOfYear() + "-" + dt.getDayOfMonth();
        pairs.add(new BasicNameValuePair(field, dateStr));
    }
};
```

# RestfulJsonTypeAdapter #

TODO

# RestfulXmlTypeAdapter #

TODO

# Registering TypeAdapters #

A `TypeAdapter` is registered with a [Session](Session.md) for a certain class, allowing fields of that class to be mapped to a database table or web service model.  Registering a `TypeAdapter` for a class which already has a `TypeAdapter` registered for it will result in the previous `TypeAdapter` being overridden. Continuing again with the `TypeAdapter` example for `DateTime`, an example of creating and registering a `SqliteTypeAdapter` with a `Session` is given below.

```
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