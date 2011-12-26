/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.process.engine.factories;

import java.util.Map;
import com.salaboy.process.engine.structures.ProcessDefinition;
import com.salaboy.process.engine.structures.ProcessInstance;
import com.salaboy.process.engine.structures.impl.ProcessInstanceImpl;
import com.salaboy.process.engine.services.DefaultProcessEventSupportService;
import com.salaboy.process.engine.services.Service;

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
