/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jbpm.api;

import java.util.Map;
import org.jbpm.api.impl.ProcessInstanceImpl;
import org.jbpm.tasks.services.Service;

/**
 *
 * @author salaboy
 */
public interface ProcessInstance {
    public void setId(long id);

    public long getId();
    
    public void setProcessDefinition(ProcessDefinition process);

    public ProcessDefinition getProcessDefinition();   
    
    public ContextInstance getContextInstance();
    
    public TaskContainer getTaskContainer();
    
    public void start();

    public void triggerCompleted();
    
    public void addService(String name, Service service);
    
    public Service getService(String name);
    
    public void setServices(Map<String, Service> services);
    
    public void setStatus(ProcessInstanceImpl.STATUS status);
    
    public ProcessInstanceImpl.STATUS getStatus();
    
}
