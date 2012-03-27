/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.dev.guide.callbacks;

import com.salaboy.jbpm5.dev.guide.executor.CommandContext;
import com.salaboy.jbpm5.dev.guide.executor.CommandCallback;
import com.salaboy.jbpm5.dev.guide.executor.ExecutionResults;

/**
 *
 * @author salaboy
 */
public class PrintResultsCallback implements CommandCallback{

    public void onCommandDone(CommandContext ctx, ExecutionResults results) {
        String methodName = (String) ctx.getData("methodName");
        String outputName = (String) ctx.getData("outputName");
        System.out.println(">>> invoked " + methodName + ". Result: " + results.getData(outputName));
    }
    
}
