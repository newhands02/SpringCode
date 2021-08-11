AbstractBeanFactory

1.doGetBean（）

```java
/** * Return an instance, which may be shared or independent, of the specified bean. * @param name the name of the bean to retrieve * @param requiredType the required type of the bean to retrieve * @param args arguments to use when creating a bean instance using explicit arguments * (only applied when creating a new instance as opposed to retrieving an existing one) * @param typeCheckOnly whether the instance is obtained for a type check, * not for actual use * @return an instance of the bean * @throws BeansException if the bean could not be created */ 
//获取一个Bean实例，通过名字，类型，指定参数，是否进行类型检查 
@SuppressWarnings("unchecked") protected T doGetBean( String name, @Nullable Class requiredType, @Nullable Object[] args, boolean typeCheckOnly) throws BeansException { String beanName = transformedBeanName(name); Object beanInstance; 
// Eagerly check singleton cache for manually registered singletons. 
//先尝试从缓存里拿，先一级，再二级，都没有就从三级中获取工厂对象，调用getOBject 
//生成对象并加入二级缓存 Object sharedInstance = getSingleton(beanName); 
//如果获取的是单例对象 
if (sharedInstance != null && args == null) { if (logger.isTraceEnabled()) 
{ 
    //如果该单例对象当前正在创建中（没有完全初始化） 
    if (isSingletonCurrentlyInCreation(beanName)) { logger.trace(); } else { logger.trace(); } } 
     //获取Bean对象，检查下是不是工厂对象 
     beanInstance = getObjectForBeanInstance(sharedInstance, name, beanName, null); } 
     //如果缓存中没有，从父类中查找 
else { 
    // Fail if we're already creating this bean instance: 
    // We're assumably within a circular reference. 
    //如果原型对象正在创建中，说明存在循环引用，抛出异常 
    if (isPrototypeCurrentlyInCreation(beanName)) { throw new BeanCurrentlyInCreationException(beanName); }
    // Check if bean definition exists in this factory.
	BeanFactory parentBeanFactory = getParentBeanFactory();
	if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
		// Not found -> check parent.
		String nameToLookup = originalBeanName(name);
		if (parentBeanFactory instanceof AbstractBeanFactory) {
return ((AbstractBeanFactory) parentBeanFactory).doGetBean(
nameToLookup, requiredType, args, typeCheckOnly);
}
else if (args != null) {
// Delegation to parent with explicit args.
return (T) parentBeanFactory.getBean(nameToLookup, args);
}
else if (requiredType != null) {
// No args -> delegate to standard getBean method.
return parentBeanFactory.getBean(nameToLookup, requiredType);
}
else {
return (T) parentBeanFactory.getBean(nameToLookup);
}
//查询父工厂是否包含该Bean，是的话调用父工厂的getBean
}
if (!typeCheckOnly) {
markBeanAsCreated(beanName);
}
StartupStep beanCreation = this.applicationStartup.start("spring.beans.instantiate")
.tag("beanName", name);
try {
if (requiredType != null) {
beanCreation.tag("beanType", requiredType::toString);
}
//获取合并后的定义信息
RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
checkMergedBeanDefinition(mbd, beanName, args);
// Guarantee initialization of beans that the current bean depends on.
//获取对象所依赖的其他对象
String[] dependsOn = mbd.getDependsOn();
if (dependsOn != null) {
for (String dep : dependsOn) {
//依赖的其他对象是否已完成初始化
if (isDependent(beanName, dep)) {
//否则抛出循环引用异常
throw new BeanCreationException(mbd.getResourceDescription(), beanName,
"Circular depends-on relationship between '" + beanName + "' and '" + dep + "'");
}
//都完成了初始化，注册这些Bean
registerDependentBean(dep, beanName);
try {
//获取这些Bean
getBean(dep);
}
catch (NoSuchBeanDefinitionException ex) {
throw new BeanCreationException(mbd.getResourceDescription(), beanName,
"'" + beanName + "' depends on missing bean '" + dep + "'", ex);
}
}
}
  //如果单例缓存中没有、父工厂中也没有，则开始创建 
    // Create bean instance. 
    if (mbd.isSingleton()) { 
        sharedInstance = getSingleton(beanName, () -> { 
            try { 
                return createBean(beanName, mbd, args); } 
            catch (BeansException ex) { 
                // Explicitly remove instance from singleton cache: It might have been put there 
                // eagerly by the creation process, to allow for circular reference resolution. 
                // Also remove any beans that received a temporary reference to the bean. 
                //销毁所有对象，包括创建不完全的对象 
                destroySingleton(beanName); throw ex; } }); 
        beanInstance = getObjectForBeanInstance(sharedInstance, name, beanName, mbd); } 
    else if (mbd.isPrototype()) { 
        // It's a prototype -> create a new instance. 
        Object prototypeInstance = null; 
        try { 
            //把bean name添加到正在创建的多例对象列表中 
            beforePrototypeCreation(beanName); prototypeInstance = createBean(beanName, mbd, args); 
        } finally { 
            //把bean name移除列表 
            afterPrototypeCreation(beanName); 
        } 
        beanInstance = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd); } 
    //这里省略了如果是其他scope的Bean的创建过程 
    //最后把bean对象转换成具体的类型后返回 
    return adaptBeanInstance(name, beanInstance, requiredType); 
}

```

