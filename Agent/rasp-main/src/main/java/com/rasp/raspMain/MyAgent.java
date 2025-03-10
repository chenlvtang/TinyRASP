package com.rasp.raspMain;

import com.rasp.myLoader.RaspClassLoader;


import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.List;


public class MyAgent {
    public static void premain(String args, Instrumentation ins) throws Exception {
        System.out.println("======Premain Begin=======");

        // 使用自定义的RaspClassLoader，实现Agent依赖包隔离
        // 动态实例化所有Hook类
        List<Object> hooks = RaspClassLoader.getRaspClassLoader().getAllHookClasses();

        // 动态添加转换器
        for (Object hook : hooks) {
            ins.addTransformer((ClassFileTransformer) hook, true);
        }


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
