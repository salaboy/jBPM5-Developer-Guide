/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.dev.guide.callbacks;

import javax.inject.Named;
import org.jbpm.executor.api.CommandCallback;
import org.jbpm.executor.api.CommandContext;
import org.jbpm.executor.api.ExecutionResults;

/**
 *
 * @author salaboy
 */
@Named
public class PrintResultsCallback implements CommandCallback{

    public void onCommandDone(CommandContext ctx, ExecutionResults results) {
        String methodName = (String) ctx.getData("methodName");
        String outputName = (String) ctx.getData("outputName");
        System.out.println(">>> invoked " + methodName + ". Result: " + results.getData(outputName));
    }
    
}
