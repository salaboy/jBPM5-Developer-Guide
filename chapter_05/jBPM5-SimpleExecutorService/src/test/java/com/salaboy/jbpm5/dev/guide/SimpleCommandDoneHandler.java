/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.dev.guide;

import com.salaboy.jbpm5.dev.guide.executor.CommandContext;
import com.salaboy.jbpm5.dev.guide.executor.CommandDoneHandler;
import com.salaboy.jbpm5.dev.guide.executor.ExecutionResults;

/**
 *
 * @author salaboy
 */
public class SimpleCommandDoneHandler implements CommandDoneHandler{

    public void onCommandDone(CommandContext ctx, ExecutionResults results) {
        System.out.println(" ??? Business key: "+ctx.getData("key"));
        Object get = ExecutorSimpleTest.cachedEntities.get(ctx.getData("key"));
        System.out.println("GET = "+get);
        System.out.println(" ??? Command Done!");
        System.out.println(" ??? Command Full Context = "+ctx);
        System.out.println(" ??? Command Full Results = "+results);
    }
    
}
