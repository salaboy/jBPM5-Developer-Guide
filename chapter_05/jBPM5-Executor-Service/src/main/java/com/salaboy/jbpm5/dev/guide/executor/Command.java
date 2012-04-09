/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.salaboy.jbpm5.dev.guide.executor;

/**
 *
 * @author salaboy
 */
public interface Command {
    public ExecutionResults execute(CommandContext ctx) throws Exception;
}
