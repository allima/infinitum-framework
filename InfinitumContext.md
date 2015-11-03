**Contents**


# Introduction #

Application-wide context information, which is read from the InfinitumCfgXml file, is stored in a container called `InfinitumContext`. Since the information stored in `InfinitumContext` is initialized from the configuration file, this object should not be instantiated directly. Rather, it should be obtained through the ContextFactory.


# Configuration #

`InfinitumContext` is also configured in `ContextFactory` in addition to being obtained from it. Invoking the `configure` method in `ContextFactory` is critical and must be done before any attempts are made to access the context. The following code snippet illustrates this notion:

```
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    InfinitumContext ctx = ContextFactory.newInstance().getContext(); 
}
```

The above example will throw an `InfinitumConfigurationException` because `configure` has not been called on the `ContextFactory`. The corrected code is as follows:

```
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    ContextFactory.newInstance().configure(this);
    InfinitumContext ctx = ContextFactory.newInstance().getContext();
}
```

`ContextFactory`'s `configure` method takes a `Context` as its argument. This method will implicitly configure Infinitum using the `infinitum.cfg.xml` file placed in the res/raw directory. Because `configure` returns an `InfinitumContext` instance, the above code can be made more concise:

```
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    InfinitumContext ctx = ContextFactory.newInstance().configure(this);
}
```

The XML configuration file used to configure Infinitum can be specified by using the overloaded `configure` method illustrated below:

```
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    InfinitumContext ctx = ContextFactory.newInstance().configure(this, R.raw.myconfig);
}
```

# Using `InfinitumContext` #

`InfinitumContext` can be used to evaluate application-configuration settings at runtime. More important, it is used to acquire a [Session](Session.md), Infinitum's datastore persistence service. The code snippet below shows how a SQLite `Session` is acquired.

```
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    Session session = ContextFactory.newInstance().configure(this).getSession(DataSource.Sqlite);
}
```

`InfinitumContext` also acts as a service locator, allowing configured beans to be retrieved. The code below shows how a `service` bean, configured to use the implementation `MyService`, might be retrieved using `InfinitumContext`.

```
MyService service = context.getBean("service", MyService.class);
```