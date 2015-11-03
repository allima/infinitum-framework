**Contents**


# Introduction #

The `Rest` annotation is used to indicate the name of a endpoint field an entity class's field is mapped to for a RESTful web service. To put it more plainly, this is the name of the field in a web service form that is used in HTTP POST requests. It has a single attribute, value, which represents the form field name. If the annotation is not provided, the field is mapped to a endpoint field with the same name as itself.

# `Rest` Example #

The example below shows how the field `mBar` is mapped to the endpoint field named `my_field`.

```
public class Foo {

    @Rest("my_field")
    private int mBar;

}
```