关键就是getSingleton和createBean（）方法，查看DefaultSingletonRegistry中的方法源码 getSingleton方法传入一个beanName和beanFactory返回一个当前bean name已注册的原生单例对象，或者创建并注册一个新的对象

```java
/** * Return the (raw) singleton object registered under the given name, * creating and registering a new one if none registered yet. * @param beanName the name of the bean * @param singletonFactory the ObjectFactory to lazily create the singleton * with, if necessary * @return the registered singleton object */ public Object getSingleton(String beanName, ObjectFactory singletonFactory) { Assert.notNull(beanName, "Bean name must not be null"); //同步，把整个一级缓存锁住 synchronized (this.singletonObjects) { //先从spring一级缓存中拿 Object singletonObject = this.singletonObjects.get(beanName); if (singletonObject == null) { //判断一下对象是否被销毁 if (this.singletonsCurrentlyInDestruction) { throw new BeanCreationNotAllowedException(); } if (logger.isDebugEnabled()) { logger.debug(); } //创建前检查，把bean加入正在创建集合中 beforeSingletonCreation(beanName); boolean newSingleton = false; boolean recordSuppressedExceptions = (this.suppressedExceptions == null); if (recordSuppressedExceptions) { this.suppressedExceptions = new LinkedHashSet<>(); } try { //调用getObject方法创建对象 singletonObject = singletonFactory.getObject(); newSingleton = true; }//有异常的话再尝试去一级缓存拿，没有抛出异常 catch (IllegalStateException ex) { // Has the singleton object implicitly appeared in the meantime ->// if yes, proceed with it since the exception indicates that state. singletonObject = this.singletonObjects.get(beanName); if (singletonObject == null) { throw ex; } } catch (BeanCreationException ex) { if (recordSuppressedExceptions) { for (Exception suppressedException : this.suppressedExceptions) { ex.addRelatedCause(suppressedException); } } throw ex; } finally { if (recordSuppressedExceptions) { this.suppressedExceptions = null; } //创建后后的回调，把bean名移除正在创建列表 afterSingletonCreation(beanName); } if (newSingleton) { //加入一级缓存，移除二级和三级缓存，把bean加入已注册列表 addSingleton(beanName, singletonObject); } } return singletonObject; } }

```

在单例模式下创建对象的过程时调用getSingleton方法，参入bean name和ObjectFactory，ObjectFactory是一个函数式接口，调用它的getObject方法，doCreateBean方法传入了一个creatBean方法。getSingleton方法会先尝试从一级缓存里面拿，没有的话就开始创建对象，并将对象放入一级缓存和注册列表中

接下来看abstractBeanFactory如何createBean 

