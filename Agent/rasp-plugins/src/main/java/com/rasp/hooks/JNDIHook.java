package com.rasp.hooks;

import com.rasp.utils.RASPUtils;
import javassist.CtClass;
import javassist.CtMethod;
import javax.naming.Reference;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class JNDIHook extends AbstractHook {
    // 协议黑名单
    private static String[] dangerProtocol = new String[]{"ldap://", "rmi://"};
    // 远程地址白名单
    private static String[] whiteAddr = new String[]{"127.0.0.1"};
    // 禁止加载的类
    private static String[] blackClass = new String[]{"BeanFactory"};

    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {

        // Protocol Blacklist
        //thanks to @github.com/ez-lbz, ref: https://github.com/chenlvtang/TinyRASP/issues/4
        if (className.equals("com/sun/jndi/toolkit/url/GenericURLContext")) {
            try {
                CtClass clz = RASPUtils.getTargetClass(className, this.getClass());
                CtMethod ctMethod = null;
                ctMethod = clz.getDeclaredMethod("lookup",
                        new CtClass[]{clz.getClassPool().get("java.lang.String")});
                String code = RASPUtils.getInjectCode(this.getClass().getName());
                ctMethod.insertBefore(code);
                return clz.toBytecode();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        // factory addr and name check
        else if(className.equals("javax/naming/spi/NamingManager")){
            try {
                CtClass clz = RASPUtils.getTargetClass(className, this.getClass());
                CtMethod ctMethod = null;
                ctMethod = clz.getDeclaredMethod("getObjectFactoryFromReference");
                String code = RASPUtils.getInjectCode(this.getClass().getName());
                ctMethod.insertBefore(code);
                return clz.toBytecode();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        // check the JAVA_ATTRIBUTES[1]
        else if (className.equals("com/sun/jndi/ldap/Obj")) {
            try {
                CtClass clz = RASPUtils.getTargetClass(className, this.getClass());
                CtMethod ctMethod = null;
                ctMethod = clz.getDeclaredMethod("decodeObject");
                String code = RASPUtils.getInjectCode(this.getClass().getName());
                ctMethod.insertBefore(code);
                return clz.toBytecode();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return classfileBuffer;
        }
    }

    @Override
    public void checkLogic(Object[] args) throws Exception {
        if (args[0] instanceof String) {
            checkProtocol((String) args[0]);
        } else if (args[0] instanceof Reference) {
            checkFactory((Reference) args[0], (String) args[1]);
        }else if (args[0] instanceof Attributes) {
            checkAttr((Attributes) args[0]);
        }

    }

    public static void checkProtocol(String url) throws Exception{
        for (String item : dangerProtocol) {
            if (url.contains(item)) {
                RASPUtils.getLogAndAlert("JNDI");
                throw new SecurityException("JNDI Injection");
            }
        }
    }

    public static void checkFactory(Reference ref, String factoryName) throws Exception{
        String addr = ref.getFactoryClassLocation();
        if (addr != null){
            for (String item : whiteAddr) {
                if (addr.contains(item)) {
                    break;
                }
            }
        }

        for (String item : blackClass) {
            if (factoryName.contains(item)) {
                RASPUtils.getLogAndAlert("JNDI");
                throw new SecurityException("JNDI Injection");
            }
        }
    }

    public static void checkAttr(Attributes attrs) throws Exception{
        if (attrs.get("javaSerializedData") != null) {
            RASPUtils.getLogAndAlert("JNDI");
            throw new SecurityException("JNDI Injection");
        }
    }
}

