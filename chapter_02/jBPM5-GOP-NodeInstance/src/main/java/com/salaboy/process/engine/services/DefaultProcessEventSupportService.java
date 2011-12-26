/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.process.engine.services;

import com.salaboy.process.engine.structures.TaskInstance;

/**
 *
 * @author salaboy
 */
public class DefaultProcessEventSupportService implements ProcessEventSupportService{

    @Override
    public void fireBeforeTaskTriggered(TaskInstance task) {
        System.out.println("BEFORE TASK FIRED: "+ task.getTask());
    }

    @Override
    public void fireAfterTaskTriggered(TaskInstance task) {
        System.out.println("AFTER TASK FIRED: "+ task.getTask());
    }

    @Override
    public void fireBeforeTaskLeft(TaskInstance task) {
        System.out.println("BEFORE TASK LEFT: "+ task.getTask());
    }

    @Override
    public void fireAfterTaskLeft(TaskInstance task) {
        System.out.println("AFTER TASK LEFT: "+ task.getTask());
    }
    
}
