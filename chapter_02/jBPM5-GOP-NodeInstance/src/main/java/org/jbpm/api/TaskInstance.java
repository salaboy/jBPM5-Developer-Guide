/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jbpm.api;

/**
 *
 * @author salaboy
 */
public interface TaskInstance {

    public void setTask(Task task);

    public Task getTask();

    public void trigger(TaskInstance from, String type);

    public ProcessInstance getProcessInstance();
}
