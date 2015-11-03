**Contents**


# Introduction #

`JsonDeserializer` provides an API for deserializing JSON into domain model entities. A `JsonDeserializer` is registered with a [RestfulSession](http://code.google.com/p/infinitum-framework/wiki/Session#RestfulSession) for a particular model class, and it requires two methods to be implemented: `deserializeObject` and `deserializeObjects`. Both methods take a JSON string as an argument, while the former returns a single domain object and the latter returns a list of objects. Thus, the JSON that is passed to `deserializeObject` will consist of a single object, and the JSON passed to `deserializeObjects` will consist of a collection of objects (as a JSON array).

`JsonDeserializers` are useful because they tell the framework how to interpret the JSON responses received from a RESTful web service. And since web services can come in many different shapes and sizes (e.g. the JSON formatting used by web service A may differ slightly from that of web service B), it's important to provide a means to accommodate this. Below is a more concrete example of this.

# Example Use Case #

Assume we have two different web services that are based on the same domain model. They have a class `Foo`, which has a field `id` and `val`. Web service A's JSON might look something like this:

```
{"foo":{"id":"328","val":"42"}}
```

And web service B's JSON might look something like this:

```
{"id":"328","val":"42"}
```

Both are valid JSON and both represent the same `Foo` object -- the first just has some additional syntax. Assuming we don't want to (or are unable to) coerce the JSON response from the server side and we don't want to clutter our domain model with wrapper objects, we can utilize a `JsonDeserializer` to parse out the superfluous artifacts from the response.

# Example Implementation #

Carrying on with our use case described above, we would like to implement a `JsonDeserializer` which can interpret JSON formatted by web service A for the class `Foo`. Trivially, we can accomplish this with some simple string manipulation. We will use [Gson](http://code.google.com/p/google-gson/) to convert the formatted JSON into Java objects.

```
public class FooDeserializer extends JsonDeserializer<Foo> {

    @Override
    public Foo deserializeObject(String json) {
        Gson gson = new Gson();
        // {"foo":{"id":"328","val":"42"}} => {"id":"328","val":"42"}
        json = json.substring(json.indexOf('{') + 1, json.lastIndexOf('}') + 1);
        json = json.substring(json.indexOf('{'), json.lastIndexOf('}'));
        return gson.fromJson(json, Foo.class);
    }

    @Override
    public List<Foo> deserializeObjects(String json) {
        List<Foo> ret = new ArrayList<Foo>();
	Gson gson = new Gson();
        // {"foo":{"id":null,"val":null},"fooSet":[{"id":"328","val":"42"},{"id":"329","val":"27"}]} => [{"id":"328","val":"42"},{"id":"329","val":"27"}]
	json = json.substring(json.indexOf(":") + 1);
	json = json.substring(json.indexOf("["));
	json = json.substring(0, json.length() - 1);
	JsonElement jsonElement = new JsonParser().parse(json);
	JsonArray jsonArray = jsonElement.getAsJsonArray();
	for (JsonElement e : jsonArray)
	    ret.add(gson.fromJson(e, Foo.class));
	return ret;
    }

}
```

# Registering a `JsonDeserializer` #

The [Session](Session.md) interface exposes a method `registerDeserializer`, which can register a `JsonDeserializer` for a given class. However, only the `RestfulSession` implementation makes use of deserializers.

```
restSession.registerDeserializer(Foo.class, new FooDeserializer());
```

Once a `JsonDeserializer` has been registered with a `RestfulSession` for a particular class, its serialization methods will be used to interpret responses from the web service. If no deserializer is registered for a class, the framework will attempt to convert responses to objects using the default [Gson](http://code.google.com/p/google-gson/) implementation.