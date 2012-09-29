/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.dev.guide.executor;


import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jbpm.task.api.TaskServiceEntryPoint;
import org.jbpm.task.impl.TaskServiceEntryPointImpl;
import org.jbpm.task.lifecycle.listeners.TaskLifeCycleEventListener;

/**
 *
 */
public class ExecutorServiceModule {
    private static ExecutorServiceModule instance;
    private ExecutorService executorService;
    private WeldContainer container;
    private Weld weld;
    
    public static ExecutorServiceModule getInstance(){
        if(instance == null){
            instance = new ExecutorServiceModule();
        }
        return instance;
    }

    public ExecutorServiceModule() {
        weld = new Weld();
        this.container = weld.initialize();
        
        this.executorService = this.container.instance().select(ExecutorServiceImpl.class).get();
        //Singleton.. that we need to instantiate
        //this.container.instance().select(TaskLifeCycleEventListener.class).get(); 
    }

    public ExecutorService getTaskService() {
        return this.executorService;
    }

    public WeldContainer getContainer() {
        return container;
    }
    
    public void dispose(){
        instance = null;
        weld.shutdown();
    }
    
    
}
