package com.rasp.vulHook;

import com.rasp.utils.RASPUtils;
import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import com.alibaba.druid.wall.WallUtils;

public class SpELHook implements ClassFileTransformer {
    // 黑名单
    private static String[] spelBlackList = {
            "java.lang.Runtime",
            "java.lang.ProcessBuilder",
            "javax.script.ScriptEngineManager",
            "java.lang.System",
            "org.springframework.cglib.core.ReflectUtils",
            "java.io.File",
            "javax.management.remote.rmi.RMIConnector"
    };

    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className.equals("org/springframework/expression/common/TemplateAwareExpressionParser")) {
            try {
                String loadName = className.replace("/", ".");
                ClassPool pool = ClassPool.getDefault();
                ClassClassPath classPath = new ClassClassPath(this.getClass());
                pool.insertClassPath(classPath);
                // 暂时用来解决源码运行可以，但打包后的SpringBoot类无法Hook的问题
                pool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));

                System.out.println("Into the SpELHook");
                CtClass clz = pool.get(loadName);
                // Hook住parseExpression
                CtMethod ctMethod = clz.getDeclaredMethod("parseExpression");

                String code = "System.out.println(\"In the SpELHook \" + $1);" +
                        "Class hookClass = Class.forName(\"com.rasp.vulHook.SpELHook\",true, Thread.currentThread().getContextClassLoader());" +
                        "java.lang.reflect.Method checkSpEL = hookClass.getDeclaredMethod(\"checkSpEL\", new Class []{String.class});" +
                        "checkSpEL.invoke(hookClass.newInstance(), new Object[]{$1});";


                ctMethod.insertBefore(code);
                System.out.println("Finish the SpELHook");
                return clz.toBytecode();
            } catch (Exception e) {
                System.out.println(e);
                throw new RuntimeException(e);
            }
        } else {
            return classfileBuffer;
        }
    }

    public static void checkSpEL(String expression) throws Exception {
        for (String item : spelBlackList) {
            if (expression.contains(item)) {
                RASPUtils.getLogAndAlert("SpEL");
                throw new SecurityException("illegal expression" + expression);
            }
        }
    }
}
