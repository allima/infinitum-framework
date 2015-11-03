**Contents**


# Introduction #

The `PrimaryKey` annotation indicates if an entity class's field is a primary key, meaning the value is unique to all other entity instances. It has a single attribute, `autoincrement`, which denotes if the primary key should automatically increment when a row is inserted, and it's enabled by default. If the annotation is missing from the class hierarchy, Infinitum will look for a field called `mId` or `id` to use as the primary key. If such a field is found, `autoincrement` will be enabled for it by default if it is of type `int` or `long`. If the primary key is assigned to a field which is not an `int` or `long` and `autoincrement` is enabled, a ModelConfigurationException will be thrown at runtime. Any field marked as a primary key will inherently be marked as persistent, regardless of any [Persistence](Persistence.md) annotation that might be associated with it.

Currently, Infinitum does not support composite or compound keys. More specifically, only _one_ `PrimaryKey` annotation may exist per model class. That said, a compound key can effectively be achieved by designating the "primary key" columns as unique using the [Unique](Unique.md) annotation.

# `PrimaryKey` Example #

As was hinted at earlier, Infinitum will search the entire class hierarchy for a `PrimaryKey`. If class `AbstractBase` has a field `mPk` marked as a primary key and class `Foo` extends `AbstractBase`, `Foo`'s primary key is `mPk`. This notion is illustrated in the example below.

```
public abstract class AbstractBase {

    @PrimaryKey
    protected long mPk;

    public long getPk() {
        return mPk;
    }

    public void setPk(long pk) {
        mPk = pk;
    }

}

public class Foo extends AbstractBase {
    // Foo's primary key is mPk
}
```