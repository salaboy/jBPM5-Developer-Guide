package org.jbpm.examples.chapter02.simpleGOP.definition.more.expressive.power;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import org.jbpm.examples.chapter02.simpleGOP.definition.Node;
import org.jbpm.examples.chapter02.simpleGOP.definition.Transition;
import org.jbpm.examples.chapter02.simpleGOP.execution.Execution;
import org.jbpm.examples.chapter02.simpleGOP.execution.Token;

/**
 *
 * @author salaboy
 */
public class StartNode extends Node {

    public StartNode(String name) {
        super(name);
    }
    @Override
    public void graph(){
        String padding="";
        String token="-";
        for(int i=0; i < this.getName().length(); i++){
            padding+=token;
        }
        System.out.println("+------------.");
        System.out.println("|<START NODE>|");
        System.out.println("+---"+padding+"---.");
        System.out.println("|   "+this.getName()+"   |");
        System.out.println("+---"+padding+"---+");

        Iterator<Transition> transitionIt =  getTransitions().values().iterator();
        while(transitionIt.hasNext()){
            transitionIt.next().graph();
        }
    }

    @Override
    public void leave(Token token) {
        System.out.println("Starting process at "+new Date());
        //Watch out here.. we are duplicating code!!!
        System.out.println("Leaving "+this.getName());
        Collection<Transition> transitions = getLeavingTransitions().values();
        Iterator<Transition> it = transitions.iterator();
        if(it.hasNext()){
            it.next().take(token);
        }
    }
//    There is no need to override this node, the start node never get executed
//    @Override
//    public void execute(Execution execution) {
//        super.execute(execution);
//    }



}
