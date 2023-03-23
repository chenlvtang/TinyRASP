package com.vulHook;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;

public class RceHook implements ClassFileTransformer {
    public RceHook() {
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className.endsWith("ProcessImpl")) {
            try {
                String loadName = className.replace("/", ".");
                ClassPool pool = ClassPool.getDefault();
                ClassClassPath classPath = new ClassClassPath(this.getClass());
                pool.insertClassPath(classPath);

                System.out.println("In the RCEHook");
                CtClass clz = pool.get(loadName);
                CtBehavior[] ctBehaviors = clz.getDeclaredConstructors();

                for(CtBehavior cb: ctBehaviors) {
                    String code = "Class utilsClass = " +
                            "Class.forName(\"com.utils.RASPUtils\", " +
                            "true, Thread.currentThread().getContextClassLoader());" +
                            "Object utilsObj = utilsClass.newInstance();" +
                            "java.lang.reflect.Method method = " +
                            "utilsClass.getDeclaredMethod(\"alert\", " +
                            "new Class []{String.class});" +
                            "method.setAccessible(true);" +
                            "Object[] args = new Object[]{\"Hacker\"};" +
                            "method.invoke(utilsObj, args);";
                    cb.insertBefore(code);
                }

                return clz.toBytecode();
            } catch (Exception e) {
                System.out.println(e);
                throw new RuntimeException(e);
            }
        } else {
            return classfileBuffer;
        }
    }
}
