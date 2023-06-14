package com.rasp.vulHook;

import com.rasp.utils.RASPUtils;
import javassist.*;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.Descriptor;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FileHook implements ClassFileTransformer {
    // 允许读取的文件格式
    private static final Set<String> ALLOWED_FILE_EXTENSIONS = new HashSet<>(Arrays.asList("css", "jpg"));
    // 目录穿越黑名单
    private static String[] travelPath = new String[]{"../", "..\\", ".."};
    // 危险目录黑名单
    private static Set<String> dangerPathList = new HashSet<String>(Arrays.asList(
            "/", "/home", "/etc",
            "/usr", "/usr/local",
            "/var/log", "/proc",
            "/sys", "/root",
            "C:\\", "D:\\", "E:\\")
    );



    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className.equals("java/io/FileInputStream")) {
            try {
                String loadName = className.replace("/", ".");
                ClassPool pool = ClassPool.getDefault();
                ClassClassPath classPath = new ClassClassPath(this.getClass());
                pool.insertClassPath(classPath);

                System.out.println("Into the FileHook");
                CtClass clz = pool.get(loadName);

                //init(File file)插桩
                CtBehavior[] ctBehaviors = clz.getDeclaredConstructors();
                for(CtBehavior cb: ctBehaviors) {
                    CtClass[] parameterTypes = cb.getParameterTypes();
                    if (parameterTypes != null && parameterTypes.length == 1 && parameterTypes[0].getName().equals("java.io.File")) {
                        // 排除tzdb.dat，不然会影响log4j2的加载
                        String code ="System.out.println(\"In the FileHook \" + $1);" +
                                "if (!($1.getPath().endsWith(\"tzdb.dat\"))){" +
                                "Class hookClass = Class.forName(\"com.rasp.vulHook.FileHook\",true, Thread.currentThread().getContextClassLoader());" +
                                "java.lang.reflect.Method checkFilePathMethod = hookClass.getDeclaredMethod(\"checkFilePath\", new Class []{(java.io.File).class});" +
                                "checkFilePathMethod.invoke(hookClass.newInstance(), new Object[]{$1});}";
                        cb.insertBefore(code);
                    }
                }

                return clz.toBytecode();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return classfileBuffer;
    }


    // 路径检测算法
    public static void checkFilePath(String filePath) throws Exception {
            // 判断是否为空
            if(filePath == null){
                return;
            }
            // 判断是否存在目录穿越
            if (isPathTraversal(filePath)) {
                RASPUtils.getLogAndAlert("FileRead");
                throw new SecurityException("PathTraversal is not allowed: " + filePath);
            }
            // 是否为危险目录
            if(isDangerPath(filePath)){
                RASPUtils.getLogAndAlert("FileRead");
                throw new SecurityException("DangerPath is not allowed: " + filePath);
            }
            // 是否为允许的文件后缀

    }

    public static void checkFilePath(File file) throws Exception{
        String filePath = file.getPath();
        checkFilePath(filePath);
    }

    // 参考JRASP检测算法
    // 目录穿越检测
    public static boolean isPathTraversal(String filePath) {
        for (String item : travelPath) {
            if (filePath.contains(item)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDangerPath(String filePath){
        File file = new File(filePath);
        String realpath = "";
        try {
            realpath = file.getCanonicalPath();
        } catch (IOException e) {
            realpath = file.getAbsolutePath();
        }
        if (dangerPathList.contains(realpath)) {
            return true;
        }
        return false;
    }


}