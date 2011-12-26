package org.jbpm.examples.chapter02.simpleGOP.definition;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author salaboy
 */
public class Definition implements Graphicable, NodeContainer {
    private String name;
    private List<Node> nodes;
    

    public Definition(String name) {
        this.name = name;
    }

    
    public void graph(){
        for (Node node : getNodes()){
            node.graph();
        }

    }

    public void addNode(Node node) {
        if(getNodes() == null){
            setNodes(new ArrayList<Node>());
        }
        getNodes().add(node);
    }

    public Node getNode(Long id) {
        for(Node node : getNodes()){
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

    /**
     * @return the nodes
     */
    public List<Node> getNodes() {
        return nodes;
    }

    /**
     * @param nodes the nodes to set
     */
    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }
}
