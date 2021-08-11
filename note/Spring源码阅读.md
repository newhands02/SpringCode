## 1.BeanFactory 

```java
/** * The root interface for accessing a Spring bean container. * 访问spring容器的根接口 * This is the basic client view of a bean container; * further interfaces such as ListableBeanFactory and * ConfigurableBeanFactory are available for specific purposes. * bean容器的基础视图，ListableBeanFactory和ConfigurableBeanFactory等都是它的特殊实现 *
* <p>This interface is implemented by objects that hold a number of bean definitions,each uniquely identified by a String name.
*此接口被一个持有多个bean定义信息的对象所实现，通过唯一的name进行标识
*Depending on the bean definition,the factory will return either an independent instance of a contained object (the Prototype design pattern), or a single shared instance (a superior alternative to the Singleton design pattern, in which the instance is a singleton in the scope of the factory).
*根据bean的定义信息，beanFactory会返回一个已包含对象的独立实例（prototype模式下）或者返回一个共享的单例（一种优于单例模式的替代方案），实例在factory的作用域下是单例的
*Which type of instance will be returned depends on the bean factory configuration: the API is the same. Since Spring 2.0, further scopes are available depending on the concrete application context (e.g. "request" and "session" scopes in a web environment).
*那种类型的实例会被返回取决于beanFactory的配置，从spring 2.0 之后更多的bean作用域可供选择如request、session等在web环境中，这依赖于具体applicaiton context的实现
* <p>The point of this approach is that the BeanFactory is a central registry of application components, and centralizes configuration of application components (no more do individual objects need to read properties files,for example). See chapters 4 and 11 of "Expert One-on-One J2EE Design and Development" for a discussion of the benefits of this approach.
*这样做的重点是BeanFactory是applicaiton组件的注册中心，并为组件们提供了中心化的配置（例如不需要再为单独的一个对象去读取配置文件）
*
* <p>Note that it is generally better to rely on Dependency Injection
* ("push" configuration) to configure application objects through setters or constructors, rather than use any form of "pull" configuration like a BeanFactory lookup. Spring's Dependency Injection functionality is
implemented using this BeanFactory interface and its subinterfaces
*注意一般最好使用DI（”推“配置）去配置applicaiton中的对象通过set方法或者构造方法，而不是使用任何形式的（“拉”配置）例如BeanFactory 查找（直接getBean？）
* <p>Normally a BeanFactory will load bean definitions stored in a configuration source (such as an XML document), and use the {@code org.springframework.beans} package to configure the beans. However, an implementation could simply return Java objects it creates as necessary directly in Java code.
*通常一个beanFactory会加载存储在配置源（例如一个xml文件）中的bean的定义信息，并使用org.springframework.beans包中的对象去配置这些bean。但是一个实现类只能简单返回其根据必要的java代码所创建的java对象
*There are no constraints on how the definitions could be stored: LDAP, RDBMS, XML,properties file, etc.
*Implementations are encouraged to support references amongst beans (Dependency Injection).
*beanfactory的实现类被鼓励去支持在bean之间引用
*
* <p>In contrast to the methods in {@link ListableBeanFactory}, all of the operations in this interface will also check parent factories if this is a {@link HierarchicalBeanFactory}. If a bean is not found in this factory instance,the immediate parent factory will be asked. Beans in this factory instance are supposed to override beans of the same name in any parent factory.
*与ListbaleBeanFactory中的方法相反，这个接口如果是HierachicalBeanFactory的话所有的操作都会先检查parent factories，如果一个bean没有在这个工厂实例中找到，则会立刻调用parent factory。这个factory生产的bean支持在任意parent factory中重写同名的Bean
*
* <p>Bean factory implementations should support the standard bean lifecycle interfaces as far as possible. The full set of initialization methods and their standard order is:
*Bean factory的实现类们需要尽可能支持的bean生命周期，完成的初始化方法以及他们的标准顺序如下：
* <ol>
* <li>BeanNameAware's {@code setBeanName}
* <li>BeanClassLoaderAware's {@code setBeanClassLoader}
* <li>BeanFactoryAware's {@code setBeanFactory}
* <li>EnvironmentAware's {@code setEnvironment}
* <li>EmbeddedValueResolverAware's {@code setEmbeddedValueResolver}
* <li>ResourceLoaderAware's {@code setResourceLoader}
* (only applicable when running in an application context)
* <li>ApplicationEventPublisherAware's {@code setApplicationEventPublisher}
* (only applicable when running in an application context)
* <li>MessageSourceAware's {@code setMessageSource}
* (only applicable when running in an application context)
* <li>ApplicationContextAware's {@code setApplicationContext}
* (only applicable when running in an application context)
* <li>ServletContextAware's {@code setServletContext}
* (only applicable when running in a web application context)
* <li>{@code postProcessBeforeInitialization} methods of BeanPostProcessors
* <li>InitializingBean's {@code afterPropertiesSet}
* <li>a custom init-method definition
* <li>{@code postProcessAfterInitialization} methods of BeanPostProcessors
* </ol>
*
* <p>On shutdown of a bean factory, the following lifecycle methods apply:
* <ol>
* <li>{@code postProcessBeforeDestruction} methods of DestructionAwareBeanPostProcessors
* <li>DisposableBean's {@code destroy}
* <li>a custom destroy-method definition
* </ol>
*/

```

