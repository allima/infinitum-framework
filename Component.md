**Contents**


# Introduction #

The `Component` annotation is used to indicate that a class is a framework component, meaning it is a candidate for auto-detection by the framework if classpath scanning is enabled.

The annotation has a single, optional attribute which indicates the name of the component. If it is not specified, the component takes the class name in camelcase form.

`Component`s include mechanisms such a post processors, e.g, `BeanFactoryPostProcessors` and `BeanPostProcessors`.

If looking to inject modules, use the [Bean](Bean.md) annotation since beans, which are specialized components, are intended to resolve autowire dependencies. Components simply register a module with the framework context.

# Component Example #

The below example illustrates an implementation of a BeanFactoryPostProcessor. The `Component` annotation will allow the post processor to be picked up and registered automatically by the framework.

```
@Component
public class BuildTargetedPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(BeanFactory beanFactory) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            // Configure beans for newer APIs
        } else {
            // Configure beans for older APIs
        }
    }

}
```