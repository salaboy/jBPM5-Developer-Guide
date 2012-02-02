/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.salaboy.jbpm5.dev.guide;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
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
import com.salaboy.jbpm5.dev.guide.executor.CommandContext;
import com.salaboy.jbpm5.dev.guide.executor.CommandDoneHandler;
import com.salaboy.jbpm5.dev.guide.executor.ExecutionResults;
import com.salaboy.jbpm5.dev.guide.executor.Executor;
import com.salaboy.jbpm5.dev.guide.executor.ExecutorFactoryBean;
import com.salaboy.jbpm5.dev.guide.executor.ExecutorImpl;
import com.salaboy.jbpm5.dev.guide.executor.ExecutorListenerImpl;
import com.salaboy.jbpm5.dev.guide.workitems.AbstractAsyncWorkItemHandler;

/**
 *
 * @author salaboy
 */
public class WaitCompletionAsyncTaskSimpleTest {

	protected Executor executor;
	protected ExecutorListenerImpl listener;
    protected StatefulKnowledgeSession session;

    public WaitCompletionAsyncTaskSimpleTest() {
    }

    @Before
    public void setUp() throws Exception {
    	initializeExecutionEnvironment();
        initializeSession();
    }
    
    @After
    public void tearDown(){
    	stopExecutionEnvironment();
    }
    
    protected void initializeExecutionEnvironment() throws Exception {
    	CheckInCommand.reset();
    	ExecutorFactoryBean factoryBean = new ExecutorFactoryBean();
    	factoryBean.setWaitTime(5000);
		executor = factoryBean.getObject();
    	listener = new ExecutorListenerImpl();
    	listener.setEntityManager(factoryBean.createEntityManager());
    }
    
    protected void stopExecutionEnvironment() {
		executor.destroy();
	}

	private void initializeSession() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(new ClassPathResource("DeferExecutionScenarioV2-data.bpmn"), ResourceType.BPMN2);
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
        
        ((StatefulKnowledgeSessionImpl)session).session.addEventListener(new org.drools.event.AgendaEventListener() {
            public void activationCreated(org.drools.event.ActivationCreatedEvent event, WorkingMemory workingMemory) { }
            public void activationCancelled(org.drools.event.ActivationCancelledEvent event, WorkingMemory workingMemory) { }
            public void beforeActivationFired(org.drools.event.BeforeActivationFiredEvent event, WorkingMemory workingMemory) { }
            public void afterActivationFired(org.drools.event.AfterActivationFiredEvent event, WorkingMemory workingMemory) { }
            public void agendaGroupPopped(org.drools.event.AgendaGroupPoppedEvent event, WorkingMemory workingMemory) { }
            public void agendaGroupPushed(org.drools.event.AgendaGroupPushedEvent event, WorkingMemory workingMemory) { }
            public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event, WorkingMemory workingMemory) { }
            public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event, WorkingMemory workingMemory) {
                workingMemory.fireAllRules();
            }
            public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event, WorkingMemory workingMemory) { }
            public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event, WorkingMemory workingMemory) { }
        });
        
        
    }

    @Test
    public void executorCheckInTestFinishesWithoutHandler() throws InterruptedException {
        HashMap<String, Object> input = new HashMap<String, Object>();
        
        String patientName = "John Doe";
		input.put("bedrequest_patientname", patientName);

		listener.setHandler(new CommandDoneHandler() {
			public void onCommandDone(CommandContext ctx, ExecutionResults execResults) {
				//do nothing
				System.out.println("I'm not completing the workItem");
			}
		});
		
        AbstractAsyncWorkItemHandler asyncHandler = new AbstractAsyncWorkItemHandler(executor, listener);
        session.getWorkItemManager().registerWorkItemHandler("Async Work", asyncHandler);
        
        WorkflowProcessInstance pI = (WorkflowProcessInstance) session.startProcess("PatientDeferredCheckIn", input);

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

		listener.setHandler(new CommandDoneHandler() {
			public void onCommandDone(CommandContext ctx, ExecutionResults execResults) {
				Map<String, Object> results = new HashMap<String, Object>();
				for (Map.Entry<String, Serializable> entry : ctx.getData().entrySet()) {
					results.put(entry.getKey(), entry.getValue());
				}
				String sWorkItemId = (String) ctx.getData("_workItemId");
				session.getWorkItemManager().completeWorkItem(Long.valueOf(sWorkItemId), results);
			}
		});
		
        AbstractAsyncWorkItemHandler asyncHandler = new AbstractAsyncWorkItemHandler(executor, listener);
        session.getWorkItemManager().registerWorkItemHandler("Async Work", asyncHandler);
        
        WorkflowProcessInstance pI = (WorkflowProcessInstance) session.startProcess("PatientDeferredCheckIn", input);

        assertEquals(ProcessInstance.STATE_ACTIVE, pI.getState());

        assertEquals(0, CheckInCommand.getCheckInCount());

        Thread.sleep(((ExecutorImpl) executor).getWaitTime() * 2);
        
        assertEquals(1, CheckInCommand.getCheckInCount());
        
        assertEquals(ProcessInstance.STATE_COMPLETED, pI.getState());
    }
    
    @Test
    public void executorCheckInTestStoppedBefore() throws InterruptedException {
        HashMap<String, Object> input = new HashMap<String, Object>();
        
        String patientName = "John Doe";
		input.put("bedrequest_patientname", patientName);
        
		listener.setHandler(new CommandDoneHandler() {
			public void onCommandDone(CommandContext ctx, ExecutionResults execResults) {
				//do nothing
				System.out.println("I'm not completing the workItem");
			}
		});
		
        AbstractAsyncWorkItemHandler asyncHandler = new AbstractAsyncWorkItemHandler(executor, listener);
        session.getWorkItemManager().registerWorkItemHandler("Async Work", asyncHandler);
        
        WorkflowProcessInstance pI = (WorkflowProcessInstance) session.startProcess("PatientDeferredCheckIn", input);

        assertEquals(ProcessInstance.STATE_ACTIVE, pI.getState());

        assertEquals(0, CheckInCommand.getCheckInCount());
        
        Thread.sleep(((ExecutorImpl) executor).getWaitTime() - 1000);
        
        assertEquals(0, CheckInCommand.getCheckInCount());
    }

}
