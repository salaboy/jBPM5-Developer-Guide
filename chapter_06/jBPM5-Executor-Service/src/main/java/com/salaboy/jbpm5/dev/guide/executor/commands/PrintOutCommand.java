/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.salaboy.jbpm5.dev.guide.executor.commands;

import com.salaboy.jbpm5.dev.guide.executor.Command;
import com.salaboy.jbpm5.dev.guide.executor.CommandContext;
import com.salaboy.jbpm5.dev.guide.executor.ExecutionResults;

/**
 *
 * @author salaboy
 */
public class PrintOutCommand implements Command{

    public ExecutionResults execute(CommandContext ctx) {
        System.out.println(">>> Hi This is the first command!");
        ExecutionResults executionResults = new ExecutionResults();
        return executionResults;
    }
    
}
