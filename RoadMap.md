# What's In Progress #

**Unit Testing:** The project began through rapid prototyping, so we're now focusing on establishing a comprehensive test suite to cover all components, old and new. We're utilizing JUnit, [Robolectric](http://pivotal.github.com/robolectric/), and [Mockito](http://code.google.com/p/mockito/) to write unit tests for Android code that can be run on a JVM outside of the Dalvik runtime environment.

**System Testing:** There's a lot of features in Infinitum, so it's a lot of work to make sure everything is working as it should!

**Technical Debt:** There are some tech debt items that have presented themselves in the project, so we're doing our best to improve code quality as much as possible.

**Performance Optimization:** Performance is critical, which is why we're making it a priority to revisit code and squeeze out every bit we can.

**Modular Distribution:** Although we're doing our best to minimize the footprint of the overall framework, we're also working towards a modular distribution that will allow developers to pick and choose, à la carte, the framework modules they want to include in their applications, e.g. ORM, DI, AOP, and HTTP modules. If all you need is a REST client, why include all that other stuff?

**Cache Abstraction:** Transparent caching for methods through annotations.

# What's Planned #

**Paged Lazy Loading:** Collections are currently lazily loaded by loading the entire collection on request, so it's planned to implement paged collections which load and unload collection entities lazily.

# What's Missing? #

Is there something you would like to see added? Email ttreat31@gmail.com with feedback and suggestions or get involved yourself!