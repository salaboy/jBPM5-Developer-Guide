/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5;

/**
 *
 * @author salaboy
 */
public class OnExitNodeEvent implements NodeEvent{
    private String nodeName;
    private String processInstanceId;

    public OnExitNodeEvent() {
    }

    
    public OnExitNodeEvent(String nodeName, String processInstanceId) {
        this.nodeName = nodeName;
        this.processInstanceId = processInstanceId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Override
    public String toString() {
        return "OnExitNodeEvent{" + "nodeName=" + nodeName + ", processInstanceId=" + processInstanceId + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OnExitNodeEvent other = (OnExitNodeEvent) obj;
        if ((this.nodeName == null) ? (other.nodeName != null) : !this.nodeName.equals(other.nodeName)) {
            return false;
        }
        if ((this.processInstanceId == null) ? (other.processInstanceId != null) : !this.processInstanceId.equals(other.processInstanceId)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.nodeName != null ? this.nodeName.hashCode() : 0);
        hash = 97 * hash + (this.processInstanceId != null ? this.processInstanceId.hashCode() : 0);
        return hash;
    }
    
    
}
