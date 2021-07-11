## Spring源码

```java
AbstractApplicaitonContext ac=new ClassPathXmlApplicationContext("application.xml");
```

![image-20210711142950167](C:\Users\661572\AppData\Roaming\Typora\typora-user-images\image-20210711142950167.png)

```java
1.super(parent);//继承父类运行环境（猜测）
2.setConfigLocations(configLocaiton);//设置配置？？
3.refresh()//刷新上下文
```

```java

@Override
	public void refresh() throws BeansException, IllegalStateException {
		synchronized (this.startupShutdownMonitor) {
			/**
			Prepare this context for refreshing, setting its startup date and active flag as well as 			performing any initialization of property sources.
			**/
			prepareRefresh();//刷新前的准备工作

			/**
			Tell the subclass to refresh the internal bean factory.
			Returns:the fresh BeanFactory instance
			**/
			ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory(); //获取Bean工厂

			// Prepare the bean factory for use in this context.
             //给BeanFactory设置一些初始的属性值
			prepareBeanFactory(beanFactory);

			try {
				// Allows post-processing of the bean factory in context subclasses.
                 //这里是一个空方法，留给子类去拓展
				postProcessBeanFactory(beanFactory);

				// Invoke factory processors registered as beans in the context.
                 //执行BeanFactoryPostProcessors
				invokeBeanFactoryPostProcessors(beanFactory);

				// Register bean processors that intercept bean creation.
                 //实例化Bean所用到的BeanPosProcessor，在这里先进行注册
				registerBeanPostProcessors(beanFactory);

				// Initialize message source for this context.
                 //有国际化会涉及不同的语言体，在这里进行相关初始化操作
				initMessageSource();

				// Initialize event multicaster for this context.
                 //初始化多播器
				initApplicationEventMulticaster();

				// Initialize other special beans in specific context subclasses.
                 //也是一个空方法，留给子类去实现，初始化其他Bean对象
				onRefresh();

				// Check for listener beans and register them.
                 //初始化监听器
				registerListeners();

                
				// Instantiate all remaining (non-lazy-init) singletons.
                 //实例化前的准备工作已经完成，开始实例化剩余单例对象（非懒加载的）
				finishBeanFactoryInitialization(beanFactory);

				// Last step: publish corresponding event.
				finishRefresh();
			}

			catch (BeansException ex) {
				if (logger.isWarnEnabled()) {
					logger.warn("Exception encountered during context initialization - " +
							"cancelling refresh attempt: " + ex);
				}

				// Destroy already created singletons to avoid dangling resources.
				destroyBeans();

				// Reset 'active' flag.
				cancelRefresh(ex);

				// Propagate exception to caller.
				throw ex;
			}

			finally {
				// Reset common introspection caches in Spring's core, since we
				// might not ever need metadata for singleton beans anymore...
				resetCommonCaches();
			}

```

![image-20210711144037746](C:\Users\661572\AppData\Roaming\Typora\typora-user-images\image-20210711144037746.png)

```java

	/**
	 * This implementation performs an actual refresh of this context's underlying
	 * bean factory, shutting down the previous bean factory (if any) and
	 * initializing a fresh bean factory for the next phase of the context's lifecycle.
	 */
	@Override
	protected final void refreshBeanFactory() throws BeansException {
        //如果已经有Beanfactory则进行清理
		if (hasBeanFactory()) {
			destroyBeans();
			closeBeanFactory();
		}
		try {
			DefaultListableBeanFactory beanFactory = createBeanFactory();//创建一个新的beanFactory
			beanFactory.setSerializationId(getId());//设置序列化ID
			customizeBeanFactory(beanFactory);//支持自定义beanFactory？？
			loadBeanDefinitions(beanFactory);//加载bean的定义信息到工厂，在这里读取bean定义信息
			this.beanFactory = beanFactory;
		}
		catch (IOException ex) {
			throw new ApplicationContextException("I/O error parsing bean definition source for " + getDisplayName(), ex);
		}
	}
/**
Create an internal bean factory for this context. Called for each refresh() attempt.
The default implementation creates a DefaultListableBeanFactory with the internal bean factory of this context's parent as parent bean factory. Can be overridden in subclasses, for example to customize DefaultListableBeanFactory's settings.
Returns:
the bean factory for this context
**/
	protected DefaultListableBeanFactory createBeanFactory() {
		return new DefaultListableBeanFactory(getInternalParentBeanFactory());
	}
```

