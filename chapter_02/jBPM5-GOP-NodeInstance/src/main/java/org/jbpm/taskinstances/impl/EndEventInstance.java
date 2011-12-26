/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jbpm.taskinstances.impl;

import org.jbpm.api.ProcessInstance;
import org.jbpm.api.Task;
import org.jbpm.api.TaskInstance;
import org.jbpm.api.impl.ProcessInstanceImpl;
import org.jbpm.tasks.services.ProcessEventSupportService;

/**
 *
 * @author salaboy
 */
public class EndEventInstance extends AbstractTaskInstance {

    public EndEventInstance(ProcessInstance pI, Task task) {
        super(pI, task);
    }

    
    
    
    @Override
    public void internalTrigger(TaskInstance from, String type) {
        this.processInstance.setStatus(ProcessInstanceImpl.STATUS.ENDED);
        System.out.println("YOU REACH THE END OF THE PROCESS");
    }

   
    
}
