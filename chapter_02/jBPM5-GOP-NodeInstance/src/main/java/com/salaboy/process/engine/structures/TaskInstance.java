/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.process.engine.structures;

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
