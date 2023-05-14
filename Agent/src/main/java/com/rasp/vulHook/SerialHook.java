package com.rasp.vulHook;

import com.alibaba.druid.wall.WallUtils;
import com.rasp.utils.RASPUtils;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.ObjectStreamClass;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SerialHook implements ClassFileTransformer {
    // jdk反序列化类黑名单（JRASP加自收集）
    private static Set<String> BlackClassSet = new HashSet<String>(Arrays.asList(
            "org.codehaus.groovy.runtime.ConvertedClosure",
            "org.codehaus.groovy.runtime.ConversionHandler",
            "org.codehaus.groovy.runtime.MethodClosure",
            "org.springframework.transaction.support.AbstractPlatformTransactionManager",
            "java.rmi.server.UnicastRemoteObject",
            "java.rmi.server.RemoteObjectInvocationHandler",
            "com.bea.core.repackaged.springframework.transaction.support.AbstractPlatformTransactionManager",
            "java.rmi.server.RemoteObject",
            "com.tangosol.coherence.rest.util.extractor.MvelExtractor",
            "java.lang.Runtime",
            "oracle.eclipselink.coherence.integrated.internal.cache.LockVersionExtractor",
            "org.eclipse.persistence.internal.descriptors.MethodAttributeAccessor",
            "org.eclipse.persistence.internal.descriptors.InstanceVariableAttributeAccessor",
            "org.apache.commons.fileupload.disk.DiskFileItem",
            "oracle.jdbc.pool.OraclePooledConnection",
            "com.tangosol.util.extractor.ReflectionExtractor",
            "com.tangosol.internal.util.SimpleBinaryEntry",
            "com.tangosol.coherence.component.util.daemon.queueProcessor.service.grid.partitionedService.PartitionedCache$Storage$BinaryEntry",
            "com.sun.rowset.JdbcRowSetImpl",
            "org.eclipse.persistence.internal.indirection.ProxyIndirectionHandler",
            "bsh.XThis",
            "bsh.Interpreter",
            "com.mchange.v2.c3p0.PoolBackedDataSource",
            "com.mchange.v2.c3p0.impl.PoolBackedDataSourceBase",
            "org.apache.commons.beanutils.BeanComparator",
            "java.util.PriorityQueue",
            "java.lang.reflect.Proxy",
            "clojure.lang.PersistentArrayMap",
            "org.apache.commons.io.output.DeferredFileOutputStream",
            "org.apache.commons.io.output.ThresholdingOutputStream",
            "org.apache.wicket.util.upload.DiskFileItem",
            "org.apache.wicket.util.io.DeferredFileOutputStream",
            "org.apache.wicket.util.io.ThresholdingOutputStream",
            "com.sun.org.apache.bcel.internal.util.ClassLoader",
            "com.sun.syndication.feed.impl.ObjectBean",
            "org.springframework.beans.factory.ObjectFactory",
            "org.springframework.aop.framework.AdvisedSupport",
            "org.springframework.aop.target.SingletonTargetSource",
            "com.vaadin.data.util.NestedMethodProperty",
            "com.vaadin.data.util.PropertysetItem",
            "javax.management.BadAttributeValueExpException",
            "org.apache.myfaces.context.servlet.FacesContextImpl",
            "org.apache.myfaces.context.servlet.FacesContextImplBase",
            //补充
            "org.apache.commons.collections.functors.InvokerTransformer",
            "org.apache.commons.collections.functors.InstantiateTransformer",
            "org.apache.commons.collections4.functors.InvokerTransformer",
            "org.apache.commons.collections4.functors.InstantiateTransformer"

    ));


    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {

        if (className.equals("java/io/ObjectInputStream")) {
            try {
                String loadName = className.replace("/", ".");
                ClassPool pool = ClassPool.getDefault();
                ClassClassPath classPath = new ClassClassPath(this.getClass());
                pool.insertClassPath(classPath);

                System.out.println("Into the SerialHook");
                CtClass clz = pool.get(loadName);
                // Hook住 resolveClass (Java反序列化流程中的关键点)
                CtMethod ctMethod = clz.getDeclaredMethod("resolveClass");

                String code = "System.out.println(\"In the SerialHook \" + $1);" +
                        "Class hookClass = Class.forName(\"com.rasp.vulHook.SerialHook\",true, Thread.currentThread().getContextClassLoader());" +
                        "java.lang.reflect.Method checkName = hookClass.getDeclaredMethod(\"checkName\", new Class []{String.class});" +
                        "checkName.invoke(hookClass.newInstance(), new Object[]{$1.getName()});"
                        ;

                ctMethod.insertBefore(code);
                System.out.println("Finish the SerialHook");
                return clz.toBytecode();
            } catch (Exception e) {
                System.out.println(e);
                throw new RuntimeException(e);
            }
        } else {
            return classfileBuffer;
        }
    }


    public static void checkName(String name) throws Exception{
        if (BlackClassSet.contains(name)){
            RASPUtils.getLogAndAlert("Deserialization");
            throw new SecurityException("Illegal Deserialization Class: " + name);
        }
        else{
            return;
        }
    }

}
