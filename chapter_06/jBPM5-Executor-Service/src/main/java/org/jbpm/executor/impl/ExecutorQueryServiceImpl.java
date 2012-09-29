/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jbpm.executor.impl;

import java.util.List;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import org.jbpm.executor.api.ExecutorQueryService;
import org.jbpm.executor.entities.ErrorInfo;
import org.jbpm.executor.entities.RequestInfo;
import org.jboss.seam.transaction.Transactional;

/**
 *
 * @author salaboy
 */
@Transactional
public class ExecutorQueryServiceImpl implements ExecutorQueryService{
    @Inject 
    private EntityManager em;

    public ExecutorQueryServiceImpl() {
    }
    
    public List<RequestInfo> getQueuedRequests() {
        List resultList = em.createNamedQuery("QueuedRequests").getResultList();
        return resultList;
    }

    public List<RequestInfo> getExecutedRequests() {
        List resultList = em.createNamedQuery("ExecutedRequests").getResultList();
        return resultList;
    }

    public List<RequestInfo> getInErrorRequests() {
        List resultList = em.createNamedQuery("InErrorRequests").getResultList();
        return resultList;
    }

    public List<RequestInfo> getCancelledRequests() {
        List resultList = em.createNamedQuery("CancelledRequests").getResultList();
        return resultList;
    }

    public List<ErrorInfo> getAllErrors() {
        List resultList = em.createNamedQuery("GetAllErrors").getResultList();
        return resultList;
    }
    
    public List<RequestInfo> getAllRequests() {
        List resultList = em.createNamedQuery("GetAllRequests").getResultList();
        return resultList;
    }
}
