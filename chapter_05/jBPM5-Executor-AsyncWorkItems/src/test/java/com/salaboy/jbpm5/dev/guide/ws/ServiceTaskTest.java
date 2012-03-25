//package com.salaboy.jbpm5.dev.guide.ws;
//
//import static org.junit.Assert.assertEquals;
//
//import java.util.HashMap;
//
//import org.drools.KnowledgeBase;
//import org.drools.KnowledgeBaseFactory;
//import org.drools.WorkingMemory;
//import org.drools.builder.KnowledgeBuilder;
//import org.drools.builder.KnowledgeBuilderError;
//import org.drools.builder.KnowledgeBuilderErrors;
//import org.drools.builder.KnowledgeBuilderFactory;
//import org.drools.builder.ResourceType;
//import org.drools.event.RuleFlowGroupActivatedEvent;
//import org.drools.event.RuleFlowGroupDeactivatedEvent;
//import org.drools.impl.StatefulKnowledgeSessionImpl;
//import org.drools.io.impl.ClassPathResource;
//import org.drools.logger.KnowledgeRuntimeLoggerFactory;
//import org.drools.runtime.StatefulKnowledgeSession;
//import org.drools.runtime.process.ProcessInstance;
//import org.drools.runtime.process.WorkflowProcessInstance;
//import org.jbpm.bpmn2.handler.ServiceTaskHandler;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import com.salaboy.jbpm5.dev.guide.webservice.InsuranceService;
//import com.salaboy.jbpm5.dev.guide.webservice.InsuranceServiceImpl;
//
//public class ServiceTaskTest {
//    tata
//    protected StatefulKnowledgeSession session;
//
//    @Before
//    public void setUp(){
//    	initializeService();
//        initializeSession();
//    }
//    
//    @After
//    public void tearDown(){
//    	stopService();
//    }
//    
//    @Test
//    public void testPatientInsuranceCheckProcessFalse() {
//        HashMap<String, Object> input = new HashMap<String, Object>();
//        
//        String patientName = "John Doe";
//		input.put("bedrequest_patientname", patientName);
//		
//		session.getWorkItemManager().registerWorkItemHandler("Service Task", new ServiceTaskHandler());
//		
//        WorkflowProcessInstance pI = (WorkflowProcessInstance) session.startProcess("NewPatientInsuranceCheck", input);
//
//        assertEquals(ProcessInstance.STATE_COMPLETED, pI.getState());
//        assertEquals(Boolean.FALSE, pI.getVariable("checkinresults_patientInsured"));
//        System.out.println("-> Insurance Valid = "+pI.getVariable("checkinresults_patientInsured"));
//    }
//    
//    @Test
//    public void testPatientInsuranceCheckProcessTrue() {
//        HashMap<String, Object> input = new HashMap<String, Object>();
//        
//        String patientName = "John Doe";
//		input.put("bedrequest_patientname", patientName);
//        this.service.insurance(patientName);
//        
//        session.getWorkItemManager().registerWorkItemHandler("Service Task", new ServiceTaskHandler());
//		
//        WorkflowProcessInstance pI = (WorkflowProcessInstance) session.startProcess("NewPatientInsuranceCheck", input);
//
//        assertEquals(ProcessInstance.STATE_COMPLETED, pI.getState());
//        assertEquals(Boolean.TRUE, pI.getVariable("checkinresults_patientInsured"));
//        System.out.println("-> Insurance Valid = "+pI.getVariable("checkinresults_patientInsured"));
//
//        this.service.revoke(patientName);
//        
//        WorkflowProcessInstance pI2 = (WorkflowProcessInstance) session.startProcess("NewPatientInsuranceCheck", input);
//
//        assertEquals(ProcessInstance.STATE_COMPLETED, pI2.getState());
//        assertEquals(Boolean.FALSE, pI2.getVariable("checkinresults_patientInsured"));
//        System.out.println("-> Insurance Valid = "+pI2.getVariable("checkinresults_patientInsured"));
//    }
//
//    private InsuranceService service;
//    
//    private void initializeService() {
//    	this.service = new InsuranceServiceImpl();
//		this.service.revokeAll();
//    }
//
//    private void stopService() {
//    	this.service.revokeAll();
//    }
//    
//    private void initializeSession() {
//        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
//
//        kbuilder.add(new ClassPathResource("HospitalInvokeInsuranceScenarioV1-data.bpmn"), ResourceType.BPMN2);
//        if (kbuilder.hasErrors()) {
//            KnowledgeBuilderErrors errors = kbuilder.getErrors();
//
//            for (KnowledgeBuilderError error : errors) {
//                System.out.println(">>> Error:" + error.getMessage());
//
//            }
//            throw new IllegalStateException(">>> Knowledge couldn't be parsed! ");
//        }
//
//        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
//
//        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
//
//        session = kbase.newStatefulKnowledgeSession();
//        KnowledgeRuntimeLoggerFactory.newConsoleLogger(session);
//        
//        ((StatefulKnowledgeSessionImpl)session).session.addEventListener(new org.drools.event.AgendaEventListener() {
//            public void activationCreated(org.drools.event.ActivationCreatedEvent event, WorkingMemory workingMemory) { }
//            public void activationCancelled(org.drools.event.ActivationCancelledEvent event, WorkingMemory workingMemory) { }
//            public void beforeActivationFired(org.drools.event.BeforeActivationFiredEvent event, WorkingMemory workingMemory) { }
//            public void afterActivationFired(org.drools.event.AfterActivationFiredEvent event, WorkingMemory workingMemory) { }
//            public void agendaGroupPopped(org.drools.event.AgendaGroupPoppedEvent event, WorkingMemory workingMemory) { }
//            public void agendaGroupPushed(org.drools.event.AgendaGroupPushedEvent event, WorkingMemory workingMemory) { }
//            public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event, WorkingMemory workingMemory) { }
//            public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event, WorkingMemory workingMemory) {
//                workingMemory.fireAllRules();
//            }
//            public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event, WorkingMemory workingMemory) { }
//            public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event, WorkingMemory workingMemory) { }
//        });
//        
//        
//    }
//}
