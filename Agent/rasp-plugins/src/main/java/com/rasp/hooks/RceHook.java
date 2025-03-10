package com.rasp.hooks;

import com.rasp.utils.RASPUtils;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

public class RceHook implements ClassFileTransformer {

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
                    // 插入检测函数
                    String code = "System.out.println(\"In the RCEHook \" + $1 + Thread.currentThread().getContextClassLoader());" +
                            // 获取参数
                            "String _ = String.join(\" \", $1);" +
                            // 通过反射动态加载 RaspClassLoader, 因为ProcessImpl由BootStarpClassLoader加载，没办法直接调用RaspClassLoader
                            "Class raspClassLoaderClass = Class.forName(\"com.rasp.myLoader.RaspClassLoader\", true, Thread.currentThread().getContextClassLoader());"+
                            "java.lang.reflect.Method  getRaspClassLoader = raspClassLoaderClass.getMethod(\"getRaspClassLoader\", new Class[0]);"+
                            "ClassLoader raspClassLoaderInstance = getRaspClassLoader.invoke(null, new Object[0]);"+

                            "Class hookClass = Class.forName(\"com.rasp.hooks.RceHook\",true, raspClassLoaderInstance);"+
                            "java.lang.reflect.Method checkCmd = hookClass.getDeclaredMethod(\"checkCmd\", new Class []{String.class});" +
                            "checkCmd.invoke(hookClass.newInstance(), new Object[]{_});";
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

    public static void checkCmd(String cmd) throws Exception {
        List<String> whiteList = new ArrayList<String>();
        whiteList.add("ping 127.0.0.1");
        if (!whiteList.contains(cmd)){
            try{
                RASPUtils.getLogAndAlert("RCE");
            } catch (Exception e){
                //RASP 本身错误，catch住，不影响程序原本进行
                System.out.println(e);
            }
            // 抛出错误，中断当前线程，实现拦截
            throw new RuntimeException("RCE Attack -- RASP");
        }
    }

}
