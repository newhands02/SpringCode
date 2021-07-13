循环依赖

1.为什么需要三级缓存才能解决问题

如果能保证所有bean都不会调用getEarlyBeanReferece方法，使用二级缓存就能解决问题

```java

	/**
	 * Obtain a reference for early access to the specified bean,
	 * typically for the purpose of resolving a circular reference.
	 * @param beanName the name of the bean (for error handling purposes)
	 * @param mbd the merged bean definition for the bean
	 * @param bean the raw bean instance
	 * @return the object to expose as bean reference
	 */
	protected Object getEarlyBeanReference(String beanName, RootBeanDefinition mbd, Object bean) {
		Object exposedObject = bean;
        /**
        	在这里的判断为了解决AOP代理问题
        **/
		if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
			for (BeanPostProcessor bp : getBeanPostProcessors()) {
				if (bp instanceof SmartInstantiationAwareBeanPostProcessor) {
					SmartInstantiationAwareBeanPostProcessor ibp = (SmartInstantiationAwareBeanPostProcessor) bp;
					exposedObject = ibp.getEarlyBeanReference(exposedObject, beanName);
				}
			}
		}
		return exposedObject;
	}
/**
是一个接口方法，有两个实现类，分别是
AbstractAutoProxyCreator和InstantiationAwareBeanPostProcessorAdapter
**/
default Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {
		return bean;
}
/**
查看第一个AbstractAutoProxyCreator的实现
**/
@Override
public Object getEarlyBeanReference(Object bean, String beanName) {
		Object cacheKey = getCacheKey(bean.getClass(), beanName);
		this.earlyProxyReferences.put(cacheKey, bean);
		return wrapIfNecessary(bean, beanName, cacheKey);
}

/**
	 * Wrap the given bean if necessary, i.e. if it is eligible for being proxied.
	 * @param bean the raw bean instance
	 * @param beanName the name of the bean
	 * @param cacheKey the cache key for metadata access
	 * @return a proxy wrapping the bean, or the raw bean instance as-is
*/
protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {
		if (StringUtils.hasLength(beanName) && this.targetSourcedBeans.contains(beanName)) {
			return bean;
		}
		if (Boolean.FALSE.equals(this.advisedBeans.get(cacheKey))) {
			return bean;
		}
		if (isInfrastructureClass(bean.getClass()) || shouldSkip(bean.getClass(), beanName)) {
			this.advisedBeans.put(cacheKey, Boolean.FALSE);
			return bean;
		}

		// Create proxy if we have advice.
		Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(bean.getClass(), beanName, null);
		if (specificInterceptors != DO_NOT_PROXY) {
			this.advisedBeans.put(cacheKey, Boolean.TRUE);
            /**关键方法，在这里创建了代理**/
			Object proxy = createProxy(
					bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean));
			this.proxyTypes.put(cacheKey, proxy.getClass());
			return proxy;
		}

		this.advisedBeans.put(cacheKey, Boolean.FALSE);
		return bean;
	}

/**
	 * Create an AOP proxy for the given bean.
	 * @param beanClass the class of the bean
	 * @param beanName the name of the bean
	 * @param specificInterceptors the set of interceptors that is
	 * specific to this bean (may be empty, but not null)
	 * @param targetSource the TargetSource for the proxy,
	 * already pre-configured to access the bean
	 * @return the AOP proxy for the bean
	 * @see #buildAdvisors
 */
	protected Object createProxy(Class<?> beanClass, @Nullable String beanName,
			@Nullable Object[] specificInterceptors, TargetSource targetSource) {

		if (this.beanFactory instanceof ConfigurableListableBeanFactory) {
			AutoProxyUtils.exposeTargetClass((ConfigurableListableBeanFactory) this.beanFactory, beanName, beanClass);
		}

		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.copyFrom(this);

		if (!proxyFactory.isProxyTargetClass()) {
			if (shouldProxyTargetClass(beanClass, beanName)) {
				proxyFactory.setProxyTargetClass(true);
			}
			else {
				evaluateProxyInterfaces(beanClass, proxyFactory);
			}
		}

		Advisor[] advisors = buildAdvisors(beanName, specificInterceptors);
		proxyFactory.addAdvisors(advisors);
		proxyFactory.setTargetSource(targetSource);
		customizeProxyFactory(proxyFactory);

		proxyFactory.setFrozen(this.freezeProxy);
		if (advisorsPreFiltered()) {
			proxyFactory.setPreFiltered(true);
		}
/**通过代理工厂获取代理对象**/
		return proxyFactory.getProxy(getProxyClassLoader());
	}


	/**
	 * Create a new proxy according to the settings in this factory.
	 * <p>Can be called repeatedly. Effect will vary if we've added
	 * or removed interfaces. Can add and remove interceptors.
	 * <p>Uses the given class loader (if necessary for proxy creation).
	 * @param classLoader the class loader to create the proxy with
	 * (or {@code null} for the low-level proxy facility's default)
	 * @return the proxy object
	 */
	public Object getProxy(@Nullable ClassLoader classLoader) {
        /**创建AOP代理**/
		return createAopProxy().getProxy(classLoader);
	}

	/**
	 * Create a new proxy object.
	 * <p>Uses the given class loader (if necessary for proxy creation).
	 * {@code null} will simply be passed down and thus lead to the low-level
	 * proxy facility's default, which is usually different from the default chosen
	 * by the AopProxy implementation's {@link #getProxy()} method.
	 * @param classLoader the class loader to create the proxy with
	 * (or {@code null} for the low-level proxy facility's default)
	 * @return the new proxy object (never {@code null})
	 */
/**AopProxy接口，getProxy是一个接口方法，有两个实现类，分别对象AOP两种生成代理的方法**/
	Object getProxy(@Nullable ClassLoader classLoader);

```

![image-20210712175722493](C:\Users\661572\AppData\Roaming\Typora\typora-user-images\image-20210712175722493.png)

2.三级缓存本质是解决AOP代理问题；生成代理对象后是否还会生成普通对象？会！！所以需要代理的bean在创建过程中会存在两种状态的对象----代理对象和普通对象，因为Spring是单例的，需要保证对象的全局唯一，所以在getEarlyBeanReferece中加了一层判断，如果需要生成代理对象的化就会进行覆盖