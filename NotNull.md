**Contents**


# Introduction #

The `NotNull` annotation is used to indicate that an entity class's field may not contain a `null` value when being persisted to the database. `NotNull` has no effect on a field that is marked transient.

# `NotNull` Example #

The example below shows how the field `mBar` is marked as not nullable.

```
public class Foo {

    @NotNull
    private String mBar;

    public String getBar() {
        return mBar;
    }

    public void setBar(String bar) {
        mBar = bar;
    }

}
```