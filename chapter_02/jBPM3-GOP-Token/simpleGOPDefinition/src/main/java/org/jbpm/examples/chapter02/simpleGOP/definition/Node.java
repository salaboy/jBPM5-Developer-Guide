package org.jbpm.examples.chapter02.simpleGOP.definition;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author salaboy
 */
public class Node implements Graphicable{
    private Long id;
    private String name;
    private Map<String, Transition> leavingTransitions = new HashMap<String, Transition>();

    public Node(String name) {
        this.name = name;
    }
    public void addTransition(String label, Node destination) {
        leavingTransitions.put(label, new Transition(label, this,destination));
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
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

    /**
     * @return the leavingTransitions
     */
    public Map<String, Transition> getTransitions() {
        return leavingTransitions;
    }

    /**
     * @param leavingTransitions the leavingTransitions to set
     */
    public void setTransitions(Map<String, Transition> transitions) {
        this.leavingTransitions = transitions;
    }

    public void graph() {
        String padding="";
        String token="-";
        for(int i=0; i < this.getName().length(); i++){
            padding+=token;
        }
        System.out.println("+---"+padding+"---+");
        System.out.println("|   "+this.getName()+"   |");
        System.out.println("+---"+padding+"---+");

        Iterator<Transition> transitionIt =  getTransitions().values().iterator();
        while(transitionIt.hasNext()){
            transitionIt.next().graph();
        }

    }
}
