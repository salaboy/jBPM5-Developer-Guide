/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.process.engine.factories;

import com.salaboy.process.engine.structures.Task;
import com.salaboy.process.engine.structures.NodeInstance;
import com.salaboy.process.engine.structures.ProcessInstance;
import com.salaboy.process.engine.taskinstances.impl.EndTaskNodeInstance;
import com.salaboy.process.engine.taskinstances.impl.ScriptTaskNodeInstance;
import com.salaboy.process.engine.taskinstances.impl.StartTaskNodeInstance;
import com.salaboy.process.engine.tasks.impl.EndTask;
import com.salaboy.process.engine.tasks.impl.ScriptTask;
import com.salaboy.process.engine.tasks.impl.StartTask;

/**
 *
 * @author salaboy
 */
public class NodeInstanceFactory {
    public static NodeInstance newNodeInstance(ProcessInstance processInstance, Task task){
       
        if(task instanceof StartTask){
            StartTaskNodeInstance startTask = new StartTaskNodeInstance(processInstance, task);
            return startTask;
        }
        if(task instanceof ScriptTask){
            ScriptTaskNodeInstance actionTask = new ScriptTaskNodeInstance(processInstance, task,((ScriptTask)task).getAction());
            
            return actionTask;
        }
        
        if(task instanceof EndTask){
            EndTaskNodeInstance endTask = new EndTaskNodeInstance(processInstance, task);
            return endTask;
        }
        
        return null;
    }
}
