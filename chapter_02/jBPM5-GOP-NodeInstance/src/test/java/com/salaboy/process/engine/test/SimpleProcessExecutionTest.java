/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.salaboy.process.engine.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.Assert;
import com.salaboy.process.engine.structures.*;
import com.salaboy.process.engine.factories.ProcessInstanceFactory;
import com.salaboy.process.engine.structures.impl.ProcessDefinitionImpl;
import com.salaboy.process.engine.structures.impl.ProcessInstanceImpl.STATUS;
import com.salaboy.process.engine.tasks.impl.EndEvent;
import com.salaboy.process.engine.tasks.impl.SequenceFlowImpl;
import com.salaboy.process.engine.tasks.impl.StartEvent;
import com.salaboy.process.engine.services.ProcessEventSupportService;
import com.salaboy.process.engine.services.Service;
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

        Assert.assertEquals(processInstance.getStatus(), STATUS.ENDED);


    }

    @Test
    public void simpleProcessExecutionWithCustomEventListener() {
        final List<String> taskExecuted = new ArrayList<String>();
        Map<String, Service> services = new HashMap<String, Service>();
        
        services.put("event-service", new ProcessEventSupportService() {

            public void fireBeforeTaskTriggered(TaskInstance task) {
                taskExecuted.add(task.getTask().getName());
            }

            public void fireAfterTaskTriggered(TaskInstance task) {
            }

            public void fireBeforeTaskLeft(TaskInstance task) {
            }

            public void fireAfterTaskLeft(TaskInstance task) {
            }
        });

        ProcessDefinition process = createProcessDefinition();

        ProcessInstance processInstance = ProcessInstanceFactory.newProcessInstance(process, services);

        assertEquals(processInstance.getStatus(), STATUS.CREATED);

        processInstance.start();

        assertEquals(3, taskExecuted.size());

        assertEquals(processInstance.getStatus(), STATUS.ENDED);


    }

    private ProcessDefinition createProcessDefinition() {
        // Process Definition
        ProcessDefinition process = new ProcessDefinitionImpl();
        
        //Start Event
        StartEvent startEvent = new StartEvent();
        process.addTask(0L, startEvent);
        
        //Action Task
        ScriptTask scriptTask = new ScriptTask("java", new Action() {

            @Override
            public void execute() {
                System.out.println("Executing the Action!!");
            }
        });
        process.addTask(1L, scriptTask);
        
        //End Event
        EndEvent endEvent = new EndEvent();
        process.addTask(2L, endEvent);
        
        //Adding the connections
        startEvent.addOutgoingFlow(SequenceFlow.FLOW_DEFAULT_TYPE, new SequenceFlowImpl(SequenceFlow.FLOW_DEFAULT_TYPE, scriptTask));
        scriptTask.addOutgoingFlow(SequenceFlow.FLOW_DEFAULT_TYPE, new SequenceFlowImpl(SequenceFlow.FLOW_DEFAULT_TYPE, endEvent));
        return process;
    }
}
