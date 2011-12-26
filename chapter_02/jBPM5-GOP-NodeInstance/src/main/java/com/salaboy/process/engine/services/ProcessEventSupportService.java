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
public interface ProcessEventSupportService extends Service{
    public void fireBeforeTaskTriggered(TaskInstance task);
    public void fireAfterTaskTriggered(TaskInstance task);
    public void fireBeforeTaskLeft(TaskInstance task);
    public void fireAfterTaskLeft(TaskInstance task);
}
