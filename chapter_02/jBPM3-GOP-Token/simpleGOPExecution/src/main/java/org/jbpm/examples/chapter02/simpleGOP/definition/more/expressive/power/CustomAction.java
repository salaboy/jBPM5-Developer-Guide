package org.jbpm.examples.chapter02.simpleGOP.definition.more.expressive.power;

import org.jbpm.examples.chapter02.simpleGOP.execution.Action;
import org.jbpm.examples.chapter02.simpleGOP.execution.Token;

/**
 *
 * @author salaboy
 */
public class CustomAction implements Action{
    private String name;

    public CustomAction(String name) {
        this.name = name;
    }

    public void execute(Token token) {
        System.out.println("executing Custom Action "+name+" !!");

    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

}
