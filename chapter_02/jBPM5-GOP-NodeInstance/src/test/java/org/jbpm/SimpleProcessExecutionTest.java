/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package org.jbpm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.Assert;
import org.jbpm.api.*;
import org.jbpm.factories.ProcessInstanceFactory;
import org.jbpm.api.impl.ProcessDefinitionImpl;
import org.jbpm.api.impl.ProcessInstanceImpl.STATUS;
import org.jbpm.tasks.impl.ActionTask;
import org.jbpm.tasks.impl.EndEvent;
import org.jbpm.tasks.impl.SequenceFlowImpl;
import org.jbpm.tasks.impl.StartEvent;
import org.jbpm.tasks.services.ProcessEventSupportService;
import org.jbpm.tasks.services.Service;
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
        ProcessDefinition process = new ProcessDefinitionImpl();
        StartEvent startEvent = new StartEvent();
        process.addTask(0L, startEvent);
        ActionTask actionTask = new ActionTask(new Action() {

            @Override
            public void execute() {
                System.out.println("Executing the Action!!");
            }
        });
        process.addTask(1L, actionTask);
        EndEvent endEvent = new EndEvent();
        process.addTask(2L, endEvent);
        startEvent.addOutgoingFlow(SequenceFlow.FLOW_DEFAULT_TYPE, new SequenceFlowImpl(SequenceFlow.FLOW_DEFAULT_TYPE, actionTask));
        actionTask.addOutgoingFlow(SequenceFlow.FLOW_DEFAULT_TYPE, new SequenceFlowImpl(SequenceFlow.FLOW_DEFAULT_TYPE, endEvent));
        return process;
    }
}
