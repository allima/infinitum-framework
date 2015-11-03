**Contents**


# Introduction #

`ContextFactory` is used to configure Infinitum and to retrieve an instance of InfinitumContext. Before a context can be retrieved, `ContextFactory`'s `configure` method must be invoked by passing in a `Context` and a resource ID for InfinitumCfgXml. If a call is made to get an `InfinitumContext` before `configure` has been invoked, an `InfinitumConfigurationException` will be thrown.

`ContextFactory` is not instantiated directly but rather retrieved by invoking its static method `newInstance()`.

# Acquiring an `InfinitumContext` #

The following code snippet illustrates how Infinitum is configured and how an instance of `InfinitumContext` is retrieved.

```
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    InfinitumContext ctx = ContextFactory.newInstance().configure(this, R.raw.infinitum);
}
```

The ID for the XML configuration does not need to be specified if using the standard naming convention, `infinitum.cfg.xml`:

```
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    InfinitumContext ctx = ContextFactory.newInstance().configure(this);
}
```