package org.jbpm.examples.chapter02.simpleGOP.definition;

/**
 *
 * @author salaboy
 */
public interface NodeContainer {
    public void addNode(Node node);
    public Node getNode(Long id);
}
