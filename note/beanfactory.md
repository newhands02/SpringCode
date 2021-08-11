1.ConfigurableBeanFactory

```java
/** * Configuration interface to be implemented by most bean factories. Provides facilities to configure a bean factory, in addition to the bean factory client methods in the BeanFactory interface. *被大多数bean工厂实现的配置接口 
* 在这个接口中定义了添加beanPostProcessor方法和获取Bean定义信息方法**/
```

在这个接口中定义了添加beanPostProcessor方法和获取Bean定义信息方法

```java
/** * Add a new BeanPostProcessor that will get applied to beans created * by this factory. To be invoked during factory configuration. *添加一个后置处理器。其会被应用到工厂创建了beans之后。这个方法在配置工厂期间执行 * 
```

2.AutowireCapableBeabFactory

```java
/** * Extension of the {BeanFactory} interface to be implemented by bean factories that are capable of autowiring, provided that they want to expose this functionality for existing bean instances. *BeanFactory接口的拓展，能够自动注入的bean工厂实现，暴露一些功能给存在的Bean实例 * This subinterface of BeanFactory is not meant to be used in normal * application code: stick to {BeanFactory} or {ListableBeanFactory} for *typical use cases. * * Integration code for other frameworks can leverage this interface to wire and populate existing bean instances that Spring does not controlthe lifecycle of. This is particularly useful for WebWork Actions andTapestry Page objects, for example.其他框架的集成代码可以通过这个接口填充存在的不受Spring生命周期控制的bean实例 * * Note that this interface is not implemented by {ApplicationContext} facades, as it is hardly ever used by application code. That said, it is available from an application context too, accessible through ApplicationContext's {ApplicationContext#getAutowireCapableBeanFactory()}method. *这个接口没有被application相关类实现，因为其很难直接用在application中的代码，但是他也能够被应用上下文访问通过applicationContext的getAutowireCapableBeanFactory()方法 * * /
```

这个接口包含的是一些创建Bean相关的方法

```java
/** * Fully create a new bean instance of the given class. * Performs full initialization of the bean, including all applicable {BeanPostProcessor BeanPostProcessors}. *根据给定的class创建完全的bean实例。执行完全的bean初始化过程，包括执行所有匹配的bean后置处理器 * Note: This is intended for creating a fresh instance, populating annotated fields and methods as well as applying all standard bean initialization callbacks. *这个方法意图创建一个全新的bean对象，填充标注的属性和方法，也应用所有标准bean的初始化回调方法 * It does not imply traditional by-name or by-type autowiring of properties;use {@link #createBean(Class, int, boolean)} for those purposes. 如果要通过传统的by名字或者by类型来注入属性，使用createBean(Class, int, boolean)方法 */ T createBean(Class beanClass) throws BeansException;/** * Populate the given bean instance through applying after-instantiation callbacks and bean property post-processing (e.g. for annotation-driven injection). 填充指定的bean实例通过使用实例化后回调方法和bean属性后置处理（如使用注解注入） * */
/**
* Initialize the given raw bean, applying factory callbacks such as {@code setBeanName} and {@code setBeanFactory},also applying all bean post processors (including ones which might wrap the given raw bean).
初始化指定的原生bean，使用诸如setBean和setBeanFactory方法，也包括使用所有的Bean后置处理器（包括一些可能对bean进行包装的）
* <p>Note that no bean definition of the given name has to exist in the bean factory. The passed-in bean name will simply be used for callbacks but not checked against the registered bean definitions.
*bean工厂中不需要存在任何给定bean的定义信息，传入的bean名字只会用于回调到不会检查定义信息是否已注册
* @param existingBean the existing bean instance
* @param beanName the name of the bean, to be passed to it if necessary
* (only passed to {@link BeanPostProcessor BeanPostProcessors};
* can follow the {@link #ORIGINAL_INSTANCE_SUFFIX} convention in order to
* enforce the given instance to be returned, i.e. no proxies etc)
* @return the bean instance to use, either the original or a wrapped one
* @throws BeansException if the initialization failed
* @see #ORIGINAL_INSTANCE_SUFFIX
*/
Object initializeBean(Object existingBean, String beanName) throws BeansException;
/**
* Destroy the given bean instance (typically coming from {@link #createBean}),applying the {DisposableBean} contract as well as registered {@link DestructionAwareBeanPostProcessor DestructionAwareBeanPostProcessors}.
*销毁给定的bean，尤其是createBean方法创建的bean
* <p>Any exception that arises during destruction should be caught
* and logged instead of propagated to the caller of this method.
* @param existingBean the bean instance to destroy
*/
void destroyBean(Object existingBean);
```

Spring创建对象分为初始化和实例化，createBean创建完整对象，initializeBean只完成初始化，autowireBean进行属性填充

3.ConfigurableListableBeanFactory 

```java
/** * Configuration interface to be implemented by most listable bean factories.In addition to {@link ConfigurableBeanFactory}, it provides facilities to analyze and modify bean definitions, and to pre-instantiate singletons. *被大多数listable bean工厂实现的配置接口。与configurableBeanFactory相比，它增加了分析和修改bean定义信息、预实例化单例对象那个等方法 * */
```

来看一下它包含了哪些方法

