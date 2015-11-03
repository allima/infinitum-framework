**Contents**


# Introduction #

`RestfulClient` is an interface which provides facilities to communicate with RESTful web services. `RestfulClient` differs from [RestfulSession](http://code.google.com/p/infinitum-framework/wiki/Session#RestfulSession) in that it must be provided with RESTful endpoint URIs and request data, whereas the latter is integrated into the ORM and abstracts this information out so that a web service can be consumed using domain objects. While `RestfulSession` is geared towards communicating with a repository web service, `RestfulClient` is designed to simplify communication with _any_ RESTful service, external API or otherwise.

# Using a `RestfulClient` #

Infinitum provides an implementation of `RestfulClient` called `BasicRestfulClient`. Unlike `RestfulSession`, there is no configuration needed to use `RestfulClient` -- simply instantiate it and begin making web requests!

```
RestfulClient rest = new BasicRestfulClient();

// HTTP GET request
RestResponse response = rest.executeGet("http://localhost/mywebservice/foo/42");

// HTTP DELETE request
response = rest.executeDelete("http://localhost/mywebservice/foo/42");

// HTTP POST request
String someJson = "{\"id\":\"328\",\"val\":\"42\"}";
response = rest.executePost("http://localhost/mywebservice/foo", someJson, "application/json");

// HTTP PUT request
someJson = "{\"id\":\"328\",\"val\":\"76\"}";
response = rest.executePut("http://localhost/mywebservice/foo", someJson, "application/json");

```

We can also apply headers to our requests:

```
Map<String, String> headers = new HashMap<String, String>();
headers.put("Accept", "text/xml");
response = rest.executeGet("http://localhost/mywebservice/foo/42", headers);
```

Additionally, connection and response timeouts can be configured for the `RestfulClient`:

```
rest.setConnectionTimeout(5000);
rest.setResponseTimeout(5000);
```