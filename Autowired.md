**Contents**


# Introduction #

The `Autowired` annotation Indicates that the annotated constructor, setter, or field is to be autowired, i.e. injected, by the framework. Only beans have autowired functionality.

The annotation has a single, optional attribute which declares the qualifier indicating the name of the bean to autowire. If this is not defined, Infinitum will inject a candidate bean specified in InfinitumContext. If there is more than one candidate bean (or no candidate bean), an `InfinitumConfigurationException` will be thrown.

If a constructor is marked with this annotation, it will be used to initialize the bean. Only one such constructor can carry this annotation, and it does not have to be public.

# Autowired Example #

The below example illustrates the usage of the `Autowired` annotation for a field, constructor, and setter (note that, normally, you wouldn't inject all of these together unless it's an unusual case).

```
@Bean
public class MyBean {

    @Autowired("myBean")
    private AnotherBean mAnotherBean;
    private SomeBean mSomeBean;
    private boolean mSomeBool;
    private FooBean mFooBean;

    @Autowired
    public MyBean(SomeBean someBean, boolean someBool) {
        mSomeBean = someBean;
        mSomeBool = someBool;
    }

    @Autowired
    public void setFooBean(FooBean fooBean) {
        mFooBean = fooBean;
    }

}
```