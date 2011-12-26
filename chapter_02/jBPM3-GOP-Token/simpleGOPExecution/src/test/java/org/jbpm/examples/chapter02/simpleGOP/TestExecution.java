package org.jbpm.examples.chapter02.simpleGOP;

import junit.framework.TestCase;
import org.jbpm.examples.chapter02.simpleGOP.definition.Definition;
import org.jbpm.examples.chapter02.simpleGOP.definition.Node;
import org.jbpm.examples.chapter02.simpleGOP.definition.more.expressive.power.ActivityNode;
import org.jbpm.examples.chapter02.simpleGOP.definition.more.expressive.power.CustomAction;
import org.jbpm.examples.chapter02.simpleGOP.definition.more.expressive.power.EndNode;
import org.jbpm.examples.chapter02.simpleGOP.definition.more.expressive.power.StartNode;
import org.jbpm.examples.chapter02.simpleGOP.execution.Execution;

/**
 *
 * @author salaboy
 */
public class TestExecution extends TestCase{



    /**
     * This test will create a Process Definition using just Nodes.
     * Once the Definition is created and all the transitions are correcly
     * attached, the test will create a new Execution using the previously defined
     * Definition.
     */
    public void testSimpleProcessExecution(){
        //Creating Definition
        Definition definition = new Definition("myFirstProcess");
        System.out.println("########################################");
        System.out.println("   Executing PROCESS: "+definition.getName()+"   ");
        System.out.println("########################################");

        //Creating Nodes
        Node firstNode = new Node("First Node");
        Node secondNode = new Node("Second Node");
        Node thirdNode = new Node("Third Node");
        //Adding Transitions
        firstNode.addTransition("to second node", secondNode);
        secondNode.addTransition("to third node", thirdNode);
        //Adding a Custom Action
        secondNode.addAction(new CustomAction("First"));
        //Adding nodes to the Definition
        definition.addNode(firstNode);
        definition.addNode(secondNode);
        definition.addNode(thirdNode);

        //We can graph it if we want.
        //definition.graph();

        //Creating Execution
        Execution execution = new Execution (definition);
        //Starting the Flow
        execution.start();
        //The execution leave the third node
        assertEquals("Third Node", execution.getMainToken().getCurrentNode().getName());

    }
      /**
     * This test will create a Process Definition using the following set of Nodes:
     * StartNode, ActivityNode and EndNode.
     * Once the Definition is created and all the transitions are correcly
     * attached, the test will create a new Execution using the previously defined
     * Definition.
     */
    public void testMoreExpressiveProcessExecution(){
        //Creating Definition
        Definition definition = new Definition("myExpressiveProcess");
        System.out.println("########################################");
        System.out.println("   PROCESS: "+definition.getName()+"   ");
        System.out.println("########################################");
        //Creating Nodes
        StartNode startNode = new StartNode("Process Start");
        ActivityNode activityNode = new ActivityNode("My Activity");
        EndNode endNode = new EndNode("Process End");
        //Adding Transitions
        startNode.addTransition("do the activity", activityNode);
        activityNode.addTransition("ending", endNode);
        //Adding Nodes to the Definition
        definition.addNode(startNode);
        definition.addNode(activityNode);
        definition.addNode(endNode);

        //We can graph it if we want.
        //definition.graph();

        //Creating Execution
        Execution execution = new Execution (definition);
        //Starting the Flow
        execution.start();
        //The execution leave the third node
        assertEquals("Process End", execution.getMainToken().getCurrentNode().getName());
    }
}
