/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.process.engine.taskinstances.impl;

import com.salaboy.process.engine.structures.Action;
import com.salaboy.process.engine.structures.ProcessInstance;
import com.salaboy.process.engine.structures.NodeInstance;
import com.salaboy.process.engine.structures.SequenceFlow;
import com.salaboy.process.engine.structures.Task;
import com.salaboy.process.engine.tasks.impl.ScriptTask;

/**
 *
 * @author salaboy
 */
public class ScriptTaskNodeInstance extends AbstractNodeInstance {

    private Action action;

    public ScriptTaskNodeInstance(ProcessInstance pI, Task task, Action action) {
        super(pI, task);
        this.action = action;
    }

   

    @Override
    public void internalTrigger(NodeInstance from, String type) {
        System.out.println("Executing Script Task ("+((ScriptTask)this.task).getDialect()+") !");
        action.execute();
        triggerCompleted(SequenceFlow.FLOW_DEFAULT_TYPE, true);
    }

    public Action getAction() {
        return action;
    }
}
