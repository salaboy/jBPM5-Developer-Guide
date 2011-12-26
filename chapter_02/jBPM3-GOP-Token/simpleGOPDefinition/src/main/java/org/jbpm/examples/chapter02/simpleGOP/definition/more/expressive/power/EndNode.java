package org.jbpm.examples.chapter02.simpleGOP.definition.more.expressive.power;

import java.util.Iterator;
import org.jbpm.examples.chapter02.simpleGOP.definition.Node;
import org.jbpm.examples.chapter02.simpleGOP.definition.Transition;

/**
 *
 * @author salaboy
 */
public class EndNode extends Node {

    public EndNode(String name) {
        super(name);
    }

    
    @Override
    public void graph(){
        String padding="";
        String token="-";
        for(int i=0; i < this.getName().length(); i++){
            padding+=token;
        }
        System.out.println("+----------.");
        System.out.println("|<END NODE>|");
        System.out.println("+---"+padding+"---.");
        System.out.println("|   "+this.getName()+"   |");
        System.out.println("+---"+padding+"---+");

        Iterator<Transition> transitionIt =  getTransitions().values().iterator();
        while(transitionIt.hasNext()){
            transitionIt.next().graph();
        }
    }
}