```java
/** * Central method of this class: creates a bean instance, * populates the bean instance, applies post-processors, etc. * @see #doCreateBean */ @Override protected Object createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args) throws BeanCreationException { if (logger.isTraceEnabled()) { logger.trace("Creating instance of bean '" + beanName + "'"); } RootBeanDefinition mbdToUse = mbd; 
// Make sure bean class is actually resolved at this point, and 
// clone the bean definition in case of a dynamically resolved Class 
// which cannot be stored in the shared merged bean definition. 
//把这个类解析出来 
Class resolvedClass = resolveBeanClass(mbd, beanName); 
if (resolvedClass != null && !mbd.hasBeanClass() && mbd.getBeanClassName() != null) { 
    mbdToUse = new RootBeanDefinition(mbd); 
    mbdToUse.setBeanClass(resolvedClass); } 
    // Prepare method overrides. 
    try { mbdToUse.prepareMethodOverrides(); } 
    catch (BeanDefinitionValidationException ex) { 
        throw new BeanDefinitionStoreException(mbdToUse.getResourceDescription(), beanName, "Validation of method overrides failed", ex); 
    } 
    try { 
        // Give BeanPostProcessors a chance to return a proxy instead of the target bean instance. 
        //调用后置处理器生成代理对象 
        Object bean = resolveBeforeInstantiation(beanName, mbdToUse); 
        if (bean != null) { 
            return bean; 
        } 
    } catch (Throwable ex) { 
        throw new BeanCreationException(mbdToUse.getResourceDescription(), beanName, "BeanPostProcessor before instantiation of bean failed", ex); }
try { 
    //如果没有postProcessor，就开始正式创建bean 
    Object beanInstance = doCreateBean(beanName, mbdToUse, args); 
    if (logger.isTraceEnabled()) { 
        logger.trace("Finished creating instance of bean '" + beanName + "'"); 
    } 
    return beanInstance; 
} catch (BeanCreationException | ImplicitlyAppearedSingletonException ex) { 
    // A previously detected exception with proper bean creation context already, 
    // or illegal singleton state to be communicated up to DefaultSingletonBeanRegistry. 
    throw ex; } 
    catch (Throwable ex) { 
        throw new BeanCreationException( mbdToUse.getResourceDescription(), beanName, "Unexpected exception during bean creation", ex); } 
        }
```

调用的是AbstractAutowireCapableBeanFactory，先根据bean名字和定义信息解析出这个类，再来看看Spring是如何resolveBean的

```java
/**
* Resolve the bean class for the specified bean definition,
* resolving a bean class name into a Class reference (if necessary)
* and storing the resolved Class in the bean definition for further use.
从bean定义信息中解析出具体的bean，把解析的bean赋给一个对象引用并再bean定义信息中存储这个class对象以供使用
*/
@Nullable
protected Class<?> resolveBeanClass(RootBeanDefinition mbd, String beanName, Class<?>... typesToMatch)
throws CannotLoadBeanClassException {
try {
//如果已经存过了，直接返回
if (mbd.hasBeanClass()) {
return mbd.getBeanClass();
}
if (System.getSecurityManager() != null) {
return AccessController.doPrivileged((PrivilegedExceptionAction<Class<?>>)
() -> doResolveBeanClass(mbd, typesToMatch), getAccessControlContext());
}
else {
//开始真正解析Bean
return doResolveBeanClass(mbd, typesToMatch);
}
}
catch (PrivilegedActionException pae) {
//。。。。。。。。。。这里省略了捕获异常的操作
}
}
@Nullable
private Class<?> doResolveBeanClass(RootBeanDefinition mbd, Class<?>... typesToMatch)
throws ClassNotFoundException {
ClassLoader beanClassLoader = getBeanClassLoader();
ClassLoader dynamicLoader = beanClassLoader;
boolean freshResolve = false;
if (!ObjectUtils.isEmpty(typesToMatch)) {
// When just doing type checks (i.e. not creating an actual instance yet),
// use the specified temporary class loader (e.g. in a weaving scenario).
//有类型检查话选取特定的类加载器，跳过
ClassLoader tempClassLoader = getTempClassLoader();
if (tempClassLoader != null) {
dynamicLoader = tempClassLoader;
freshResolve = true;
if (tempClassLoader instanceof DecoratingClassLoader) {
DecoratingClassLoader dcl = (DecoratingClassLoader) tempClassLoader;
for (Class<?> typeToMatch : typesToMatch) {
dcl.excludeClass(typeToMatch.getName());
}
}
}
}
String className = mbd.getBeanClassName();
if (className != null) {
//解析bean定义信息
Object evaluated = evaluateBeanDefinitionString(className, mbd);
if (!className.equals(evaluated)) {
// A dynamically resolved expression, supported as of 4.2...
//解析出来的是对象，直接返回
if (evaluated instanceof Class) {
return (Class<?>) evaluated;
}
else if (evaluated instanceof String) {
//解析出来的是对象名
className = (String) evaluated;
freshResolve = true;
}
else {
throw new IllegalStateException("Invalid class name expression result: " + evaluated);
}
}
    if (freshResolve) {
// When resolving against a temporary class loader, exit early in order
// to avoid storing the resolved Class in the bean definition.
//有加载器的话用加载器加载
if (dynamicLoader != null) {
try {
return dynamicLoader.loadClass(className);
}
catch (ClassNotFoundException ex) {
if (logger.isTraceEnabled()) {
logger.trace("Could not load class [" + className + "] from " + dynamicLoader + ": " + ex);
}
}
}
//否则就用Class.forName
return ClassUtils.forName(className, dynamicLoader);
}
}
//以上是bean定义信息中有beanName的时候，如果没有的话走定义信息中的resolveBean，也是ClassUtil.forName方法
// Resolve regularly, caching the result in the BeanDefinition...
return mbd.resolveBeanClass(beanClassLoader);
}
```

