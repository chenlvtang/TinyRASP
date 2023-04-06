package com.rasp.vulHook;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class RceHook implements ClassFileTransformer {
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className.endsWith("ProcessImpl")) {
            try {
                String loadName = className.replace("/", ".");
                ClassPool pool = ClassPool.getDefault();
                ClassClassPath classPath = new ClassClassPath(this.getClass());
                pool.insertClassPath(classPath);

                System.out.println("Into the RCEHook");
                CtClass clz = pool.get(loadName);
                CtBehavior[] ctBehaviors = clz.getDeclaredConstructors();

//                ClassLoader loader1 = clz.getClass().getClassLoader();
//                System.out.println(clz.getName() + " is loaded by " + loader1);

                for(CtBehavior cb: ctBehaviors) {
                    // 经过反复折磨写出来的重定向到告警页面代码，不要轻易改动 ORZ
                    String code =
                            // 反射调用RASPUtils
                            "Class utilsClass = " +
                            "Class.forName(\"com.rasp.utils.RASPUtils\", " +
                            "true, Thread.currentThread().getContextClassLoader());" +
                            // 实例化RASPUtils
                            "Object utilsObj = utilsClass.newInstance();" +
                            // 获取getLog方法
                            "java.lang.reflect.Method method =" +
                            "utilsClass.getDeclaredMethod(\"getLog\", "+
                            "new Class []{String.class});" +
                            // 调用日志记录方法
                            "Object[] args = new Object[]{\"RCE\"};" +
                            "method.invoke(utilsObj, args);" +
                            // 获取alert方法
                            "method = " +
                            "utilsClass.getDeclaredMethod(\"alert\", " +
                            "new Class []{String.class});" +
                            // 调用告警方法
                            "Object[] value = new Object[]{(String) utilsClass.getDeclaredField(\"alertInfo\").get(null)};" +
                            "method.invoke(utilsObj, value);" +
                            // 进行拦截
                            "return null;";
                    cb.insertBefore(code);
                }

                System.out.println("Finish the RceHook");
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
