/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jbpm.api.impl;

import java.util.ArrayList;
import java.util.List;
import org.jbpm.api.Task;
import org.jbpm.api.TaskContainer;
import org.jbpm.api.TaskInstance;
import org.jbpm.taskinstances.impl.EndEventInstance;

/**
 *
 * @author salaboy
 */
public class TaskContainerImpl implements TaskContainer {

    private List<TaskInstance> nodeInstances;

    public TaskContainerImpl() {
        this.nodeInstances = new ArrayList<TaskInstance>();
    }

    public List<TaskInstance> getTaskInstances() {
        return nodeInstances;
    }

    public void setTaskInstances(List<TaskInstance> nodes) {
        this.nodeInstances = nodes;
    }

    @Override
    public void addTaskInstance(TaskInstance node) {
        if (this.nodeInstances == null) {
            this.nodeInstances = new ArrayList<TaskInstance>();
        }
        nodeInstances.add(node);
    }

    @Override
    public void removeTaskInstance(TaskInstance node) {
        nodeInstances.remove(node);
    }

    @Override
    public TaskInstance getTaskInstance(Task node) {
        for (TaskInstance nodeInstance : this.nodeInstances) {
            if (nodeInstance.getTask() == node) {
                return nodeInstance;
            }
        }
        return null;

    }

    @Override
    public void taskInstanceCompleted(TaskInstance nodeInstance, String outType) {
        if (nodeInstance instanceof EndEventInstance) {

            if (nodeInstances.isEmpty()) {
                nodeInstance.getProcessInstance().triggerCompleted();
            }

        }


    }
}