解析出来类后先创建一份bean定义信息的引用，说是防止动态解析的类无法共享bean定义信息？？，然后将解析出来的bean放到bean定义信息中，后续就不用重复解析，然后检测bean中是否有重写方法，然后调用resolveBeforeInstantiation(）方法给beanPostProcessor机会创建一个代理对象然后返回。如果没有使用后置处理器，就开始创建原来的bean，看spring如和doCreateBean

```java
if (instanceWrapper == null) {
//开始创建一个新的bean实例
instanceWrapper = createBeanInstance(beanName, mbd, args);
}
Object bean = instanceWrapper.getWrappedInstance();
Class<?> beanType = instanceWrapper.getWrappedClass();
if (beanType != NullBean.class) {
mbd.resolvedTargetType = beanType;
}
// Allow post-processors to modify the merged bean definition.
//允许后置处理器对bean的定义信息进行修改
synchronized (mbd.postProcessingLock) {
if (!mbd.postProcessed) {
try {
applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
}
catch (Throwable ex) {
}
mbd.postProcessed = true;
}
}
// Eagerly cache singletons to be able to resolve circular references
// even when triggered by lifecycle interfaces like BeanFactoryAware.
//把工厂方法存入三级缓存中，用来解决循环引用的问题
boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences &&
isSingletonCurrentlyInCreation(beanName));
if (earlySingletonExposure) {
if (logger.isTraceEnabled()) {
//省略日志内容
}
addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
}
//实例化Bean结束，开始初始化Bean
// Initialize the bean instance.
Object exposedObject = bean;
try {
//填充属性
populateBean(beanName, mbd, instanceWrapper);
//初始化bean
//初始化过程为先invokeMethod，设置BeanName，BeanClassLoader，BeaFactory；applyBeanPostProcessorsBeforeInitialization执行初始化前的后置处理器；执行initMethod；applyBeanPostProcessorsAfterInitialization执行初始化后的后置处理器
exposedObject = initializeBean(beanName, exposedObject, mbd);
}
catch (Throwable ex) {
//省略抛出异常代码
}
if (earlySingletonExposure) {
Object earlySingletonReference = getSingleton(beanName, false);
if (earlySingletonReference != null) {
if (exposedObject == bean) {
//把earlySingletonReference暴露出来
exposedObject = earlySingletonReference;
}
else if (!this.allowRawInjectionDespiteWrapping && hasDependentBean(beanName)) {
String[] dependentBeans = getDependentBeans(beanName);
Set<String> actualDependentBeans = new LinkedHashSet<>(dependentBeans.length);
for (String dependentBean : dependentBeans) {
if (!removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
actualDependentBeans.add(dependentBean);
}
}
   if (!actualDependentBeans.isEmpty()) { 
       //抛出异常代码 
   } } } } 
       // Register bean as disposable. 
       try { registerDisposableBeanIfNecessary(beanName, bean, mbd); } catch (BeanDefinitionValidationException ex) { } return exposedObject; }
```

先是从缓存查询是否是未完成创建的对象，缓存里没有的话则创建一个新的对象

```java
/**
* Create a new instance for the specified bean, using an appropriate instantiation strategy:
*/
protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mbd, @Nullable Object[] args) {
// Make sure bean class is actually resolved at this point.
Class<?> beanClass = resolveBeanClass(mbd, beanName);
if (beanClass != null && !Modifier.isPublic(beanClass.getModifiers()) && !mbd.isNonPublicAccessAllowed()) {
//抛出异常
}
Supplier<?> instanceSupplier = mbd.getInstanceSupplier();
if (instanceSupplier != null) {
return obtainFromSupplier(instanceSupplier, beanName);
}
if (mbd.getFactoryMethodName() != null) {
//如果有工厂方法的话，使用工厂方法创建对象
return instantiateUsingFactoryMethod(beanName, mbd, args);
}
// Shortcut when re-creating the same bean...
//当创建重复对象的快捷方式
    boolean resolved = false;
boolean autowireNecessary = false;
if (args == null) {
synchronized (mbd.constructorArgumentLock) {
if (mbd.resolvedConstructorOrFactoryMethod != null) {
resolved = true;
//解析构造参数
autowireNecessary = mbd.constructorArgumentsResolved;
}
}
}
if (resolved) {
if (autowireNecessary) {
//如果要求自动注入，则使用构造器注入
return autowireConstructor(beanName, mbd, null, null);
}
else {
return instantiateBean(beanName, mbd);
}
}
// Candidate constructors for autowiring?
Constructor<?>[] ctors = determineConstructorsFromBeanPostProcessors(beanClass, beanName);
if (ctors != null || mbd.getResolvedAutowireMode() == AUTOWIRE_CONSTRUCTOR ||
mbd.hasConstructorArgumentValues() || !ObjectUtils.isEmpty(args)) {
return autowireConstructor(beanName, mbd, ctors, args);
}
// Preferred constructors for default construction?
ctors = mbd.getPreferredConstructors();
if (ctors != null) {
//使用有参构造方法创建对象
return autowireConstructor(beanName, mbd, ctors, null);
}
//用无参方法构造对象
// No special handling: simply use no-arg constructor.
return instantiateBean(beanName, mbd);
}
```

有工厂方法的话就使用工厂方法创建对象，没有的话就使用有参或者无参的构造方法创建对象 如果getSingleton（）方法传入的时bean和布尔值

```java
@Nullable
protected Object getSingleton(String beanName, boolean allowEarlyReference) {
// Quick check for existing instance without full singleton lock
Object singletonObject = this.singletonObjects.get(beanName);
if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
singletonObject = this.earlySingletonObjects.get(beanName);
if (singletonObject == null && allowEarlyReference) {
synchronized (this.singletonObjects) {
//这里用了DCL
singletonObject = this.singletonObjects.get(beanName);
if (singletonObject == null) {
singletonObject = this.earlySingletonObjects.get(beanName);
if (singletonObject == null) {
//从三级缓存里获取到工厂
ObjectFactory<?> singletonFactory =this.singletonFactories.get(beanName);
if (singletonFactory != null) {
//调用getObject方法，当时再doCreateBean中传入的是（）->
//getEarlyReference
singletonObject = singletonFactory.getObject();
//放入二级缓存，移除三级缓存
this.earlySingletonObjects.put(beanName, singletonObject);
this.singletonFactories.remove(beanName);
}
}
}
}
}
}
return singletonObject;
}
```

