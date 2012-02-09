package com.salaboy.jbpm5.dev.guide;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import javax.persistence.EntityManager;
import javax.xml.ws.Endpoint;

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

import com.salaboy.jbpm5.dev.guide.executor.CommandContext;
import com.salaboy.jbpm5.dev.guide.executor.CommandDoneHandler;
import com.salaboy.jbpm5.dev.guide.executor.ExecutionResults;
import com.salaboy.jbpm5.dev.guide.executor.Executor;
import com.salaboy.jbpm5.dev.guide.executor.ExecutorFactoryBean;
import com.salaboy.jbpm5.dev.guide.executor.ExecutorListenerBuilder;
import com.salaboy.jbpm5.dev.guide.webservice.SlowService;
import com.salaboy.jbpm5.dev.guide.webservice.SlowServiceImpl;
import com.salaboy.jbpm5.dev.guide.workitems.AbstractAsyncWorkItemHandler;

public class DeferredWebServiceTaskTest {

	protected Executor executor;
	protected EntityManager em;
    protected StatefulKnowledgeSession session;
    private Endpoint endpoint;
    private SlowService service;

    public DeferredWebServiceTaskTest() {
	}
    
    @Before
    public void setUp() throws Exception {
    	initializeExecutionEnvironment();
    	initializeWebService();
        initializeSession();
    }
    
    @After
    public void tearDown(){
    	stopExecutionEnvironment();
    	stopWebService();
    }

    private void initializeWebService() {
    	this.service = new SlowServiceImpl();
		this.endpoint = Endpoint.publish(
    			"http://127.0.0.1:9999/WebServiceExample/slow", 
    			service);
    }

    private void stopWebService() {
    	this.endpoint.stop();
    }

    protected void initializeExecutionEnvironment() throws Exception {
    	ExecutorFactoryBean factoryBean = new ExecutorFactoryBean();
    	factoryBean.setWaitTime(5000);
		executor = factoryBean.getObject();
    	em = factoryBean.createEntityManager();
    }
    
    protected void stopExecutionEnvironment() {
		executor.destroy();
	}

	private void initializeSession() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(new ClassPathResource("DeferredWebServicesScenarioV1-data.bpmn"), ResourceType.BPMN2);
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
	public void testSlowCallsHappen() {
		HashMap<String, Object> input = new HashMap<String, Object>();
        
        String patientName = "John Doe";
		input.put("bedrequest_patientname", patientName);

		ExecutorListenerBuilder listenerBuilder = new ExecutorListenerBuilder(em, new CommandDoneHandler() {
			public void onCommandDone(CommandContext ctx, ExecutionResults results) {
				String methodName = (String) ctx.getData("methodName");
				String outputName = (String) ctx.getData("outputName");
				String output = (String) results.getData(outputName);
				System.out.println(">>> invoked " + methodName + ". Result: " + output);
			}
		});
		
		AbstractAsyncWorkItemHandler webServiceHandler = new AbstractAsyncWorkItemHandler(executor, listenerBuilder);
        session.getWorkItemManager().registerWorkItemHandler("Deferred Web Service", webServiceHandler);
        
        WorkflowProcessInstance pI = (WorkflowProcessInstance) session.startProcess("DefferedWebService", input);

        assertEquals(ProcessInstance.STATE_COMPLETED, pI.getState());
	}
    
}
