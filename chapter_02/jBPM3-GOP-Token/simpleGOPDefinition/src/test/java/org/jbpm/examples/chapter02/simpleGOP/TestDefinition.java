package org.jbpm.examples.chapter02.simpleGOP;

import junit.framework.TestCase;
import org.jbpm.examples.chapter02.simpleGOP.definition.Definition;
import org.jbpm.examples.chapter02.simpleGOP.definition.Node;
import org.jbpm.examples.chapter02.simpleGOP.definition.more.expressive.power.ActivityNode;
import org.jbpm.examples.chapter02.simpleGOP.definition.more.expressive.power.EndNode;
import org.jbpm.examples.chapter02.simpleGOP.definition.more.expressive.power.StartNode;

/**
 *
 * @author salaboy
 */
public class TestDefinition extends TestCase{



    /**
     * This test will create a process definition using just Nodes and Transitions.
     * Once we get the Nodes joined by Transitions, we can graph it.
     */
    public void testSimpleDefinition(){
        //Creating Definition
        Definition definition = new Definition("myFirstProcess");
        System.out.println("########################################");
        System.out.println("   PROCESS: "+definition.getName()+"   ");
        System.out.println("########################################");
        //Creating nodes
        Node firstNode = new Node("First Node");
        Node secondNode = new Node("Second Node");
        Node thirdNode = new Node("Third Node");
        //Adding transitions
        firstNode.addTransition("to second node", secondNode);
        secondNode.addTransition("to third node", thirdNode);
        //Adding Nodes
        definition.addNode(firstNode);
        definition.addNode(secondNode);
        definition.addNode(thirdNode);
        //Graph it!
        definition.graph();
    }
    /**
     *  This test will create a process definition using a new set of nodes:
     *  StartNode, ActivityNode and EndNode. Once we get the defined process
     *  we can graph it.
     */
    public void testMoreExpressiveDefinition(){
        //Creating Definition
        Definition definition = new Definition("myExpressiveProcess");
        System.out.println("########################################");
        System.out.println("   PROCESS: "+definition.getName()+"   ");
        System.out.println("########################################");
        //Creating Nodes
        StartNode startNode = new StartNode("Process Start");
        ActivityNode activityNode = new ActivityNode("My Activity");
        EndNode endNode = new EndNode("Process End");
        //Adding transitions
        startNode.addTransition("do the activity", activityNode);
        activityNode.addTransition("ending", endNode);
        //Adding Nodes to Definition
        definition.addNode(startNode);
        definition.addNode(activityNode);
        definition.addNode(endNode);
        //Graph it!
        definition.graph();
    }
}
