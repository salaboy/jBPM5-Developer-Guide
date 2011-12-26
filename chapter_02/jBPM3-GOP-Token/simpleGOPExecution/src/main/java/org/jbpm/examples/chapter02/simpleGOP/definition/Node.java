package org.jbpm.examples.chapter02.simpleGOP.definition;

import org.jbpm.examples.chapter02.simpleGOP.execution.Action;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.jbpm.examples.chapter02.simpleGOP.execution.Token;

/**
 *
 * @author salaboy
 */
public class Node implements Graphicable{
      
    private Long id;
    private String name;

    private Map<String, Transition> leavingTransitions = new HashMap<String, Transition>();

    private Map<String, Action> actions = new HashMap<String, Action>();

    public Node(String name) {
        this.name = name;
    }

    public void enter(Token token){
        token.setCurrentNode(this);
        System.out.println("Entering "+this.getName());
        execute(token);
    }

    public void execute(Token token){
        System.out.println("Executing "+this.getName());
        if(actions.size() > 0){
            Collection<Action> actionsToExecute = actions.values();
            Iterator<Action> it = actionsToExecute.iterator();
            while(it.hasNext()){
                it.next().execute(token);
            }
            leave(token);
        }else{
            leave(token);
        }
    }
    public void leave(Token token){
        System.out.println("Leaving "+this.getName());
        Collection<Transition> transitions = getLeavingTransitions().values();
        Iterator<Transition> it = transitions.iterator();
        if(it.hasNext()){
            it.next().take(token);
        }
    }
    public void addAction(Action action){
        getActions().put(action.getName(), action);
    }
    public void addTransition(String event, Node destination) {
        getLeavingTransitions().put(event, new Transition(event, this,destination));
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
        return getLeavingTransitions();
    }

    /**
     * @param leavingTransitions the leavingTransitions to set
     */
    public void setTransitions(Map<String, Transition> transitions) {
        this.setLeavingTransitions(transitions);
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

    /**
     * @return the leavingTransitions
     */
    public Map<String, Transition> getLeavingTransitions() {
        return leavingTransitions;
    }

    /**
     * @param leavingTransitions the leavingTransitions to set
     */
    public void setLeavingTransitions(Map<String, Transition> leavingTransitions) {
        this.leavingTransitions = leavingTransitions;
    }

    /**
     * @return the actions
     */
    public Map<String, Action> getActions() {
        return actions;
    }

    /**
     * @param actions the actions to set
     */
    public void setActions(Map<String, Action> actions) {
        this.actions = actions;
    }
}
