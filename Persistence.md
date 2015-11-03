**Contents**


# Introduction #

The `Persistence` annotation indicates the persistence state of an entity class's field. It has a single attribute, `value`, which represents the persistence mode of the field: transient or persistent. By default, a field is persistent, meaning the annotation can be omitted. Its purpose is to support greater model flexibility, allowing for specific class fields to not be persisted.

# `Persistence` Example #

The below example shows how a field can be marked transient such that it will not be persisted.

```
public class Foo {

    @Persistence(PersistenceMode.Transient)
    private int mBar; // this field will NOT be persisted

    private int mBaz; // this field WILL be persisted

}
```