package com.rasp.hooks;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public abstract class AbstractHook implements ClassFileTransformer {
    public abstract byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                     ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException;

    public abstract void checkLogic(Object[] args) throws Exception;
}
