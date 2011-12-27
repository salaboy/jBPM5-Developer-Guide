/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.process.engine.factories;

import com.salaboy.process.engine.structures.Task;
import com.salaboy.process.engine.structures.NodeInstance;
import com.salaboy.process.engine.structures.ProcessInstance;
import com.salaboy.process.engine.taskinstances.impl.EndEventNodeInstance;
import com.salaboy.process.engine.taskinstances.impl.ScriptTaskNodeInstance;
import com.salaboy.process.engine.taskinstances.impl.StartEventNodeInstance;
import com.salaboy.process.engine.tasks.impl.EndEvent;
import com.salaboy.process.engine.tasks.impl.ScriptTask;
import com.salaboy.process.engine.tasks.impl.StartEvent;

/**
 *
 * @author salaboy
 */
public class NodeInstanceFactory {
    public static NodeInstance newNodeInstance(ProcessInstance processInstance, Task task){
       
        if(task instanceof StartEvent){
            StartEventNodeInstance startEvent = new StartEventNodeInstance(processInstance, task);
            return startEvent;
        }
        if(task instanceof ScriptTask){
            ScriptTaskNodeInstance actionTask = new ScriptTaskNodeInstance(processInstance, task,((ScriptTask)task).getAction());
            
            return actionTask;
        }
        
        if(task instanceof EndEvent){
            EndEventNodeInstance endEvent = new EndEventNodeInstance(processInstance, task);
            return endEvent;
        }
        
        return null;
    }
}