## 2.HierarchyBeanFactory

```java
/** * Sub-interface implemented by bean factories that can be part * of a hierarchy. *实现了这个子接口的beanFactory会成为层次结构的一部分 * * 
```

## 3.ListableBeanFactory

```java
/** * Extension of the {@link BeanFactory} interface to be implemented by bean factories that can enumerate all their bean instances, rather than attempting bean lookup by name one by one as requested by clients. *BeanFactory 的拓展接口，为了实现beanFactory可以在客户端请求时枚举它们所有的bean实例，而不是试图去一个接一个的查找 *BeanFactory implementations that preload all their bean definitions (such as XML-based factories) may implement this interface. *需要预加载bean定义信息的BeanFactory实现类（如基于XML文件的工厂类们）可能会实现这个接口 * If this is a {@link HierarchicalBeanFactory}, the return values will not take any BeanFactory hierarchy into account, but will relate only to the beans defined in the current factory. Use the {@link BeanFactoryUtils} helper class to consider beans in ancestor factories too. *即使同样实现了HierarchicalBeanFactory，返回值也不会考虑任何BeanFactory的层次结构，仅会与当前工厂定义的beans相关联（不会向Parent Factory 进行查询）。使用BeanFactoryUtils去考虑祖先工厂中的beans *The methods in this interface will just respect bean definitions of this factory.They will ignore any singleton beans that have been registered by other means like{ConfigurableBeanFactory} {@code registerSingleton} method, with the exception of {@code getBeanNamesForType} and {@code getBeansOfType} which will check such manually registered singletons too. *接口中的方法只会认可此工厂中的bean定义信息，他们会忽略其他工厂注册单例对象的方法，也会检查此类主动注册的单例对象，除了getBeanNamesForType和getBeansOfType方法 *Of course, BeanFactory's {@code getBean} does allow transparent access to such special beans as well. However, in typical scenarios, all beans will be defined by external bean definitions anyway, so most * applications don't need to worry about this differentiation. *当然，BeanFactory的getBean方法的确允许透明访问此类特殊beans，但是在特殊场景下，所有的bean都将被外部的bean定义信息定义，所以大部分applicaiton不必担心这些不同 * 
```

## 4.ApplicationContext

