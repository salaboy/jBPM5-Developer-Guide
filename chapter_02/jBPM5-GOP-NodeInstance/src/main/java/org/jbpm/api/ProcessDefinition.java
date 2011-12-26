/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jbpm.api;

import java.util.Map;

/**
 *
 * @author salaboy
 */
public interface ProcessDefinition {
    public Map<Long, Task> getTasks();
    public void setTasks(Map<Long, Task> tasks);
    public void addTask(Long id, Task node);
    public void addSequenceFlow(Long id, SequenceFlow sequenceFlow);
}
