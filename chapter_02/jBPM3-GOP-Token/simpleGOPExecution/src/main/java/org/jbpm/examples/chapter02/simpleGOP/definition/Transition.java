package org.jbpm.examples.chapter02.simpleGOP.definition;

import org.jbpm.examples.chapter02.simpleGOP.execution.Token;

/**
 *
 * @author salaboy
 */
public class Transition implements Graphicable{
    private Node source;
    private Node destination;
    private String label;

  public Transition(String label,Node source, Node destination) {
    this.source = source;
    this.destination = destination;
    this.label = label;
  }

  public void take(Token token){
    System.out.println("Taking transition to "+getDestination().getName());
    this.getDestination().enter(token);

  }

    /**
     * @return the destination
     */
    public Node getDestination() {
        return destination;
    }

    /**
     * @param destination the destination to set
     */
    public void setDestination(Node destination) {
        this.destination = destination;
    }

    /**
     * @return the source
     */
    public Node getSource() {
        return source;
    }

    /**
     * @param source
     */
    public void setSource(Node source) {
        this.source = source;
    }

     /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    public void graph() {
        System.out.println("        |");
        System.out.println("        |");
        System.out.println("        |  "+this.getLabel());
        System.out.println("        |");
        System.out.println("        \u25bc");

    }
}
