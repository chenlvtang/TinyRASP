package com.rasp.vulHook;

import com.rasp.utils.RASPUtils;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Array;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;

public class RceHook implements ClassFileTransformer {
    private static ArrayList<String> whiteLists = new ArrayList<>(Arrays.asList(
            "ping 127.0.0.1"
    ));

    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className.endsWith("ProcessImpl")||className.endsWith("UnixProcess")) {
            try {
                String loadName = className.replace("/", ".");
                ClassPool pool = ClassPool.getDefault();
                ClassClassPath classPath = new ClassClassPath(this.getClass());
                pool.insertClassPath(classPath);

                System.out.println("Into the RCEHook");
                CtClass clz = pool.get(loadName);
                // Hook住init方法
                CtBehavior[] ctBehaviors = clz.getDeclaredConstructors();
                for(CtBehavior cb: ctBehaviors) {
                    // 告警逻辑为：如果命令包含管道符号等符号就告警
                    String code =
                            "System.out.println(\"In the RceHook\");" +
                            "String _ = String.join(\" \", cmd);"+
                            "if (!_.equals(\"\")) { " +
                                "Class hookClass = Class.forName(\"com.rasp.vulHook.RceHook\",true, Thread.currentThread().getContextClassLoader());" +
                                "java.lang.reflect.Method checkCmd = hookClass.getDeclaredMethod(\"checkCmd\", new Class []{String.class});" +
                                "checkCmd.invoke(hookClass.newInstance(), new Object[]{_});"+
                            "}";
                    cb.insertBefore(code);
                }

                System.out.println("Finish the RceHook");
                return clz.toBytecode();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return classfileBuffer;
        }
    }

    public static void checkCmd(String cmd) throws Exception{
        if (!whiteLists.contains(cmd)){
            RASPUtils.getLogAndAlert("RCE");
            throw new SecurityException("illegal command");
        }
    }

}
