/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.salaboy.jbpm5.dev.guide.executor.commands;

import com.salaboy.jbpm5.dev.guide.executor.Command;
import com.salaboy.jbpm5.dev.guide.executor.CommandContext;

/**
 *
 * @author salaboy
 */
public class PrintOutCommand implements Command{

	public void setContext(CommandContext ctx) {
	}
	
    public void execute() {
        System.out.println(">>> Hi This is the first command!");
    }
    
}
