/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jbpm.taskinstances.impl;

import org.jbpm.api.Action;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.TaskInstance;
import org.jbpm.api.SequenceFlow;
import org.jbpm.api.Task;

/**
 *
 * @author salaboy
 */
public class ActionTaskInstance extends AbstractTaskInstance {

    private Action action;

    public ActionTaskInstance(ProcessInstance pI, Task task, Action action) {
        super(pI, task);
        this.action = action;
    }

   

    @Override
    public void internalTrigger(TaskInstance from, String type) {
        action.execute();
        triggerCompleted(SequenceFlow.FLOW_DEFAULT_TYPE, true);
    }

    public Action getAction() {
        return action;
    }
}
