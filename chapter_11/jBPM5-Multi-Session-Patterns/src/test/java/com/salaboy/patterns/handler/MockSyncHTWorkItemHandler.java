package com.salaboy.patterns.handler;

import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;

/**
 * Mock implementation of a Synchronous Work Item Handler.
 * @author esteban
 */
public class MockSyncHTWorkItemHandler implements WorkItemHandler {

    public MockSyncHTWorkItemHandler() {
    }

    public void executeWorkItem(WorkItem wi, WorkItemManager wim) {
        System.out.println(">>> Working on a Human Interaction");
        System.out.println(">>> Completing a Human Interaction");
        wim.completeWorkItem(wi.getId(), null);
    }

    public void abortWorkItem(WorkItem wi, WorkItemManager wim) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