```java
/** * Central interface to provide configuration for an application. * This is read-only while the application is running, but may be * reloaded if the implementation supports this. * 配置applicaiton的核心接口。在程序运行期间是不可被修改的，但能够被重新加载（取决于子实现类是否支持） * An ApplicationContext provides: * 
* Bean factory methods for accessing application components. * Inherited from ListableBeanFactory * The ability to load file resources in a generic fashion. * Inherited from the ResourceLoader interface. * The ability to publish events to registered listeners. * Inherited from the ApplicationEventPublisher interface. * The ability to resolve messages, supporting internationalization. * Inherited from the MessageSource interface. *applcitionContext接口提供了访问应用组件的bean 工厂方法（继承自ListableBeanFactory）,在广泛场景中加载资源文件（继承自ResourceLoader），注册监听器来发布事件（继承自ApplicaitonEventPublisher），解析消息，支持国际化（继承自MEssageSource） * Inheritance from a parent context. Definitions in a descendant context will always take priority. This means, for example, that a single parent context can be used by an entire web application, while each servlet has its own child context that is independent of that of any other servlet. *继承自父类context。后代context中的定义信息始终优先。也就是说有一个父类context应用于整个webapplction，但是每个servlet都有自己的context独立于其他的servlet * 
* * 
```

## 5.ConfigurableApplication

```java
/** * SPI interface to be implemented by most if not all application contexts. *Service Provider Interface,被绝大多数的applicationContext所实现 * Provides facilities to configure an application context in addition * to the application context client methods in the * {@link org.springframework.context.ApplicationContext} interface. *除了applicaitonContext接口中的方法另外提供了措施去配置一个应用上下文 * **/
这个applicaiton中定义了refresh方法和close方法
/** * Load or refresh the persistent representation of the configuration, which might be from Java-based configuration, an XML file, a properties file, a relational database schema, or some other format. *加载或者刷新持久化配置（可能加载自java配置类、xml文件、propeties文件、关联的数据库表或者其他格式） * As this is a startup method, it should destroy already created singletons if it fails, to avoid dangling resources. In other words, after invocation of this method, either all or no singletons at all should be instantiated. *这是一个启动方法，如果执行失败需要销毁或有已经创建的单例，避免干扰资源。也就是说，当方法执行之后，单例对象要么全部被实例化要么一个也没有实例化 * @throws BeansException if the bean factory could not be initialized * @throws IllegalStateException if already initialized and multiple refresh * attempts are not supported */ void refresh() throws BeansException, IllegalStateException;/** * Close this application context, releasing all resources and locks that the implementation might hold. This includes destroying all cached singleton beans. 关闭应用上下文，释放实现类占有的所有资源和锁，包括销毁所有缓存的单例对象 * Note: Does not invoke {@code close} on a parent context; * parent contexts have their own, independent lifecycle. * 
```

## 6.AbstractApplicationContext

```java
/** * Abstract implementation of the ApplicationContext interface. Doesn't mandate the type of storage used for configuration; simply implements common context functionality. applcationContext接口的抽象实现，不适用与缓存类型的配置，只是实现了普通context的功能 Uses the Template Method design pattern,requiring concrete subclasses to implement abstract methods. *使用了模板方法设计模式，要求具体的子类去实现这些抽象方法 * In contrast to a plain BeanFactory, an ApplicationContext is supposed to detect special beans defined in its internal bean factory: * Therefore, this class automatically registers * {BeanFactoryPostProcessor BeanFactoryPostProcessors}, * {BeanPostProcessor BeanPostProcessors}, * and {ApplicationListener ApplicationListeners} * which are defined as beans in the context. *与简朴的BeanFactory接口相比，一个applicationContext支持探测在内部bean工厂定义的特殊的bean，如：BeanFactoryPostProcessor BeanFactoryPostProcessors，BeanPostProcessor BeanPostProcessors，ApplicationListener ApplicationListeners * A {MessageSource} may also be supplied as a bean in the context, with the name "messageSource"; otherwise, message resolution is delegated to the parent context. Furthermore, a multicaster for application events can be supplied as an "applicationEventMulticaster" bean of type {ApplicationEventMulticaster}in the context; otherwise, a default multicaster of type SimpleApplicationEventMulticaster will be used. *在这个context中MessageSource可能会以名为messageSource的bean提供，否则的话消息的解析将会委托给父类context。更多的，application事件的多播器将以名为applicationEventMulticaster的bean提供，否则的话就使用默认的SimpleApplicationEventMulticaster。 * */
```

