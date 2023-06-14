package com.rasp.example;

import com.rasp.servletHook.ServletHook;
import com.rasp.vulHook.*;


import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class MyAgent {
    public static void premain(String args, Instrumentation ins) throws UnmodifiableClassException {
        System.out.println("======Premain Begin=======");
        // 添加转换器
        ins.addTransformer(new ServletHook(), true);
        ins.addTransformer(new RceHook(), true);
        ins.addTransformer(new FileHook(), true);
        ins.addTransformer(new SqlHook(), true);
        ins.addTransformer(new SerialHook(), true);
        ins.addTransformer(new JNDIHook(), true);
        ins.addTransformer(new SpELHook(), true);
        // 重新定义所有已经加载过的类，这样可以确保所有的类都被 hook (不然FileInputStream Hook不上)
        Class[] allLoadedClasses = ins.getAllLoadedClasses();
        for (Class aClass : allLoadedClasses) {
            if (ins.isModifiableClass(aClass) && !aClass.getName().startsWith("java.lang.invoke.LambdaForm")){
                // 调用instrumentation中所有的ClassFileTransformer#transform方法，实现类字节码修改
                ins.retransformClasses(new Class[]{aClass});
            }
        }
        System.out.println("======Premain Finish=======");
    }
}
