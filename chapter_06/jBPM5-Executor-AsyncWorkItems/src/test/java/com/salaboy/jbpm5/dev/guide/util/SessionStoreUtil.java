/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.dev.guide.util;

import java.util.HashMap;
import java.util.Map;
import org.drools.runtime.StatefulKnowledgeSession;

/**
 *
 * @author salaboy
 */
public class SessionStoreUtil {
    public final static Map<String, StatefulKnowledgeSession> sessionCache = new HashMap<String, StatefulKnowledgeSession>();
    
    
    public static void clean(){
        sessionCache.clear();
    }
}
