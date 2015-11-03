**Contents**


# Introduction #

Although Infinitum fosters the convention-over-configuration principle, it _does_ require a configuration file, called `infinitum.cfg.xml`, which contains properties that can otherwise not be assumed by the framework. That said, many of the configuration properties in this file are, in fact, optional and _can_ be resolved by the framework, but there are a few which must be provided.

# Application Configuration #

Infinitum has two optional application-wide properties which can be configured: debug and object-relational mapping mode.

Enabling debug will prompt Infinitum to print log messages using Logcat. It's recommended that this be disabled for production environments. If this property is omitted, debug will be disabled by default.

The object-relational mapping mode indicates how the Infinitum ORM will be configured. Domain objects require some metadata for database mapping, which can come in the form of annotations or XML map files. By default, annotations are assumed.

## Application Configuration Example ##

```
<application>
    <property name="debug">true</property> <!-- [true | false] -->
    <property name="mode">annotations</property> <!-- [annotations | xml] -->
</application>
```

# Domain Configuration #

In order to make use of Infinitum's ORM or RESTful client, an application's domain model classes must be declared within `infinitum.cfg.xml`. A model class is declared simply by identifying its package-qualified name.

## Domain Configuration Example ##

```
<domain>
    <model resource="com.clarionmedia.infinitumapp.domain.Foo" />
    <model resource="com.clarionmedia.infinitumapp.domain.Bar" />
    <model resource="com.clarionmedia.infinitumapp.domain.Baz" />
</domain>
```

# SQLite Configuration #

If SQLite is being used as a datastore, a database name and version number _must_ be provided. Optionally, a property indicating if the framework should create the database schema automatically and a property for indicating if autocommit is enabled can be provided as well. Both of these are enabled by default.

## SQLite Configuration Example ##

```
<sqlite>
    <property name="dbName">myDatabase</property> 
    <property name="dbVersion">2</property>
    <property name="generateSchema">true</property> <!-- [true | false] -->
    <property name="autocommit">false</property> <!-- [true | false] -->
</sqlite>
```

# RESTful Configuration #

If `RestfulSession` is being utilized, it _must_ be configured, at least minimally, in `infinitum.cfg.xml`. The only required property is the host, which designates the URL of the web service. Optional properties include connection timeout, response timeout, and authentication.

Connection and response timeouts indicate the time, in milliseconds, the client will wait for a connection or response, respectively, before failing.

If a web service uses some type of authentication, an authentication strategy can be registered. For example, if using a shared secret, a "token" strategy can be declared, indicating the name of the shared secret and the token itself (a token generator can also be used if an unchanging token is undesirable).

Authentication is included in the request URI's query string, but it can be included as a header instead by enabling the `header` property.

## RESTful Configuration Example ##

```
<rest>
    <property name="host">http://localhost/mywebservice</property>
    <property name="connectionTimeout">5000</property>
    <property name="responseTimeout">5000</property>
    <authentication strategy="token" header="true">
        <property name="tokenName">token</property>
        <property name="token">52b353fb27267973cebb5e8566e0415d</property>
    </authentication>
</rest>
```

By default, the `getSession` method in InfinitumContext will use `RestfulJsonSession` as its RESTful client implementation. However, alternative implementations can be used by providing a bean, which will then be injected into the `InfinitumContext`. In a similar manner, alternative AuthenticationStrategy implementations can be used by providing a bean also. Both of these notions are illustrated in the example below.

```
<rest ref="restClient">
    <property name="host">http://localhost/mywebservice</property>
    <property name="connectionTimeout">5000</property>
    <property name="responseTimeout">5000</property>
    <authentication ref="customAuthenticator" enabled="true" />
</rest>
```

The bean `restClient` will be used as the `RestfulSession`, while the `customAuthenticator` bean will be used as the client's `AuthenticationStrategy`. These beans would be defined as follows:

