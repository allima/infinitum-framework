**Contents**


# Configuring Build Path #

  1. Download the latest Infinitum distribution from the [Downloads](http://code.google.com/p/infinitum-framework/downloads/list) page.
  1. Extract the JAR files from the zip archive (infinitum-_version_.jar and its dependencies in `lib`)  and place them in your application's `libs` directory. Note that Google Gson is only needed if making use of the [RestfulSession](https://code.google.com/p/infinitum-framework/wiki/Session#RestfulSession).
  1. Verify that the JARs have been added to the application's build path (in Eclipse: right-click on the project > Build Path > Configure Build Path)
Your project can now reference Infinitum Framework APIs.

# Framework Configuration #

Before Infinitum can be used, you must create a configuration file, `infinitum.cfg.xml` and place it in your application's `res/raw` directory. Below is an example template that can be used with minimal configuration. See the InfinitumCfgXml guide for more information on configuring the framework.

```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE infinitum-configuration PUBLIC
"-//Infinitum/Infinitum Configuration DTD 1.0//EN"
"http://clarionmedia.com/infinitum/dtd/infinitum-configuration-1.0.dtd">

<infinitum-configuration>

    <application>
        <property name="debug">true</property>
    </application>
    
    <sqlite>
        <property name="dbName">myDb</property> 
        <property name="dbVersion">1</property>
    </sqlite>
    
    <rest>
        <property name="host">http://localhost/webservice</property>
    </rest>
    
    <domain>
        <model resource="com.example.domain.Foo" />
    </domain>

</infinitum-configuration>
```

The framework must be configured within the application code before any of its classes may be used. If using one of Infinitum's provided Activities, this is taken care of for you. Otherwise, within your `Activity`, call the following code:

```
ContextFactory.newInstance().configure(this);
```

This will configure Infinitum using the `infinitum.cfg.xml` file in `res/raw`. Alternatively, you can specify explicitly which configuration resource to use by doing the following:

```
ContextFactory.newInstance().configure(this, R.raw.infinitum);
```