/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package org.jbpm.api.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.net.ssl.SSLEngineResult;
import org.jbpm.api.ContextInstance;
import org.jbpm.api.TaskContainer;
import org.jbpm.api.TaskInstance;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessInstance;
import org.jbpm.factories.TaskInstanceFactory;
import org.jbpm.tasks.services.Service;

/**
 *
 * @author salaboy
 */
public class ProcessInstanceImpl implements ProcessInstance {


   

    public enum STATUS {

        CREATED, ACTIVE, SUSPENDED, CANCELLED, ENDED
    };
    private long id;
    private ProcessDefinition process;
    private ContextInstance context;
    private TaskContainer taskContainer;
    private STATUS status;
    
    private Map<String, Service> services = new HashMap<String, Service>();

    public ProcessInstanceImpl() {
    }

    public ProcessInstanceImpl(ProcessDefinition process) {
        this.id = new Random().nextLong();
        this.process = process;
        this.context = new ContextInstanceImpl();
        this.taskContainer = new TaskContainerImpl();
        this.status = STATUS.CREATED;
    }

    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }


    public void setServices(Map<String, Service> services) {
        this.services = services;
    }
    
    @Override
    public ProcessDefinition getProcessDefinition() {
        return process;
    }

    @Override
    public void start() {
        // We should check that the first task inside the process.tasks is a startEvent
        TaskInstance startEvent = TaskInstanceFactory.newTaskInstance(this, process.getTasks().get(0L));
        this.taskContainer.addTaskInstance(startEvent);
        this.status = STATUS.ACTIVE;
        startEvent.trigger(null, null);

    }
    
    public void addService(String name, Service service) {
        this.services.put(name, service);
    }
    
    public Service getService(String name){
        return this.services.get(name);
    }

    @Override
    public void setProcessDefinition(ProcessDefinition process) {
        this.process = process;
    }

    @Override
    public ContextInstance getContextInstance() {
        return context;
    }

    @Override
    public TaskContainer getTaskContainer() {
        return taskContainer;
    }

    @Override
    public void triggerCompleted() {
        this.status = STATUS.ENDED;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }
    public STATUS getStatus() {
        return status;
    }
}
