/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jbpm.factories;

import org.jbpm.api.Task;
import org.jbpm.api.TaskInstance;
import org.jbpm.api.ProcessInstance;
import org.jbpm.taskinstances.impl.ActionTaskInstance;
import org.jbpm.taskinstances.impl.EndEventInstance;
import org.jbpm.taskinstances.impl.StartEventInstance;
import org.jbpm.tasks.impl.ActionTask;
import org.jbpm.tasks.impl.EndEvent;
import org.jbpm.tasks.impl.StartEvent;

/**
 *
 * @author salaboy
 */
public class TaskInstanceFactory {
    public static TaskInstance newTaskInstance(ProcessInstance processInstance, Task task){
       
        if(task instanceof StartEvent){
            StartEventInstance startEvent = new StartEventInstance(processInstance, task);
            return startEvent;
        }
        if(task instanceof ActionTask){
            ActionTaskInstance actionTask = new ActionTaskInstance(processInstance, task,((ActionTask)task).getAction());
            
            return actionTask;
        }
        
        if(task instanceof EndEvent){
            EndEventInstance endEvent = new EndEventInstance(processInstance, task);
            return endEvent;
        }
        
        return null;
    }
}
