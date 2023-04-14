package com.rasp.vulHook;

import com.rasp.utils.RASPUtils;
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
                // Hook住init方法
                CtBehavior[] ctBehaviors = clz.getDeclaredConstructors();
                for(CtBehavior cb: ctBehaviors) {
                    // 告警逻辑为：如果命令包含管道符号等符号就告警
                    String code =
                            "String _ = String.join(\" \", cmd);"+
                            "if (_.contains(\"|\") || _.contains(\";\") " +
                                "|| _.contains(\"&\")|| _.contains(\"`\")) { " +
                                RASPUtils.getLogAndAlertCode("RCE") +
                            "}";
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
