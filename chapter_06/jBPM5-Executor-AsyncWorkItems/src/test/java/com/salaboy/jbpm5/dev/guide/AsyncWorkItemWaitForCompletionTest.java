/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.salaboy.jbpm5.dev.guide;

import com.salaboy.jbpm5.dev.guide.callbacks.PrintResultsCallback;
import com.salaboy.jbpm5.dev.guide.callbacks.CompleteWorkItemCallback;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;


import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.WorkingMemory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.event.RuleFlowGroupActivatedEvent;
import org.drools.event.RuleFlowGroupDeactivatedEvent;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.io.impl.ClassPathResource;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkflowProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.salaboy.jbpm5.dev.guide.commands.CheckInCommand;
import com.salaboy.jbpm5.dev.guide.executor.Executor;
import com.salaboy.jbpm5.dev.guide.executor.ExecutorImpl;

import com.salaboy.jbpm5.dev.guide.executor.wih.AsyncGenericWorkItemHandler;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Server;
import org.junit.Ignore;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author salaboy
 */
public class AsyncWorkItemWaitForCompletionTest {

    protected Executor executor;
    protected StatefulKnowledgeSession session;
    protected ApplicationContext ctx;
    private Server server;

    public AsyncWorkItemWaitForCompletionTest() {
    }

    @Before
    public void setUp() throws Exception {
        DeleteDbFiles.execute("~", "mydb", false);

        try {

            server = Server.createTcpServer(new String[]{"-tcp", "-tcpAllowOthers", "-tcpDaemon", "-trace"}).start();
        } catch (SQLException ex) {
            System.out.println("ex: " + ex);
        }
        initializeExecutionEnvironment();
        initializeSession();
    }

    @After
    public void tearDown() {
        executor.destroy();
        server.stop();
    }

    protected void initializeExecutionEnvironment() throws Exception {
        CheckInCommand.reset();
        ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        executor = (Executor) ctx.getBean("executorService");
        executor.init();
    }

    

    private void initializeSession() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(new ClassPathResource("async-work-item-wait.bpmn"), ResourceType.BPMN2);
        if (kbuilder.hasErrors()) {
            KnowledgeBuilderErrors errors = kbuilder.getErrors();

            for (KnowledgeBuilderError error : errors) {
                System.out.println(">>> Error:" + error.getMessage());

            }
            throw new IllegalStateException(">>> Knowledge couldn't be parsed! ");
        }

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        session = kbase.newStatefulKnowledgeSession();
        KnowledgeRuntimeLoggerFactory.newConsoleLogger(session);

        ((StatefulKnowledgeSessionImpl) session).session.addEventListener(new org.drools.event.AgendaEventListener() {

            public void activationCreated(org.drools.event.ActivationCreatedEvent event, WorkingMemory workingMemory) {
            }

            public void activationCancelled(org.drools.event.ActivationCancelledEvent event, WorkingMemory workingMemory) {
            }

            public void beforeActivationFired(org.drools.event.BeforeActivationFiredEvent event, WorkingMemory workingMemory) {
            }

            public void afterActivationFired(org.drools.event.AfterActivationFiredEvent event, WorkingMemory workingMemory) {
            }

            public void agendaGroupPopped(org.drools.event.AgendaGroupPoppedEvent event, WorkingMemory workingMemory) {
            }

            public void agendaGroupPushed(org.drools.event.AgendaGroupPushedEvent event, WorkingMemory workingMemory) {
            }

            public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event, WorkingMemory workingMemory) {
            }

            public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event, WorkingMemory workingMemory) {
                workingMemory.fireAllRules();
            }

            public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event, WorkingMemory workingMemory) {
            }

            public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event, WorkingMemory workingMemory) {
            }
        });


    }

    @Test
    @Ignore
    public void executorCheckInTestFinishesWithoutHandler() throws InterruptedException {
        HashMap<String, Object> input = new HashMap<String, Object>();

        String patientName = "John Doe";
        input.put("bedrequest_patientname", patientName);

        
        List<String> callbacks = new ArrayList<String>();
        callbacks.add(PrintResultsCallback.class.getCanonicalName());
        AsyncGenericWorkItemHandler asyncHandler = new AsyncGenericWorkItemHandler(executor, session.getId());
        session.getWorkItemManager().registerWorkItemHandler("Async Work", asyncHandler);

        WorkflowProcessInstance pI = (WorkflowProcessInstance) session.startProcess("PatientCheckIn", input);

        assertEquals(ProcessInstance.STATE_ACTIVE, pI.getState());

        assertEquals(0, CheckInCommand.getCheckInCount());

        Thread.sleep(((ExecutorImpl) executor).getWaitTime() + 1000);

        assertEquals(1, CheckInCommand.getCheckInCount());
    }

    @Test
    public void executorCheckInTestFinishesWithHandler() throws InterruptedException {
        HashMap<String, Object> input = new HashMap<String, Object>();

        String patientName = "John Doe";
        input.put("bedrequest_patientname", patientName);

        SessionStoreUtil.sessionCache.put("sessionId="+session.getId(), session);
        
        AsyncGenericWorkItemHandler asyncHandler = new AsyncGenericWorkItemHandler(executor, session.getId());
        session.getWorkItemManager().registerWorkItemHandler("Async Work", asyncHandler);

        WorkflowProcessInstance pI = (WorkflowProcessInstance) session.startProcess("PatientCheckIn", input);

        assertEquals(ProcessInstance.STATE_ACTIVE, pI.getState());

        assertEquals(0, CheckInCommand.getCheckInCount());

        Thread.sleep(((ExecutorImpl) executor).getWaitTime() * 2);

        assertEquals(1, CheckInCommand.getCheckInCount());

        assertEquals(ProcessInstance.STATE_COMPLETED, pI.getState());
        
        SessionStoreUtil.clean();
    }

    @Test
    @Ignore
    public void executorCheckInTestStoppedBefore() throws InterruptedException {
        HashMap<String, Object> input = new HashMap<String, Object>();

        String patientName = "John Doe";
        input.put("bedrequest_patientname", patientName);

        List<String> callbacks = new ArrayList<String>();
        callbacks.add(PrintResultsCallback.class.getCanonicalName());
        AsyncGenericWorkItemHandler asyncHandler = new AsyncGenericWorkItemHandler(executor, session.getId());
        session.getWorkItemManager().registerWorkItemHandler("Async Work", asyncHandler);

        WorkflowProcessInstance pI = (WorkflowProcessInstance) session.startProcess("PatientCheckIn", input);

        assertEquals(ProcessInstance.STATE_ACTIVE, pI.getState());

        assertEquals(0, CheckInCommand.getCheckInCount());

        Thread.sleep(((ExecutorImpl) executor).getWaitTime() - 1000);

        assertEquals(0, CheckInCommand.getCheckInCount());

        Thread.sleep(1500);

        assertEquals(1, CheckInCommand.getCheckInCount());
    }
}
