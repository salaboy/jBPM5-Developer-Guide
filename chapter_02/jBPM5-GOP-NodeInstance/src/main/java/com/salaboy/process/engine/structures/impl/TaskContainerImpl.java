/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.process.engine.structures.impl;

import java.util.ArrayList;
import java.util.List;
import com.salaboy.process.engine.structures.Task;
import com.salaboy.process.engine.structures.TaskContainer;
import com.salaboy.process.engine.structures.TaskInstance;
import com.salaboy.process.engine.taskinstances.impl.EndEventInstance;

/**
 *
 * @author salaboy
 */
public class TaskContainerImpl implements TaskContainer {

    private List<TaskInstance> taskInstances;

    public TaskContainerImpl() {
        this.taskInstances = new ArrayList<TaskInstance>();
    }

    public List<TaskInstance> getTaskInstances() {
        return taskInstances;
    }

    public void setTaskInstances(List<TaskInstance> tasks) {
        this.taskInstances = tasks;
    }

    @Override
    public void addTaskInstance(TaskInstance task) {
        if (this.taskInstances == null) {
            this.taskInstances = new ArrayList<TaskInstance>();
        }
        taskInstances.add(task);
    }

    @Override
    public void removeTaskInstance(TaskInstance task) {
        taskInstances.remove(task);
    }

    @Override
    public TaskInstance getTaskInstance(Task task) {
        for (TaskInstance nodeInstance : this.taskInstances) {
            if (nodeInstance.getTask() == task) {
                return nodeInstance;
            }
        }
        return null;

    }

    @Override
    public void taskInstanceCompleted(TaskInstance taskInstance, String outType) {
        if (taskInstance instanceof EndEventInstance) {

            if (taskInstances.isEmpty()) {
                taskInstance.getProcessInstance().triggerCompleted();
            }

        }


    }
}
