/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.salaboy.process.engine.taskinstances.impl;

import com.salaboy.process.engine.factories.NodeInstanceFactory;
import java.util.List;
import com.salaboy.process.engine.structures.Task;
import com.salaboy.process.engine.structures.NodeInstance;
import com.salaboy.process.engine.structures.ProcessInstance;
import com.salaboy.process.engine.structures.SequenceFlow;
import com.salaboy.process.engine.services.ProcessEventSupportService;
import com.salaboy.process.engine.services.ProcessEventSupportServiceFactory;

/**
 *
 * @author salaboy
 */
public abstract class AbstractNodeInstance implements NodeInstance {

    protected ProcessInstance processInstance;
    protected Task task;
    protected ProcessEventSupportService eventService;

    public AbstractNodeInstance(ProcessInstance processInstance, Task task) {
        this.processInstance = processInstance;
        this.task = task;
        eventService = (ProcessEventSupportService)processInstance.getService("event-service");
    }
    
    
    
    @Override
    public final void trigger(NodeInstance from, String type) {

        //Fire before TASK Triggered

        eventService.fireBeforeTaskTriggered(this);

        internalTrigger(from, type);

        //Fire after TASK Triggered
        eventService.fireAfterTaskTriggered(this);

    }

    @Override
    public ProcessInstance getProcessInstance() {
        return processInstance;
    }

    public void setProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
    }

    public abstract void internalTrigger(NodeInstance from, String type);

    private ProcessEventSupportService getProcessEventSupportService() {
        return ProcessEventSupportServiceFactory.getService();
    }

    protected void triggerCompleted(String type, boolean remove) {
        if (remove) {

            processInstance.removeNodeInstance(this);
        }
        Task task = getTask();
        List<SequenceFlow> flows = null;
        if (task != null) {
            flows = task.getOutgoingFlows(type);
        }
        if (flows == null || flows.isEmpty()) {
            processInstance.nodeInstanceCompleted(this, type);
        } else {
            for (SequenceFlow flow : flows) {
                triggerConnection(flow);
            }
        }
    }

    protected void triggerConnection(SequenceFlow flow) {

        eventService.fireBeforeTaskLeft(this);
        this.processInstance.addNodeInstance(NodeInstanceFactory.newNodeInstance(this.processInstance, flow.getTo()));
        // trigger next TASK
        this.processInstance.getNodeInstance(flow.getTo()).trigger(this, flow.getToType());

        eventService.fireAfterTaskLeft(this);

    }

    @Override
    public void setTask(Task task) {
        this.task = task;
    }

    @Override
    public Task getTask() {
        return this.task;
    }
}
