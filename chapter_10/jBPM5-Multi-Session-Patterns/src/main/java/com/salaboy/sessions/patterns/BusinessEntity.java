package com.salaboy.sessions.patterns;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author salaboy
 */
@Entity
public class BusinessEntity implements Serializable {

    @Id
    @GeneratedValue
    private Long id;
    private int sessionId;
    private long processId;
    private long workItemId;
    private String businessKey;
    private boolean active = true;
    // Include any other relevant information about the process or session
    // Like for example the stakeholder or information which is unique for
    // this relationship
    public BusinessEntity() {
    }

    public BusinessEntity(int sessionId, long processId, long workItemId, String businessKey) {
        this.sessionId = sessionId;
        this.processId = processId;
        this.workItemId = workItemId;
        this.businessKey = businessKey;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public long getProcessId() {
        return processId;
    }

    public void setProcessId(long processId) {
        this.processId = processId;
    }

    public long getWorkItemId() {
        return workItemId;
    }

    public void setWorkItemId(long workItemId) {
        this.workItemId = workItemId;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "BusinessEntity{" + "id=" + id + ", sessionId=" + sessionId + ", processId=" + processId + ", workItemId=" + workItemId + ", businessKey=" + businessKey + ", active=" + active + '}';
    }

   
    
    
    
}
