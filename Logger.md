**Contents**


# Introduction #

The official Android [development guide](http://developer.android.com/guide/publishing/preparing.html#publishing-configure) advises that logging be disabled when configuring an application for release:

> Make sure you deactivate logging and disable the debugging option before
> you build your application for release. You can deactivate logging by
> removing calls to `Log` methods in your source files. You can disable
> debugging by removing the `android:debuggable` attribute from the
> `<application>` tag in your manifest file, or by setting the
> `android:debuggable` attribute to `false` in your manifest file. Also, remove
> any log files or static test files that were created in your project.

Of course, if your application has a lot of logging calls, removing them or commenting them out could become quite tedious. This is why Infinitum provides a simple logging wrapper that prints log messages to Logcat but adheres to environment configuration. More simply, logging calls can remain in your application's code but be disabled by setting `debug` to `false` in InfinitumCfgXml.

# Acquiring a `Logger` #

A `Logger` instance is acquired using a static factory method on the `Logger` class called `getInstance`, which takes an InfinitumContext and  logging tag as arguments.

```
Logger logger = Logger.getInstance(context, "My Logger");
```