/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.salaboy.jbpm5.dev.guide.executor.commands;

import com.salaboy.jbpm5.dev.guide.executor.Command;

/**
 *
 * @author salaboy
 */
public class PrintOutCommand implements Command{

    public void execute() {
        System.out.println(">>> Hi This is the first command!");
    }
    
}
