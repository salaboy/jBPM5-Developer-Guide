/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.salaboy.process.engine.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.salaboy.process.engine.structures.*;
import com.salaboy.process.engine.factories.ProcessInstanceFactory;
import com.salaboy.process.engine.structures.impl.ProcessDefinitionImpl;
import com.salaboy.process.engine.tasks.impl.EndTask;
import com.salaboy.process.engine.tasks.impl.SequenceFlowImpl;
import com.salaboy.process.engine.tasks.impl.StartTask;
import com.salaboy.process.engine.services.ProcessEventSupportService;
import com.salaboy.process.engine.services.Service;
import com.salaboy.process.engine.structures.ProcessInstance.STATUS;
import com.salaboy.process.engine.tasks.impl.ScriptTask;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author salaboy
 */
public class SimpleProcessExecutionTest {

    public SimpleProcessExecutionTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void simpleProcessExecution() {
        ProcessDefinition process = createProcessDefinition();

        ProcessInstance processInstance = ProcessInstanceFactory.newProcessInstance(process);

        assertEquals(processInstance.getStatus(), STATUS.CREATED);

        processInstance.start();

        assertEquals(processInstance.getStatus(), STATUS.ENDED);


    }

    @Test
    public void simpleProcessExecutionWithCustomEventListener() {
        final List<String> taskExecuted = new ArrayList<String>();
        Map<String, Service> services = new HashMap<String, Service>();
        
        services.put("event-service", new ProcessEventSupportService() {

            public void fireBeforeTaskTriggered(NodeInstance task) {
                taskExecuted.add(task.getTask().getName());
            }

            public void fireAfterTaskTriggered(NodeInstance task) {
            }

            public void fireBeforeTaskLeft(NodeInstance task) {
            }

            public void fireAfterTaskLeft(NodeInstance task) {
            }
        });

        ProcessDefinition process = createProcessDefinition();

        ProcessInstance processInstance = ProcessInstanceFactory.newProcessInstance(process, services);

        assertEquals(processInstance.getStatus(), STATUS.CREATED);

        processInstance.start();

        assertEquals(3, taskExecuted.size());

        assertEquals(processInstance.getStatus(), STATUS.ENDED);


    }
    
    @Test
    public void simpleProcessExecutionWithProcessVariables() {
        final List<String> taskExecuted = new ArrayList<String>();
        Map<String, Service> services = new HashMap<String, Service>();
        
        services.put("event-service", new ProcessEventSupportService() {

            public void fireBeforeTaskTriggered(NodeInstance node) {
                
                taskExecuted.add(node.getTask().getName());
            }

            public void fireAfterTaskTriggered(NodeInstance node) {
            }

            public void fireBeforeTaskLeft(NodeInstance node) {
            }

            public void fireAfterTaskLeft(NodeInstance node) {
            }
        });

        ProcessDefinition process = createProcessDefinition();

        
        ProcessInstance processInstance = ProcessInstanceFactory.newProcessInstance(process, services);

        assertEquals(processInstance.getStatus(), STATUS.CREATED);
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("MyProcessVariable", new Object());
        processInstance.start(variables);

        assertEquals(3, taskExecuted.size());

        assertEquals(processInstance.getStatus(), STATUS.ENDED);


    }

    private ProcessDefinition createProcessDefinition() {
        // Process Definition
        ProcessDefinition process = new ProcessDefinitionImpl();
        
        //Start Task
        StartTask startTask = new StartTask();
        process.addTask(0L, startTask);
        
        //Action Task
        ScriptTask scriptTask = new ScriptTask("java", new Action() {

            @Override
            public void execute() {
                System.out.println("Executing the Action!!");
            }
        });
        process.addTask(1L, scriptTask);
        
        //End Task
        EndTask endTask = new EndTask();
        process.addTask(2L, endTask);
        
        //Adding the connections
        startTask.addOutgoingFlow(SequenceFlow.FLOW_DEFAULT_TYPE, new SequenceFlowImpl(SequenceFlow.FLOW_DEFAULT_TYPE, scriptTask));
        scriptTask.addOutgoingFlow(SequenceFlow.FLOW_DEFAULT_TYPE, new SequenceFlowImpl(SequenceFlow.FLOW_DEFAULT_TYPE, endTask));
        return process;
    }
}
