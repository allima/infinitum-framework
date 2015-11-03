**Contents**


# Introduction #

`SharedSecretAuthentication` is an AuthenticationStrategy implementation used for token-based/shared-secret web service authentication. Shared-secret authentication consists of a token name and a token (shared secret) in the form of `tokenName=myToken`. Thus, this authentication string is typically appended to the URL of a web service request as a query-string parameter, such as `http://localhost/webservice/endpoint?token=5f4dcc3b5aa765d61d8327deb882cf99`, or as a request header.

# Configuring Token Authentication #

Both the token name and the token itself can be provided to `SharedSecretAuthentication` from `infinitum.cfg.xml` in the [RESTful configuration](InfinitumCfgXml#RESTful_Configuration.md). Otherwise, a TokenGenerator can be supplied to `SharedSecretAuthentication` by injecting a `TokenGenerator` bean into it. Both of these methods are illustrated in the examples below.

## Explicit Token Values ##

If an unchanging shared secret is sufficient for your web service, the token values can be specified in the `authentication` element of `infinitum.cfg.xml`.

```
<rest>
    <property name="host">http://localhost/mywebservice</property>
    <authentication strategy="token" enabled="true">
        <property name="tokenName">token</property>
        <property name="token">5f4dcc3b5aa765d61d8327deb882cf99</property>
    </authentication>
</rest>
```

## Token Generation ##

If a changing, per-user, or per-session shared secret is desirable, a `TokenGenerator` can be supplied, allowing for shared secrets to be generated based on a predefined method, perhaps making use of a session or user GUID. This must be done through bean wiring.

```
<rest>
    <property name="host">http://localhost/mywebservice</property>
    <authentication ref="tokenAuthentication" enabled="true" /> 
</rest>

<beans>
    <bean id="tokenGenerator" src="com.example.rest.MyTokenGenerator" />
    <bean id="tokenAuthentication" src="com.clarionmedia.infinitum.http.rest.impl.SharedSecretAuthentication">
        <property name="mTokenName" value="token" />
        <property name="mGenerator" ref="tokenGenerator" />
    </bean>
</beans>
```

The above example configures the `RestfulSession` to use `SharedSecretAuthentication`, which itself uses `MyTokenGenerator` to retrieve shared secrets.