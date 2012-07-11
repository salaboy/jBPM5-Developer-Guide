/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.dev.guide.callbacks;

import com.salaboy.jbpm5.dev.guide.SessionStoreUtil;
import com.salaboy.jbpm5.dev.guide.executor.CommandContext;
import com.salaboy.jbpm5.dev.guide.executor.CommandCallback;
import com.salaboy.jbpm5.dev.guide.executor.ExecutionResults;
import java.util.HashMap;
import java.util.Map;
import org.drools.runtime.StatefulKnowledgeSession;

/**
 *
 * @author salaboy
 */
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
        StatefulKnowledgeSession session = SessionStoreUtil.sessionCache.get(key[1]);
        session.getWorkItemManager().completeWorkItem(Long.valueOf(sWorkItemId), output);
    }
}
