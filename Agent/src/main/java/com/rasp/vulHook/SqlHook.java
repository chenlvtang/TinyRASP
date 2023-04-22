package com.rasp.vulHook;

import com.rasp.utils.RASPUtils;
import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import com.alibaba.druid.wall.WallUtils;

public class SqlHook implements ClassFileTransformer {
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        // mysql 5.x
        if (className.equals("com/mysql/jdbc/StatementImpl")||className.equals("com/mysql/jdbc/PreparedStatement")) {
            try {
                String loadName = className.replace("/", ".");
                ClassPool pool = ClassPool.getDefault();
                ClassClassPath classPath = new ClassClassPath(this.getClass());
                pool.insertClassPath(classPath);

                System.out.println("Into the SQLHook");
                CtClass clz = pool.get(loadName);
                // Hook住executeInternal、executeQuery等
                CtMethod ctMethod = clz.getDeclaredMethod("executeQuery");

                String code = "System.out.println(\"In the SQLHook \" + $1);" +
                        "Class hookClass = Class.forName(\"com.rasp.vulHook.SqlHook\",true, Thread.currentThread().getContextClassLoader());" +
                        "java.lang.reflect.Method checkSql = hookClass.getDeclaredMethod(\"checkSql\", new Class []{String.class});" +
                        "checkSql.invoke(hookClass.newInstance(), new Object[]{$1});";


                ctMethod.insertBefore(code);
                System.out.println("Finish the SQLHook");
                return clz.toBytecode();
            } catch (Exception e) {
                System.out.println(e);
                throw new RuntimeException(e);
            }
        } else {
            return classfileBuffer;
        }
    }

    public static void checkSql(String sql) throws Exception{
        boolean validate = WallUtils.isValidateMySql(sql);
        if (validate){
            return;
        }
        else{
            RASPUtils.getLogAndAlert("SQLInjection");
            new SecurityException("SQL Injection : " + sql);
        }
    }

}
