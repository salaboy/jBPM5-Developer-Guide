package org.jbpm.examples.chapter02.simpleGOP.definition.more.expressive.power;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jbpm.examples.chapter02.simpleGOP.definition.Node;
import org.jbpm.examples.chapter02.simpleGOP.definition.Transition;
import org.jbpm.examples.chapter02.simpleGOP.execution.Token;

/**
 *
 * @author salaboy
 */
public class ActivityNode extends Node {

    public ActivityNode(String name) {
        super(name);
    }

    @Override
    public void graph(){
        String padding="";
        String token="-";
        for(int i=0; i < this.getName().length(); i++){
            padding+=token;
        }
        System.out.println("+---------------.");
        System.out.println("|<ACTIVITY NODE>|");
        System.out.println("+---"+padding+"---.");
        System.out.println("|   "+this.getName()+"   |");
        System.out.println("+---"+padding+"---+");

        Iterator<Transition> transitionIt =  getTransitions().values().iterator();
        while(transitionIt.hasNext()){
            transitionIt.next().graph();
        }
    }

    @Override
    public void execute(Token token) {
        System.out.println("Executing the activity... this could take a while...");
        try {
            Thread.currentThread().sleep(5000);
        } catch (InterruptedException ex) {
            Logger.getLogger(ActivityNode.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Activity Finished");
        leave(token);
    }

}
