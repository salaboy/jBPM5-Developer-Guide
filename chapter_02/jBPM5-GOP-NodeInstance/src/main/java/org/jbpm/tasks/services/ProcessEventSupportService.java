/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jbpm.tasks.services;

import org.jbpm.api.TaskInstance;

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
