/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.dev.guide.executor.callbacks;

import com.salaboy.jbpm5.dev.guide.executor.CommandContext;
import com.salaboy.jbpm5.dev.guide.executor.CommandCallback;
import com.salaboy.jbpm5.dev.guide.executor.ExecutionResults;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.drools.runtime.StatefulKnowledgeSession;

/**
 *
 * @author salaboy
 */
public class CompleteWorkItemWithPersistenceCallback implements CommandCallback {

    public void onCommandDone(CommandContext ctx, ExecutionResults results) {
        Map<String, Object> output = new HashMap<String, Object>();
        if (results != null) {
            for (Map.Entry<String, Object> entry : results.getData().entrySet()) {
                output.put(entry.getKey(), entry.getValue());
            }
        }
        String sWorkItemId = (String) ctx.getData("_workItemId");
        String businessKey = (String) ctx.getData("businessKey");
        System.out.println(" >>> WorkItemId = "+sWorkItemId + " - key = "+businessKey);
        String[] key = businessKey.split("@");
        System.out.println("Key[1]="+key[1]);
        StatefulKnowledgeSession session = null; // use the JPAKnowledgeService with the service session 
        // to reload the session
        System.out.println(" >>> session = "+session);
        session.getWorkItemManager().completeWorkItem(Long.valueOf(sWorkItemId), output);
    }
}
