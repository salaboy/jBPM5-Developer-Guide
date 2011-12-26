/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.process.engine.factories;

import com.salaboy.process.engine.structures.Task;
import com.salaboy.process.engine.structures.TaskInstance;
import com.salaboy.process.engine.structures.ProcessInstance;
import com.salaboy.process.engine.taskinstances.impl.EndEventInstance;
import com.salaboy.process.engine.taskinstances.impl.ScriptTaskInstance;
import com.salaboy.process.engine.taskinstances.impl.StartEventInstance;
import com.salaboy.process.engine.tasks.impl.EndEvent;
import com.salaboy.process.engine.tasks.impl.ScriptTask;
import com.salaboy.process.engine.tasks.impl.StartEvent;

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
        if(task instanceof ScriptTask){
            ScriptTaskInstance actionTask = new ScriptTaskInstance(processInstance, task,((ScriptTask)task).getAction());
            
            return actionTask;
        }
        
        if(task instanceof EndEvent){
            EndEventInstance endEvent = new EndEventInstance(processInstance, task);
            return endEvent;
        }
        
        return null;
    }
}
