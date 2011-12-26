package org.jbpm.examples.chapter02.simpleGOP.execution;

import org.jbpm.examples.chapter02.simpleGOP.definition.Node;

/**
 *
 * @author salaboy
 */
public class Token {
    private Node currentNode;

    public Node getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(Node currentNode) {
        this.currentNode = currentNode;
    }
    
}
