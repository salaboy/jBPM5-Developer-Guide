/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.dev.guide.callbacks;

import com.salaboy.jbpm5.dev.guide.util.SessionStoreUtil;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Named;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.executor.api.CommandCallback;
import org.jbpm.executor.api.CommandContext;
import org.jbpm.executor.api.ExecutionResults;

/**
 * Completes a work item given its id. The id of the work item is retrieved from
 * the context using the key '_workItemId'. The session used to complete the 
 * work item is get from SessionStoreUtil class using the first section of
 * the business key as id.
 * @author salaboy
 */
@Named
public class CompleteWorkItemCallback implements CommandCallback {

    public void onCommandDone(CommandContext ctx, ExecutionResults results) {
        Map<String, Object> output = new HashMap<String, Object>();
        if (results != null) {
            for (Map.Entry<String, Object> entry : results.getData().entrySet()) {
                output.put(entry.getKey(), entry.getValue());
            }
        }
        String sWorkItemId = (String) ctx.getData("_workItemId");
        String businessKey = (String) ctx.getData("businessKey");
        String[] key = businessKey.split("@");

        StatefulKnowledgeSession session = null;
        synchronized(SessionStoreUtil.sessionCache){
           session = SessionStoreUtil.sessionCache.get(key[1]);
        }
        
        session.getWorkItemManager().completeWorkItem(Long.valueOf(sWorkItemId), output);
    }
}
