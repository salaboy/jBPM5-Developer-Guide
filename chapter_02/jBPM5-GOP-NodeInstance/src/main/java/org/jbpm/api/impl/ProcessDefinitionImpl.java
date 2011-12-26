/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jbpm.api.impl;

import java.util.HashMap;
import java.util.Map;
import org.jbpm.api.Task;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.SequenceFlow;

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
    public void addSequenceFlow(Long id, SequenceFlow sequenceFlow) {
        if(this.sequenceFlows == null){
            this.sequenceFlows = new HashMap<Long, SequenceFlow>();
        }
        this.sequenceFlows.put(id, sequenceFlow);
    }
    
    
}
