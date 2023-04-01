package com.rasp.vulHook;

import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class Agent implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        if(className.endsWith("Test")){
            try {
                String loadName = className.replace("/", ".");
                ClassPool pool = ClassPool.getDefault();
                ClassClassPath classPath = new ClassClassPath(this.getClass());
                pool.insertClassPath(classPath);
                System.out.println("In the Agent");
                CtClass clz = pool.get(loadName);
                CtMethod method = clz.getDeclaredMethod("echoSomething");
                method.insertBefore("return \"Hacker\";");
//                System.out.println("In the Agent");
                return clz.toBytecode();
            } catch (Exception e) {
                System.out.println(e);
                throw new RuntimeException(e);
            }
        }
        // 返回原来的字节码
        return classfileBuffer;
    }
}
