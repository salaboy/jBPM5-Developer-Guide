/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.process.engine.structures;

import java.util.Map;

/**
 *
 * @author salaboy
 */
public interface ProcessDefinition {
    public Map<Long, Task> getTasks();
    public void setTasks(Map<Long, Task> tasks);
    public void addTask(Long id, Task task);
}
