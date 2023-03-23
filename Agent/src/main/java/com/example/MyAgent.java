package com.example;

import com.servletHook.ServletHook;
import com.vulHook.RceHook;

import java.lang.instrument.Instrumentation;

public class MyAgent {
    public static void premain(String args, Instrumentation ins){
        System.out.println("======Premain Begin=======");
        ins.addTransformer(new ServletHook());
        ins.addTransformer(new RceHook());
    }

    public static void agentmain(String args, Instrumentation ins) throws Exception {
        System.out.println("======Agentmain Begin=======");
        ins.addTransformer(new RceHook(), true);
        ins.retransformClasses(Class.forName("java.lang.ProcessImpl"));
    }
}
