package org.jbpm.examples.chapter02.simpleGOP.execution;

import org.jbpm.examples.chapter02.simpleGOP.definition.Definition;
import org.jbpm.examples.chapter02.simpleGOP.definition.Node;

/**
 *
 * @author salaboy
 */
public class Execution {
    private Definition definition;
    private Token mainToken;



    public Execution(Definition definition) {
        this.definition = definition;
        this.mainToken = new Token();
        //Setting the first Node as the current Node

        this.getMainToken().setCurrentNode(definition.getNodes().get(0));
    }

    public void start(){
        //Here we send the execution pointer to the node, so all the nodes could change
        //the current node pointer when each node get executed. This could vary from
        //one implementation to another. Because, sometimes there is no need to change
        //the current node until some wait state is reached.
        this.getMainToken().getCurrentNode().leave(mainToken);
    }


    /**
     * @return the definition
     */
    public Definition getDefinition() {
        return definition;
    }

    /**
     * @param definition the definition to set
     */
    public void setDefinition(Definition definition) {
        this.definition = definition;
    }

    
    public Token getMainToken() {
        return mainToken;
    }

    public void setMainToken(Token mainToken) {
        this.mainToken = mainToken;
    }


}
