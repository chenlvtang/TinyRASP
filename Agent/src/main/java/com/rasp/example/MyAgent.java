package com.rasp.example;

import com.rasp.servletHook.ServletHook;
import com.rasp.vulHook.RceHook;



import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class MyAgent {
    public static void premain(String args, Instrumentation ins) throws UnmodifiableClassException {
        System.out.println("======Premain Begin=======");
        // 添加转换器
        ins.addTransformer(new ServletHook(), true);
        ins.addTransformer(new RceHook(), true);
        System.out.println("======Premain Finish=======");
    }

//    public static void agentmain(String args, Instrumentation ins) throws Exception {
//        System.out.println("======Agentmain Begin=======");
//        ins.addTransformer(new RceHook(), true);
//        ins.retransformClasses(Class.forName("java.lang.ProcessImpl"));
//    }
}
