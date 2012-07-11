/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.dev.guide;

import com.salaboy.jbpm5.dev.guide.executor.CommandContext;
import com.salaboy.jbpm5.dev.guide.executor.CommandCallback;
import com.salaboy.jbpm5.dev.guide.executor.ExecutionResults;

/**
 *
 * @author salaboy
 */
public class SimpleIncrementCallback implements CommandCallback{

    public void onCommandDone(CommandContext ctx, ExecutionResults results) {
        String businessKey = (String)ctx.getData("businessKey");
        Long increment = (Long)ExecutorSimpleTest.cachedEntities.get(businessKey);
        System.out.println(" >>> Before Incrementing = "+increment);
        ExecutorSimpleTest.cachedEntities.put(businessKey, increment + 1);
        System.out.println(" >>> After Incrementing = "+ExecutorSimpleTest.cachedEntities.get(businessKey));
        
    }  
}
