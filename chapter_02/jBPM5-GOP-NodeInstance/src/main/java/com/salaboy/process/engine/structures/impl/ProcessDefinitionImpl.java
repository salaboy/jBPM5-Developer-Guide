/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.process.engine.structures.impl;

import com.salaboy.process.engine.structures.Flow;
import java.util.HashMap;
import java.util.Map;
import com.salaboy.process.engine.structures.Task;
import com.salaboy.process.engine.structures.ProcessDefinition;
import com.salaboy.process.engine.structures.SequenceFlow;

/**
 *
 * @author salaboy
 */
public class ProcessDefinitionImpl implements ProcessDefinition{
    private Map<Long, Task> tasks;
    private Map<Long, SequenceFlow> sequenceFlows;
    
    public ProcessDefinitionImpl() {
        this.tasks = new HashMap<Long, Task>();
        this.sequenceFlows = new HashMap<Long, SequenceFlow>();
    }
    
    
    @Override
    public Map<Long, Task> getTasks() {
        return this.tasks;
    }

    @Override
    public void setTasks(Map<Long, Task> tasks) {
        this.tasks = tasks;
    }

    @Override
    public void addTask(Long id, Task task) {
        if(this.tasks == null){
            this.tasks = new HashMap<Long, Task>();
        }
        this.tasks.put(id, task);
    }

    public Map<Long, SequenceFlow> getSequenceFlows() {
        return sequenceFlows;
    }

    public void setSequenceFlows(Map<Long, SequenceFlow> sequenceFlows) {
        this.sequenceFlows = sequenceFlows;
    }
    
     @Override
    public void addFlow(Long id, Flow flow) {
        if(this.sequenceFlows == null){
            this.sequenceFlows = new HashMap<Long, SequenceFlow>();
        }
        this.sequenceFlows.put(id, (SequenceFlow)flow);
    }
    
    
}
