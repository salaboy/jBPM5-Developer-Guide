/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.sessions.patterns;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author salaboy
 */
public class SessionLocator implements Serializable{
    private int sessionId;
    private String sessionName;
    private Map<String, String> props = new HashMap<String, String>();

    public SessionLocator() {
    }

    public SessionLocator(int sessionId, String sessionName) {
        this.sessionId = sessionId;
        this.sessionName = sessionName;
    }
    
    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public Map<String, String> getProps() {
        return props;
    }

    public void setProps(Map<String, String> props) {
        this.props = props;
    }

    @Override
    public String toString() {
        return "SessionLocator{" + "sessionId=" + sessionId + ", sessionName=" + sessionName + ", props=" + props + '}';
    }
    
    
}
