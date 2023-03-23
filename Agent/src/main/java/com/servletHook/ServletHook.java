package com.servletHook;

import com.utils.RASPUtils;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/*Hook住Servlet的service方法，传递给utils包获取并设置上下文*/
public class ServletHook implements ClassFileTransformer {
    private static final String Target_CLASS_PREFIX = "javax/servlet/http/HttpServlet";
    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        if(className != null && className.equals(Target_CLASS_PREFIX)){
            try {
                String loadName = className.replace("/", ".");
                ClassPool pool = ClassPool.getDefault();
                ClassClassPath classPath = new ClassClassPath(this.getClass());
                pool.insertClassPath(classPath);
                System.out.println("In the ServletHook");

                // 获取到servlet中的service方法
                CtClass clz = pool.get(loadName);
                CtMethod method = clz.getDeclaredMethod("service");

                CtClass[] parameterTypes = method.getParameterTypes();

                if (parameterTypes.length == 2) {
                    // 获取并设置上下文，以便后续的告警
                    method.insertBefore("{"
                            + RASPUtils.class.getName() + ".setRequest($1);"
                            + RASPUtils.class.getName() + ".setResponse($2);" +
                            "}");

                    // 在service的最后插入清除上下文，防止混乱
                    method.insertAfter("{"
                            + RASPUtils.class.getName() + ".clear();"
                            + "}");
                }

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
