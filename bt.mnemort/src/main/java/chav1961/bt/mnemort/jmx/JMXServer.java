package chav1961.bt.mnemort.jmx;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.cdb.CompilerUtils;

public class JMXServer implements AutoCloseable {
	private static final String			PARTITION = "bt.mnemort";
    private static final MBeanServer	SERVER = ManagementFactory.getPlatformMBeanServer();
    private static final String			COMMON_NAME = "type=basic,name=game"; 

    @FunctionalInterface
    private interface LambdaGetter {
    	Object get() throws Throwable;
    }

    @FunctionalInterface
    private interface LambdaSetter {
    	void set(Object value) throws Throwable;
    }

    @FunctionalInterface
    private interface LambdaExecutor {
    	Object execute(Object[] parameters) throws Throwable;
    }
    
	public JMXServer() throws EnvironmentException {
		try{registerItem(COMMON_NAME,new BasicMBean());
		} catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException | IllegalAccessException exc) {
		    throw new EnvironmentException("MBean registration error ("+exc.getClass().getSimpleName()+") : "+exc.getLocalizedMessage(),exc);
		}		
	}

	@Override
	public void close() throws EnvironmentException {
		try{unregisterItem(COMMON_NAME);
		} catch (InstanceNotFoundException | MalformedObjectNameException | MBeanRegistrationException exc) {
			throw new EnvironmentException("MBean unregistering error ("+exc.getClass().getSimpleName()+") : "+exc.getLocalizedMessage(),exc);
		}
	}
	
	private void registerItem(final String item, final DynamicMBean mbean) throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		final ObjectName	name = new ObjectName(PARTITION+":"+item);
		
		SERVER.registerMBean(mbean, name);
	}
	
	private void unregisterItem(final String item) throws InstanceNotFoundException, MalformedObjectNameException, MBeanRegistrationException {
		final ObjectName	name = new ObjectName(PARTITION+":"+item);
	
		SERVER.unregisterMBean(name);
	}
	
	private static LambdaGetter buildLambdaGetter(final DynamicMBean bean, final Method m) throws IllegalAccessException {
		final MethodHandle	mh = MethodHandles.lookup().unreflect(m);

		return new LambdaGetter() {
			@Override
			public Object get() throws Throwable {
				return mh.invokeExact(bean);
			}
		};
	}
	
	private static LambdaSetter buildLambdaSetter(final DynamicMBean bean, final Method m) throws IllegalAccessException {
		final MethodHandle	mh = MethodHandles.lookup().unreflect(m);

		return new LambdaSetter() {
			@Override
			public void set(final Object value) throws Throwable {
				mh.invokeExact(bean, value);
			}
		};
	}

	private static LambdaExecutor buildLambdaExecutor(final DynamicMBean bean, final Method m) throws IllegalAccessException {
		final MethodHandle	mh = MethodHandles.lookup().unreflect(m);

		return new LambdaExecutor() {
			@Override
			public Object execute(final Object[] parameters) throws Throwable {
				return mh.invoke(bean, parameters);
			}
		};
	}
	
	private static void buildAttributes(final DynamicMBean bean, final Class<?> beanClass, final List<MBeanAttributeInfo> attrs, final List<LambdaAttr> lambdas) throws IllegalAccessException {
		if (beanClass != null) {
			final Map<String,Method[]>	names = new HashMap<>(); 
			
			for (Method item : beanClass.getDeclaredMethods()) {
				if (item.isAnnotationPresent(AttrGetter.class)) {
					if (item.getParameterCount() != 0) {
						throw new IllegalArgumentException("Method "+item+" marked with @AttrGetter must not contain any parameters");
					}
					else {
						final String	name =  item.getAnnotation(AttrGetter.class).name();
						
						if (!names.containsKey(name)) {
							names.put(name, new Method[2]);
						}
						names.get(name)[0] = item;
					}
				}
				if (item.isAnnotationPresent(AttrSetter.class)) {
					if (item.getParameterCount() != 1) {
						throw new IllegalArgumentException("Method "+item+" marked with @AttrSetter must contain exactly one parameter");
					}
					else {
						final String	name =  item.getAnnotation(AttrSetter.class).name();
						
						if (!names.containsKey(name)) {
							names.put(name, new Method[2]);
						}
						names.get(name)[1] = item;
					}
				}
			}
			for (Entry<String, Method[]> item : names.entrySet()) {
				final boolean		hasGetter = item.getValue()[0] != null, hasSetter = item.getValue()[1] != null;
				final Class<?>		type = item.getValue()[0] != null ? item.getValue()[0].getReturnType() : item.getValue()[1].getParameterTypes()[0];
				final String		desc = item.getValue()[0] != null ? item.getValue()[0].getAnnotation(AttrGetter.class).description().value() : item.getValue()[1].getParameterTypes()[0].getAnnotation(AttrGetter.class).description().value();
				final LambdaGetter	getter = hasGetter ? buildLambdaGetter(bean, item.getValue()[0]) : null;
				final LambdaSetter	setter = hasSetter ? buildLambdaSetter(bean, item.getValue()[1]) : null;
				
				if (hasGetter && hasSetter) {
					if (item.getValue()[0].getReturnType() != item.getValue()[1].getParameterTypes()[0]) {
						throw new IllegalArgumentException("Getter and setter methods mismatch for attribute ["+item.getKey()+"] returned type for getter must be the same as parameter type for setter");
					}
				}
				attrs.add(new MBeanAttributeInfo(item.getKey(), type.getCanonicalName(), desc, hasGetter, hasSetter, false));
				lambdas.add(new LambdaAttr(item.getKey(), getter, setter));
			}
			buildAttributes(bean, beanClass.getSuperclass(), attrs, lambdas);
		}
	}
	

	private static MBeanConstructorInfo buildConstructorInfo(final Class<?> bean) {
		for (Constructor<?> item : bean.getConstructors()) {
			if (item.getParameterCount() == 0) {
			    return new MBeanConstructorInfo(bean.getSimpleName()+": default constructor",item);
			}
		}
		throw new IllegalArgumentException("Class ["+bean.getCanonicalName()+"] doesn't hane default public constructor");
	}

	private static void buildOperations(final DynamicMBean bean, final Class<?> beanClass, final List<MBeanOperationInfo> operations, final List<LambdaOperation> lambdaOperation) throws IllegalAccessException {
		if (beanClass != null) {
			for (Method item : beanClass.getDeclaredMethods()) {
				if (item.isAnnotationPresent(Operation.class)) {
					final List<MBeanParameterInfo>	parms = new ArrayList<>();
					
					for (Parameter parm : item.getParameters()) {
						if (parm.isAnnotationPresent(Description.class)) {
							parms.add(new MBeanParameterInfo(parm.getName(), parm.getType().getCanonicalName(), parm.getAnnotation(Description.class).value()));
						}
						else {
							parms.add(new MBeanParameterInfo(parm.getName(), parm.getType().getCanonicalName(), parm.getName()));
						}
					}
					operations.add(new MBeanOperationInfo(item.getName(), item.getAnnotation(Operation.class).value(), parms.toArray(new MBeanParameterInfo[parms.size()]), item.getReturnType().getCanonicalName(), MBeanOperationInfo.ACTION));
					lambdaOperation.add(new LambdaOperation(item.getName(), CompilerUtils.buildMethodSignature(item), buildLambdaExecutor(bean, item)));
				}
			}
			buildOperations(bean, beanClass.getSuperclass(), operations, lambdaOperation);
		}
	}
	
	private static MBeanInfo buildMBeanInfo(final DynamicMBean bean, final List<LambdaAttr> lambdaAttr, final List<LambdaOperation> lambdaOperation) throws IllegalAccessException {
		final Class<?>					clazz = bean.getClass();
		final String 					dClassName = clazz.getName();
		final String					dDescription = clazz.getAnnotation(Description.class).value();
		final List<MBeanAttributeInfo>	attrs = new ArrayList<>();
		final List<MBeanOperationInfo>	operations = new ArrayList<>();
		
		buildAttributes(bean, clazz, attrs, lambdaAttr);
		buildOperations(bean, clazz, operations, lambdaOperation);
	    return new MBeanInfo(dClassName, dDescription, attrs.toArray(new MBeanAttributeInfo[attrs.size()]), 
	    					new MBeanConstructorInfo[] {buildConstructorInfo(clazz)}, operations.toArray(new MBeanOperationInfo[operations.size()]),
	    					new MBeanNotificationInfo[0]);
	}

	private static class LambdaAttr {
		final String		name;
		final LambdaGetter	getter;
		final LambdaSetter	setter;
		
		private LambdaAttr(final String name, final LambdaGetter getter, final LambdaSetter setter) {
			this.name = name;
			this.getter = getter;
			this.setter = setter;
		}
	}

	private static class LambdaOperation {
		final String			name;
		final String			signature;
		final LambdaExecutor	executor;
		
		private LambdaOperation(final String name, final String signature, final LambdaExecutor executor) {
			this.name = name;
			this.signature = signature;
			this.executor = executor;
		}
	}
	
	static class BasicMBean implements DynamicMBean {
		private final MBeanInfo						info;
		private final Map<String,LambdaAttr>		attrs = new HashMap<>();
		private final Map<String,LambdaExecutor>	exec = new HashMap<>();
		
		public BasicMBean() throws IllegalAccessException {
			final List<LambdaAttr>		gas = new ArrayList<>();
			final List<LambdaOperation>	oper = new ArrayList<>();
			
			this.info = buildMBeanInfo(this, gas, oper);
			for (LambdaAttr item : gas) {
				attrs.put(item.name, item);
			}
			for (LambdaOperation item : oper) {
				exec.put(item.name+item.signature.substring(0,item.signature.lastIndexOf(')'))+'V', item.executor);
			}
		}
		
		@Override
		public Object getAttribute(final String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
			try{if (attribute == null || attribute.isEmpty() || !attrs.containsKey(attribute)) {
					throw new AttributeNotFoundException(""+attribute);
				}
				else {
					return attrs.get(attribute).getter.get();
				}
			} catch (Throwable e) {
				throw new ReflectionException(new Exception(e));
			}
		}

		@Override
		public void setAttribute(final Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
			try{if (attribute == null || attribute.getName() == null || attribute.getName().isEmpty() || !attrs.containsKey(attribute.getName())) {
					throw new AttributeNotFoundException(""+attribute.getName());
				}
				else {
					attrs.get(attribute.getName()).setter.set(attribute.getValue());
				}
			} catch (Throwable e) {
				throw new ReflectionException(new Exception(e));
			}
		}

		@Override
		public AttributeList getAttributes(final String[] attributes) {
			final AttributeList	result = new AttributeList();
			
			if (attributes != null) {
				for (String item : attributes) {
					try{result.add(new Attribute(item, getAttribute(item)));
					} catch (AttributeNotFoundException | MBeanException | ReflectionException e) {
					}
				}
			}
			return result;
		}

		@Override
		public AttributeList setAttributes(final AttributeList attributes) {
			if (attributes != null) {
				for (Attribute item : attributes.asList()) {
					try{setAttribute(item);
					} catch (AttributeNotFoundException | InvalidAttributeValueException | MBeanException | ReflectionException e) {
					}
				}
			}
			return attributes;
		}

		@Override
		public Object invoke(final String actionName, final Object[] params, final String[] signature) throws MBeanException, ReflectionException {
			if (actionName == null || actionName.isEmpty() || signature == null || Utils.checkArrayContent4Nulls(signature) >= 0) {
				throw new IllegalArgumentException("Action name or signature are null or contains nulls inside");
			}
			else {
				final String	key = actionName+CompilerUtils.buildParametersSignature(signature, "void");
				
				if (!exec.containsKey(key)) {
					throw new IllegalArgumentException("Action name ["+actionName+"] with signature "+Arrays.toString(signature)+" not found in the MBean");
				}
				else {
					try{return exec.get(key).execute(params);
					} catch (Throwable e) {
						throw new ReflectionException(new Exception(e));
					}
				}
			}
		}

		@Override
		public MBeanInfo getMBeanInfo() {
			return info;
		}
	}
}