开始实例化对象

```java
protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
		// Initialize conversion service for this context.
		if (beanFactory.containsBean(CONVERSION_SERVICE_BEAN_NAME) &&
				beanFactory.isTypeMatch(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class)) {
			beanFactory.setConversionService(
					beanFactory.getBean(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class));
		}

		// Register a default embedded value resolver if no bean post-processor
		// (such as a PropertyPlaceholderConfigurer bean) registered any before:
		// at this point, primarily for resolution in annotation attribute values.
		if (!beanFactory.hasEmbeddedValueResolver()) {
			beanFactory.addEmbeddedValueResolver(strVal -> getEnvironment().resolvePlaceholders(strVal));
		}

		// Initialize LoadTimeWeaverAware beans early to allow for registering their transformers early.
		String[] weaverAwareNames = beanFactory.getBeanNamesForType(LoadTimeWeaverAware.class, false, false);
		for (String weaverAwareName : weaverAwareNames) {
			getBean(weaverAwareName);
		}

		// Stop using the temporary ClassLoader for type matching.
		beanFactory.setTempClassLoader(null);

		// Allow for caching all bean definition metadata, not expecting further changes.
		beanFactory.freezeConfiguration();

		// Instantiate all remaining (non-lazy-init) singletons.
    	//初始化所有的单例对象
		beanFactory.preInstantiateSingletons();
	}

@Override
	public void preInstantiateSingletons() throws BeansException {
		if (logger.isTraceEnabled()) {
			logger.trace("Pre-instantiating singletons in " + this);
		}

		// Iterate over a copy to allow for init methods which in turn register new bean definitions.
		// While this may not be part of the regular factory bootstrap, it does otherwise work fine.
		List<String> beanNames = new ArrayList<>(this.beanDefinitionNames);

		// Trigger initialization of all non-lazy singleton beans...
		for (String beanName : beanNames) {
			RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
			if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {
				if (isFactoryBean(beanName)) {
					Object bean = getBean(FACTORY_BEAN_PREFIX + beanName);
					if (bean instanceof FactoryBean) {
						FactoryBean<?> factory = (FactoryBean<?>) bean;
						boolean isEagerInit;
						if (System.getSecurityManager() != null && factory instanceof SmartFactoryBean) {
							isEagerInit = AccessController.doPrivileged(
									(PrivilegedAction<Boolean>) ((SmartFactoryBean<?>) factory)::isEagerInit,
									getAccessControlContext());
						}
						else {
							isEagerInit = (factory instanceof SmartFactoryBean &&
									((SmartFactoryBean<?>) factory).isEagerInit());
						}
						if (isEagerInit) {
							getBean(beanName);
						}
					}
				}
				else {
					getBean(beanName);
				}
			}
		}

		// Trigger post-initialization callback for all applicable beans...
		for (String beanName : beanNames) {
			Object singletonInstance = getSingleton(beanName);
			if (singletonInstance instanceof SmartInitializingSingleton) {
				SmartInitializingSingleton smartSingleton = (SmartInitializingSingleton) singletonInstance;
				if (System.getSecurityManager() != null) {
					AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
						smartSingleton.afterSingletonsInstantiated();
						return null;
					}, getAccessControlContext());
				}
				else {
					smartSingleton.afterSingletonsInstantiated();
				}
			}
		}
	}
```

