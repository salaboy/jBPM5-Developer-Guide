/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.salaboy.jbpm5.dev.guide;

import com.salaboy.jbpm5.dev.guide.executor.Command;
import com.salaboy.jbpm5.dev.guide.executor.CommandContext;
import com.salaboy.jbpm5.dev.guide.executor.ExecutionResults;

/**
 *
 * @author salaboy
 */
public class ThrowExceptionCommand implements Command{

    public ExecutionResults execute(CommandContext ctx) {
        System.out.println(">>> Hi This is the Exception command!");
        throw new RuntimeException("Test Exception!");        
    }
    
}
