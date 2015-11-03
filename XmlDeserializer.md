**Contents**


# Introduction #

`XmlDeserializer` provides methods for transforming web service responses sent in XML into domain model entities. Like JsonDeserializer, an `XmlDeserializer` is registered with a [RestfulSession](http://code.google.com/p/infinitum-framework/wiki/Session#RestfulSession) for a particular model class, and it requires two methods to be implemented: `deserializeObject` and `deserializeObjects`. These methods are responsible for interpreting XML object representations and building Java objects from them.

# Example Use Case #

Suppose a web service sends responses back as XML when requesting `Foo` models:

```
<foos>
    <foo>
        <id>328</id>
        <val>42</val>
    </foo>
    <foo>
        <id>329</id>
        <val>27</val>
    </foo>
</foos>
```

`XmlDeserializer` tells the framework how to interpret this response as a collection of `Foo` objects.

```
public class FooDeserializer extends XmlDeserializer<Foo> {

    @Override
    public Foo deserializeObject(String xml) {
        // XML parsing logic
    }

    @Override
    public List<Foo> deserializeObjects(String xml) {
        // XML parsing logic
    }

}
```

# Registering an `XmlDeserializer` #

The [Session](Session.md) interface exposes a method `registerDeserializer`, which can register an `XmlDeserializer` for a given class. However, only the `RestfulSession` implementation makes use of deserializers.

```
restSession.registerDeserializer(Foo.class, new FooDeserializer());
```

RestfulClientBuilder also has this method, allowing a `RestfulClient` to be fully configured before it's constructed.

```
RestfulClient rest = new RestfulClientFactory().registerDeserializer(Foo.class, new FooDeserializer()).build();
```

Once an `XmlDeserializer` has been registered with a `RestfulSession` for a particular class, its serialization methods will be used to interpret responses from the web service. If no deserializer is registered for a class, the framework will attempt to convert responses to objects using the default [Simple Framework](http://simple.sourceforge.net/) implementation.