```
<bean id="restClient"
    src="com.clarionmedia.infinitumapp.service.MyRestfulClient" />
<bean id="customAuthenticator"
    src="com.clarionmedia.infinitumapp.service.MyAuthenticator" />
```

`MyRestfulClient` must extend the `RestfulSession` abstract class, and `MyAuthenticator` must implement the `AuthenticationStrategy` interface.

# Bean Configuration #

Infinitum supports the notion of beans for injecting custom implementations for some of its different services at runtime, as was outlined briefly in the RESTful Configuration section above, as well as application components. For example, implementations of `RestfulSession` and `AuthenticationStrategy` can be provided.

Beans are defined in `infinitum.cfg.xml`. Beans themselves can be injected with basic properties, such as `int`, `long`, `double`, `float`, and `String` fields, using the `value` attribute or with other beans using the `ref` attribute.

Additionally, beans can be scanned by the framework without the need to register them in XML by enabling the `component-scan` feature.

```
<component-scan base-package="com.clarionmedia.infinitumapp" />
```

This will allow Infinitum to detect bean classes annotated with the [Bean](Bean.md) annotation located in the `base-package` and any of its sub-packages.

## Bean Configuration Example ##

Take, for example, an `AuthenticationStrategy` implementation called `MyTokenAuthenticator`, which has two fields, `token` and `tokenName`, both of type `String`. Let's say we want to use this authenticator in our `RestfulSession`. It's a rather contrived example, considering SharedSecretAuthentication provides identical functionality, but it illustrates the use of beans and bean properties. First, we must define the bean:

```
<beans>
    <bean id="tokenAuthenticator" src="com.clarionmedia.infinitumapp.service.MyTokenAuthenticator">
        <property name="tokenName" value="myToken" />
        <property name="token" value="e489e8383c0ae2b7" />
    </bean>
</beans>
```

The properties `tokenName` and `token` will be injected into the `MyTokenAuthenticator` bean. Now, with the bean configured, we can reference it in our RESTful configuration using the `ref` attribute.

```
<rest>
    <property name="host">http://localhost/mywebservice</property>
    <authentication ref="tokenAuthenticator" />
</rest>
```

Our `RestfulSession` will now be injected with `MyTokenAuthenticator`.

# Putting It All Together #

All of the components discussed above are used to compose the `infinitum.cfg.xml` configuration file. An example of one such configuration file is seen below.

```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE infinitum-configuration PUBLIC
"-//Infinitum/Infinitum Configuration DTD 1.0//EN"
"http://clarionmedia.com/infinitum/dtd/infinitum-configuration-1.0.dtd">

<infinitum-configuration>

    <application>
        <property name="debug">true</property> <!-- [true | false] -->
        <property name="mode">annotations</property> <!-- [annotations | xml] -->
    </application>

    <domain>
        <model resource="com.clarionmedia.infinitumapp.domain.Foo" />
        <model resource="com.clarionmedia.infinitumapp.domain.Bar" />
        <model resource="com.clarionmedia.infinitumapp.domain.Baz" />
    </domain>
    
    <sqlite>
        <property name="dbName">myDatabase</property> 
        <property name="dbVersion">2</property>
        <property name="generateSchema">true</property> <!-- [true | false] -->
        <property name="autocommit">false</property> <!-- [true | false] -->
    </sqlite>
    
    <rest ref="restClient">
        <property name="host">http://localhost/mywebservice</property>
        <property name="connectionTimeout">5000</property>
        <property name="responseTimeout">5000</property>
        <authentication ref="authenticator" />
    </rest>

    <beans>
        <component-scan base-package="com.clarionmedia.infinitumapp" />
        <bean id="restClient" src="com.clarionmedia.infinitumapp.service.MyRestfulClient" />
        <bean id="authenticator" src="com.clarionmedia.infinitumapp.service.MyAuthenticator" />
    </beans>

</infinitum-configuration>
```