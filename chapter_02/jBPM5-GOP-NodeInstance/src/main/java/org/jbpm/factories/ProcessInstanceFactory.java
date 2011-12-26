/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jbpm.factories;

import java.util.Map;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.impl.ProcessInstanceImpl;
import org.jbpm.tasks.services.DefaultProcessEventSupportService;
import org.jbpm.tasks.services.Service;

/**
 *
 * @author salaboy
 */
public class ProcessInstanceFactory {
    public static ProcessInstance newProcessInstance(ProcessDefinition process){
        ProcessInstance instance = new ProcessInstanceImpl(process);
        instance.addService("event-service", new DefaultProcessEventSupportService());
        return instance;
    }
    
    public static ProcessInstance newProcessInstance(ProcessDefinition process, Map<String, Service> services){
        ProcessInstance instance = new ProcessInstanceImpl(process);
        instance.setServices(services);
        return instance;
    }
}
