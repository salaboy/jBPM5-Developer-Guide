/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package org.jbpm.taskinstances.impl;

import org.jbpm.api.ProcessInstance;
import org.jbpm.api.TaskInstance;
import org.jbpm.api.SequenceFlow;
import org.jbpm.api.Task;

/**
 *
 * @author salaboy
 */
public class StartEventInstance extends AbstractTaskInstance {

    private long id;

    public StartEventInstance(ProcessInstance pI, Task task) {
        super(pI, task);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public void internalTrigger(TaskInstance from, String type) {
        if (type != null) {
            throw new IllegalArgumentException(
                    "A StartEvent does not accept incoming connections!");
        }
        if (from != null) {
            throw new IllegalArgumentException(
                    "A StartEvent can only be triggered by the process itself!");
        }
        triggerCompleted();
    }

    public void triggerCompleted() {
        triggerCompleted(SequenceFlow.FLOW_DEFAULT_TYPE, true);
    }
}
