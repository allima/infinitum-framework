**Contents**


# Introduction #

The `Entity` annotation is used to indicate the persistence state of a model class. The annotation can be omitted and a model class will be persistent by default, but if properties need to be explicitly defined, then it should be applied.

Persistent models must include an empty constructor in order for the Infinitum ORM to work. For example:

```
@Entity
public class Foobar {
    // ...
    public Foobar() {
    }
    // ...
}
```

# Annotation Attributes #

`Entity` has a number of attributes which are used to specify various entity properties.

**mode:** indicates the entity's persistence mode, persistent or transient. By default, this attribute is set to persistent.

**cascade:** indicates cascade mode for the entity, `all`, `none`, or `keys`. `All` means when the entity's state is changed in the database, all entities related to it will also be updated or persisted. `None` means no related entities will be updated or persisted, and `Keys` means only foreign keys will be updated or persisted. The `all` cascade mode is enabled by default.

**lazy:** indicates if the entity has lazy loading enabled. If it is, related entities will be loaded dynamically when accessed. Lazy loading is enabled by default.

**endpoint**: indicates the REST endpoint name for this entity, i.e. in mywebservice.com/**foobar**, foobar would be the endpoint name. If the endpoint name is not specified, the lowercase form of the class name will be used.

# Entity Example #

It's important to point out that the `Entity` annotation is optional. If it is not specified for a model class, the default attribute values, described above, will be used. However, in the event that you need to specify a different endpoint name or disable cascading or lazy loading, the `Entity` annotation will need to be defined with these values. Below is an example model class which uses the `Entity` annotation to disable cascading and lazy loading. Notice that since the `endpoint` attribute is not present, the REST endpoint associated with this class will be "foo".

```
@Entity(cascade=Cascade.None, lazy=false)
public class Foo {
    // ...
}
```