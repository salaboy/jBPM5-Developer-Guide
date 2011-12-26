/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jbpm.api;

import java.util.List;

/**
 *
 * @author salaboy
 */
public interface TaskContainer {
    public List<TaskInstance> getTaskInstances();
    public void setTaskInstances(List<TaskInstance> tasks);
    public void addTaskInstance(TaskInstance task);
    public void removeTaskInstance(TaskInstance task);
    public TaskInstance getTaskInstance(Task task);
    public void taskInstanceCompleted(TaskInstance taskInstance, String outType);
}
