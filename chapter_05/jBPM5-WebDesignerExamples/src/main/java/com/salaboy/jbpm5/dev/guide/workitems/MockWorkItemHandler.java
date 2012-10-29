/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.dev.guide.workitems;

import java.util.HashMap;
import java.util.Map;
import org.drools.process.instance.WorkItemHandler;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;

/**
 * Mock Work Item Handler used for testing purposes. When executed it saves
 * all the input parameters received.
 * @author esteban
 */
public class MockWorkItemHandler implements WorkItemHandler{

    private long workItemId;
    private WorkItemManager manager;
    
    private Map<String,Object> inputParameters;
    
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        //save the info needed to complete the work item handler later
        this.workItemId = workItem.getId();
        this.manager = manager;
        
        //clear any previous input
        this.inputParameters = new HashMap<String, Object>();
        
        //save the map of received parameters
        for (Map.Entry<String, Object> entry : workItem.getParameters().entrySet()) {
            inputParameters.put(entry.getKey(), entry.getValue());
        }
        
        //do not complete the work item handler -> Asynchronous behavior.
    }

    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
    }
    
    /**
     * Completes the Work Item this handler makes reference to.
     * @param results 
     */
    public void completeWorkItem(Map<String, Object> results){
        this.manager.completeWorkItem(workItemId, results);
    }

    public Map<String, Object> getInputParameters() {
        return inputParameters;
    }
    
    public Object getInputParameter(String parameterName) {
        return inputParameters.get(parameterName);
    }

}
