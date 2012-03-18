package com.salaboy.jbpm5.dev.guide.executor;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ExecutorMain {

    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        final Executor executor = (Executor) ctx.getBean("executorService");
        Runtime.getRuntime().addShutdownHook(new Thread() {

            public void run() {
                System.out.println(" >>> Destroying Executor Service!");
                executor.destroy();
            }
        });
        
        executor.init();

    }
}
