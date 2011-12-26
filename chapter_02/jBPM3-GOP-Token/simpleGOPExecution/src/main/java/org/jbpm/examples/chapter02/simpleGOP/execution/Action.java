package org.jbpm.examples.chapter02.simpleGOP.execution;

/**
 *
 * @author salaboy
 */
public interface Action {
    public String getName();
    public void execute(Token token);
}
