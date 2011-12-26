package org.jbpm.examples.chapter02.simpleGOP.definition;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author salaboy
 */
public class Definition implements Graphicable, NodeContainer {
    private List<Node> nodes;
    private String name;

    public Definition(String name) {
        this.name = name;
    }

    
    public void graph(){
        for (Node node : nodes){
            node.graph();
        }

    }

    public void addNode(Node node) {
        if(nodes == null){
            nodes = new ArrayList<Node>();
        }
        nodes.add(node);
    }

    public Node getNode(Long id) {
        for(Node node : nodes){
            if(node.getId() == id){
                return node;
            }
        }
        return null;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }
}
