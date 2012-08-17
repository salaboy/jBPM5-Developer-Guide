/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.process.engine.structures;

import java.util.Map;
import com.salaboy.process.engine.structures.impl.ProcessInstanceImpl;
import com.salaboy.process.engine.services.Service;

/**
 *
 * @author salaboy
 */
public interface ProcessInstance extends NodeInstanceContainer{
    public enum STATUS {

        CREATED, ACTIVE, SUSPENDED, CANCELLED, ENDED
    };
    
    public void setId(long id);

    public long getId();
    
    public void setProcessDefinition(ProcessDefinition process);

    public ProcessDefinition getProcessDefinition();   
    
    public ContextInstance getContextInstance();
    
    public void start();
    
    public void start(Map<String, Object> variables);

    public void triggerCompleted();
    
    public void addService(String name, Service service);
    
    public Service getService(String name);
    
    public void setServices(Map<String, Service> services);
    
    public void setStatus(ProcessInstanceImpl.STATUS status);
    
    public ProcessInstanceImpl.STATUS getStatus();
    
}
