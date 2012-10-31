/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.patterns.handler;

import com.salaboy.sessions.patterns.BusinessEntity;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
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
    private EntityManagerFactory emf;

    public MockAsyncExternalServiceWorkItemHandler(EntityManagerFactory emf, int sessionId, String businessKey) {
        this.emf = emf;
        this.sessionId = sessionId;
        this.businessKey = businessKey;
    }

    public void executeWorkItem(WorkItem wi, WorkItemManager wim) {
        System.out.println(">>> Working in an External Interaction");
        long workItemId = wi.getId();
        long processInstanceId = wi.getProcessInstanceId();
        EntityManager em = emf.createEntityManager();
        if (businessKey == null || businessKey.equals("")) {
            //If we don't want to set the business key, the external system can
            // give us an interaction reference that can be used later to
            // complete this work item
            businessKey = UUID.randomUUID().toString();
        }
        BusinessEntity businessEntity = new BusinessEntity(sessionId, processInstanceId, workItemId, businessKey);
        System.out.println(" ### : Persisting: " + businessEntity.toString());
        em.persist(businessEntity);
        em.close();
    }

    public void abortWorkItem(WorkItem wi, WorkItemManager wim) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