```java
/**
* Ensure that all non-lazy-init singletons are instantiated, also considering{FactoryBean FactoryBeans}.Typically invoked at the end of factory setup, if desired.
确保所有非懒加载的单例都实例化，包括FactoryBean。如果需要的话通常在工厂创建之后
* @throws BeansException if one of the singleton beans could not be created.
*有一个单例对象无法创建都会抛出异常
* Note: This may have left the factory with some beans already initialized!Call {@link #destroySingletons()} for full cleanup in this case.
这样可能会让工厂遗留一些已经实例化的对象，出现这种情况的化需要调用congfigurableBeanFactory的destroySingletons()方法
*/
void preInstantiateSingletons() throws BeansException;
/**
* Return the registered BeanDefinition for the specified bean, allowing access to its property values and constructor argument value (which can be modified during bean factory post-processing).
*返回某个bean的定义信息，并且允许方法它的实行和构造方法参数（支持在bean工厂的后置处理中对这些值进行修改）
* <p>A returned BeanDefinition object should not be a copy but the original definition object as registered in the factory. This means that it should be castable to a more specific implementation type, if necessary.
*返回的定义信息不应该是一个拷贝的副本而应该是被注册在bean工厂中的原始定义信息。这意味着如果需要，它应该能够被强转为更具体的实现类型
*/
```

4.AbstractBeanFactory

```java
/**
* Abstract base class for {BeanFactory} implementations, providing the full capabilities of the {ConfigurableBeanFactory} SPI.
* Does <i>not</i> assume a listable bean factory: can therefore also be used as base class for bean factory implementations which obtain bean definitions from some backend resource (where bean definition access is an expensive operation).
*BeanFactory的抽象实现基类，提供了ConfigurableBeanFactory的全部能力。不假设可列出的Bean工厂：因此也可以用于从一些后端资源获取Bean定义信息的bean工厂实现类
* <p>This class provides a singleton cache (through its base class
* {DefaultSingletonBeanRegistry}, singleton/prototype determination, {FactoryBean}handling, aliases, bean definition merging for child bean definitions,and bean destruction (DisposableBean}interface, custom destroy methods).
*这个类提供了单例缓存（通过它的基类DefaultSingletonBeanRegistry），单例/多例对象感知，FactoryBean处理、别名、子bean定义信息合并和bean销毁、自定义销毁方法
*Furthermore, it can manage a bean factory hierarchy (delegating to the parent in case of an unknown bean), through implementing the {HierarchicalBeanFactory} interface.
*进一步，通过实现HierarchicalBeanFactory，它可以管理一个bean工厂的层次结构（把未知的bean委托给父工厂）
* <p>The main template methods to be implemented by subclasses are
* {@link #getBeanDefinition} and {@link #createBean}, retrieving a bean definition or a given bean name and creating a bean instance for a given bean definition,respectively. Default implementations of those operations can be found in
* {@link DefaultListableBeanFactory} and {@link AbstractAutowireCapableBeanFactory}.
* 这个抽象类主要实现的模板方法是getBeanDefinition和createBean，获取一个Bean定义信息或者给定的bean名字，并且根据给定的定义信息创建一个bean实例。这些操作的默认实现在上面这两个类中
* @see #getBeanDefinition
* @see #createBean
* @see AbstractAutowireCapableBeanFactory#createBean
* @see DefaultListableBeanFactory#getBeanDefinition
*/
```

5.AbstractAutowireCapableBeanFactory 

note:AbstractBeanFactory中的doGetBean调用了AbstractAutowireCapableBeanFactory的doCreateBean方法去创建对象 

```java
/** * Abstract bean factory superclass that implements default bean creation,with the full capabilities specified by the {@link RootBeanDefinition} class. *抽象Bean工厂的父类，通过RootBeanDefinition的完整功能实现了默认的bean创建方法 * Implements the {@link AutowireCapableBeanFactory}interface in addition to AbstractBeanFactory's {@link #createBean} method. *实现了AutowireCapableBeanFactory接口以及AbstractBeanFactory的createBean方法 * Provides bean creation (with constructor resolution), property population,wiring (including autowiring), and initialization. Handles runtime bean references, resolves managed collections, calls initialization methods, etc. *提供了bean创建（通过构造器方法），属性填充，注入（包括自动注入）和初始化。处理运行时bean引用，解析管理集合，调用初始化方法等 * Supports autowiring constructors, properties by name, and properties by type. *支持自动注入构造器、名称属性和类型属性 * 
```

5.DefaultListableBeanFactory

```java
/** * Spring's default implementation of the {@link ConfigurableListableBeanFactory}and{BeanDefinitionRegistry} interfaces: a full-fledged bean factory based on bean definition metadata, extensible through post-processors. *ConfigurableListableBeanFactory和BeanDefinitionRegistry的默认实现：一个基于bean定义元数据的完整成熟的bean工厂，可以被post-processors拓展 * Typical usage is registering all bean definitions first (possibly read from a bean definition file), before accessing beans. *通常的使用是再访问bean之前先注册所有的bean定义信息（可能是从bean定义信息文件进行读取） *Bean lookup by name is therefore an inexpensive operation in a local bean definition table,operating on pre-resolved bean definition metadata objects. *因此在本地bean定义信息表通过name查找一个bean是一个廉价的操作，直接对bean定义信息元数据对象进行操作 * 
*注意具体格式bean定义信息的读取通常分别实现而不是向bean工厂一样基于父类
* <p>For an alternative implementation of the{ListableBeanFactory} interface,have a look at {@link StaticListableBeanFactory}, which manages existing bean instances rather than creating new ones based on bean definitions.
```

