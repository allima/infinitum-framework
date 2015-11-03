**Contents**


# Introduction #

The `BeanPostProcessor` allows for beans to be modified after they have been initialized by the container. `BeanPostProcessor` exposes a single method, `postProcessBean(BeanFactory beanFactory, String beanName, Object bean)`. This method is invoked on every registered bean for all registered `BeanPostProcessors` after the beans have been initialized and immediately before any BeanFactoryPostProcessor implementations have been called. `BeanPostProcessors` are useful for any situations where a particular bean or group of beans need to be altered or replaced based on certain conditions.

# Example Implementation #

Infinitum uses a `BeanPostProcessor` to resolve [Autowired](Autowired.md) dependencies in beans. Below is a slightly simplified fragment of that implementation with code excluded for brevity. Since `BeanPostProcessors` are special types of beans, they are registered with an InfinitumContext as normal.

```
@Component
public class AutowiredBeanPostProcessor implements BeanPostProcessor {

    @Override
    public void postProcessBean(BeanFactory beanFactory, String beanName, Object bean) {
        injectFields(beanFactory, bean);
	injectSetters(beanFactory, bean);
    }

    // ...

}
```

If we are not using component scanning, we can register this post processor in the InfinitumCfgXml:

```
<bean id="autowiredBeanPostProcessor"
    src="com.example.postprocessor.AutowiredBeanPostProcessor" />
```