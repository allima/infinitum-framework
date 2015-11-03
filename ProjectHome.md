

# We're moving to [GitHub](https://github.com/InfinitumFramework)! #

**Infinitum** is an extensible, robust framework enabling Android developers to quickly and efficiently create rich, domain-driven applications while facilitating the convention-over-configuration paradigm. One of its primary goals is to foster a strong separation of concerns, empowering developers to write modular, cohesive, and business-focused software for the Android platform. The idea is to maintain focus on the fundamental business problem of the software, not the underlying plumbing that connects it.

Infinitum includes an **object-relational mapper** that allows developers to spend more time focusing on their problem domain and core business logic and less time on innate data-access and boilerplate code. It embraces object-oriented principles such as polymorphism, inheritance, and association while maintaining a great deal of flexibility. The ORM allows developers to specify what is transient or persistent at a class- and field-level, and it is configurable using either XML mappings or Java annotations. The Infinitum ORM also provides a criteria API for constructing database queries, allowing developers to query on objects rather than tables -- no SQL necessary.

The framework offers an **extensible REST client**, granting developers an effortless way to communicate with their own RESTful web services using domain objects and consume external REST APIs with ease.

In addition to its ORM and RESTful client, Infinitum provides an extremely **lightweight logging framework** which wraps Android's Logcat. This logging framework allows log statements to be made within application code but only asserted in debug environments. This means that logging code does not need any conditional statements or be removed altogether when preparing an application for release.

In order to separate cross-cutting concerns, Infinitum implements an **aspect-oriented programming framework**. With it, developers can alter or extend the behavior of core application code by creating aspects, which are used to apply advice at specific join points.

All of these components are built on top of a framework context, acting as an **inversion-of-control container** which allows for framework and non-framework beans to be injected and retrieved at runtime. Beans, aspects, and other application components can be automatically discovered by Infinitum, and the framework also provides support for autowiring properties, methods, and constructors. Likewise, Android activities can be injected with layouts, views, and resources -- yielding cleaner, more concise code.

Infinitum is currently in a pre-release form and is still under active development. See the [road map](https://code.google.com/p/infinitum-framework/wiki/RoadMap) for what's in store for Infinitum.

## Core DI Features ##
  * Dependency injection: singleton and prototype beans can be declared for both framework and non-framework components in XML or through annotations and be injected at runtime
  * Autowiring: bean properties, methods, and constructors can be autowired to allow Infinitum to resolve dependencies at runtime
  * Resource injection: inject Activities with Android resources, layouts, and views
  * Event binding: event listeners can be bound to views using simple annotations
  * Component scanning: the framework can be configured to automatically discover components such as beans and aspects, which can then be injected or retrieved at runtime

## Core ORM Features ##
  * Non-intrusive: no need to implement or extend any classes
  * Automated DDL generation: Infinitum can be configured to automatically create a schema with the necessary tables based on entity classes and relationships
  * XML- and annotation- based entity mappings: domain entity classes can be mapped to database tables using either XML map files or Java annotations
  * Transparent persistence for Plain Old Java Objects (POJOs)
  * Dynamic SQL generation
  * Session API: the ORM provides a session interface, which acts as the persistence layer for your application
  * Session caching: persistent entities are attached to a session through a session cache, allowing for reduced datastore calls, speedier retrieval, and enforced referential integrity
  * Transactional: sessions can be configured to autocommit or transactions can be explicitly committed or rolled back
  * Criteria API: build queries using criterion and get domain objects back when you execute them
  * Entity associations: specify associations (one-to-many, many-to-one, one-to-one, many-to-many) and let Infinitum handle populating relationships
  * Lazy- and eager- loading: Infinitum can be configured to lazily or eagerly load associated collections on-the-fly
  * Entity cascading: persistent domain entities can be configured to cascade at the class level, meaning persistent objects associated with an entity being saved will also be saved
  * Custom type adapters: register custom type adapters to allow Infinitum's ORM to map any type
  * Datastore-agnostic: make calls to SQLite databases or RESTful web services without distinction

## Core RESTful Client Features ##
  * Abstracted RESTful session interface: provides an API for basic web service CRUD operations using domain objects
  * RESTful Client: allows easy consumption of any RESTful API
  * Fully extensible: RESTful client can be easily extended or re-implemented for specific business needs
  * Configurable: register JSON deserializers and type adapters to accommodate any RESTful web service implementation
  * Authentication support: enable token-based/shared-secret web service authentication or implement your own authentication strategy
  * Token generation: provides support for custom token generation for situations where a changing, per-user, or per-session shared secret is desirable

## Core AOP Features ##

  * Aspects: create aspects which specify advice and join points to alter or extend application code while separating cross-cutting concerns
  * Before, around, and after: Infinitum supports three types of advice, _before_, which executes before a join point, _after_, which executes after a join point, and _around_, which can execute both before and after a join point
  * Pointcuts: aspects can define pointcuts, which specify where advice should be applied

## Core Logging Features ##
  * Leverages Logcat: log statements are still made through Android's Logcat utility
  * Simple API: because it utilizes Logcat, the logging interface is already familiar
  * Environment-aware: messages are asserted only when an application is configured for debug mode -- make logging calls without worrying about environments

## General Features ##
  * Convention over configuration: Infinitum requires minimal configuration, reducing the number of decisions developers need to make, while maintaining flexibility
  * Testability: framework components are coded to interfaces, allowing these application dependencies to be mocked in unit tests

## Start Using Infinitum ##

  * [Beta distribution](http://code.google.com/p/infinitum-framework/downloads/detail?name=infinitum-beta-1.1.zip): framework JAR and its dependencies
  * [Javadoc](https://infinitum-framework.googlecode.com/svn/trunk/InfinitumFramework/doc/index.html): API documentation
  * UserGuide: provides explanation for framework features and how to use them
  * InstallationGuide: complete guide to setting up Infinitum
  * ExampleApplication: a simple app that demonstrates the basics of Infinitum

Comments, questions, concerns, and other general feedback can be sent to [ttreat31@gmail.com](mailto:ttreat31@gmail.com).