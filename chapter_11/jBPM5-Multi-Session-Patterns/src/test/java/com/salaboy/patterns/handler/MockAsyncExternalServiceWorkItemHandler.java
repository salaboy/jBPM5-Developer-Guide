package com.salaboy.patterns.handler;

import com.salaboy.sessions.patterns.BusinessEntity;
import java.util.UUID;
import javax.persistence.EntityManager;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;

/**
 *
 * @author esteban
 */
public class MockAsyncExternalServiceWorkItemHandler implements WorkItemHandler {

    private int sessionId;
    private String businessKey;
    private EntityManager em;

    public MockAsyncExternalServiceWorkItemHandler(EntityManager em, int sessionId, String businessKey) {
        this.em = em;
        this.sessionId = sessionId;
        this.businessKey = businessKey;
    }

    public void executeWorkItem(WorkItem wi, WorkItemManager wim) {
        long workItemId = wi.getId();
        long processInstanceId = wi.getProcessInstanceId();
        if (businessKey == null || businessKey.equals("")) {
            //If we don't want to set the business key, the external system can
            // give us an interaction reference that can be used later to
            // complete this work item
            businessKey = UUID.randomUUID().toString();
        }
        BusinessEntity businessEntity = new BusinessEntity(sessionId, processInstanceId, workItemId, businessKey);
        System.out.println(">>> Working in an External Interaction for: "+businessKey);
        System.out.println(" ### : Persisting: " + businessEntity.toString());
        em.joinTransaction(); // I'm forced to join the transaction here, because I'm working inside the session persistence
        em.persist(businessEntity);
    }

    public void abortWorkItem(WorkItem wi, WorkItemManager wim) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
