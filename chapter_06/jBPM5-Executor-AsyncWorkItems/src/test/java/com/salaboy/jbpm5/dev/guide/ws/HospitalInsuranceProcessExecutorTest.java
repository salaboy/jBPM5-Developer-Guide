package com.salaboy.jbpm5.dev.guide.ws;

import com.salaboy.jbpm5.dev.guide.SessionStoreUtil;
import com.salaboy.jbpm5.dev.guide.executor.Executor;
import com.salaboy.jbpm5.dev.guide.executor.wih.AsyncGenericWorkItemHandler;
import com.salaboy.jbpm5.dev.guide.model.Patient;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;

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

import com.salaboy.jbpm5.dev.guide.webservice.InsuranceService;
import com.salaboy.jbpm5.dev.guide.webservice.InsuranceServiceImpl;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Server;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class HospitalInsuranceProcessExecutorTest {

    protected StatefulKnowledgeSession session;
    private Endpoint endpoint;
    private InsuranceService service;
    private Map<String, Patient> testPatients = new HashMap<String, Patient>();
    protected Executor executor;
    private ApplicationContext ctx;
    private Server server;
    @Before
    public void setUp() {
        initializeWebService();
        initializeSession();
        DeleteDbFiles.execute("~", "mydb", false);
        try {
            server = Server.createTcpServer(new String[]{"-tcp", "-tcpAllowOthers", "-tcpDaemon", "-trace"}).start();
        } catch (SQLException ex) {
            System.out.println("ex: " + ex);
        }
        
        Patient salaboy = new Patient(UUID.randomUUID().toString(), "Salaboy", "SalaboyLastName", "salaboy@gmail.com", "555-15151-515151", 28);
        testPatients.put("salaboy", salaboy);
        Patient nonInsuredBrotha = new Patient(UUID.randomUUID().toString(), "John", "Doe", "gangsta1980@gmail.com", "333-333131-13131", 40);
        testPatients.put("brotha", nonInsuredBrotha);

        this.service.getPatients().put(testPatients.get("salaboy").getId(), testPatients.get("salaboy"));
        this.service.getPatients().put(testPatients.get("brotha").getId(), testPatients.get("brotha"));
        this.service.getInsuredPatients().put(testPatients.get("salaboy").getId(), Boolean.TRUE);
        this.service.getInsuredPatients().put(testPatients.get("brotha").getId(), Boolean.FALSE);
        
        System.out.println(" >>> Insured Patients: "+this.service.getInsuredPatients());
        ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        executor = (Executor) ctx.getBean("executorService");
        executor.init();
    }

    @After
    public void tearDown() {
        stopWebService();
        executor.destroy();
        server.stop();
    }

    @Test
    public void testPatientInsuredProcessWithExecutor() throws InterruptedException {
        HashMap<String, Object> input = new HashMap<String, Object>();

        Patient salaboy = testPatients.get("salaboy");
        input.put("patientName", salaboy.getId());
        SessionStoreUtil.sessionCache.put("sessionId="+session.getId(), session);
        AsyncGenericWorkItemHandler genericHandler = new AsyncGenericWorkItemHandler(executor,session.getId());

        session.getWorkItemManager().registerWorkItemHandler("Gather Patient Data", genericHandler);
        session.getWorkItemManager().registerWorkItemHandler("Insurance Service", genericHandler);
        session.getWorkItemManager().registerWorkItemHandler("External Insurance Company Service", genericHandler);
        session.getWorkItemManager().registerWorkItemHandler("Rates Service", genericHandler);
        session.getWorkItemManager().registerWorkItemHandler("Invoice Service", genericHandler);

        WorkflowProcessInstance pI = (WorkflowProcessInstance) session.startProcess("NewPatientInsuranceCheck", input);
        //Our application can continue doing other things, the executor will do the rest
        Thread.sleep(25000);
        assertEquals(ProcessInstance.STATE_COMPLETED, pI.getState());
        assertEquals(Boolean.TRUE, pI.getVariable("patientNotified"));
        assertEquals(50, ((BigDecimal)pI.getVariable("finalAmount")).intValue());
        
    }
    
    @Test
    public void testPatientNonInsuredProcessWithExecutor() throws InterruptedException {
        HashMap<String, Object> input = new HashMap<String, Object>();

        Patient brotha = testPatients.get("brotha");
        input.put("patientName", brotha.getId());
        SessionStoreUtil.sessionCache.put("sessionId="+session.getId(), session);
        AsyncGenericWorkItemHandler genericHandler = new AsyncGenericWorkItemHandler(executor,session.getId());
       
        session.getWorkItemManager().registerWorkItemHandler("Gather Patient Data", genericHandler);
       
        session.getWorkItemManager().registerWorkItemHandler("Insurance Service", genericHandler);
       
        session.getWorkItemManager().registerWorkItemHandler("External Insurance Company Service", genericHandler);
       
        session.getWorkItemManager().registerWorkItemHandler("Rates Service", genericHandler);
       
        session.getWorkItemManager().registerWorkItemHandler("Invoice Service", genericHandler);

        WorkflowProcessInstance pI = (WorkflowProcessInstance) session.startProcess("NewPatientInsuranceCheck", input);

        //Our application can continue doing other things, the executor will do the rest
        Thread.sleep(25000);
        
        assertEquals(ProcessInstance.STATE_COMPLETED, pI.getState());
        assertEquals(Boolean.TRUE, pI.getVariable("patientNotified"));
        assertEquals(600, ((BigDecimal)pI.getVariable("finalAmount")).intValue());
        
    }


    private void initializeWebService() {
        this.service = new InsuranceServiceImpl();
        this.endpoint = Endpoint.publish(
                "http://127.0.0.1:19999/InsuranceServiceImpl/insurance",
                service);

    }

    private void stopWebService() {
        this.endpoint.stop();

    }

    private void initializeSession() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(new ClassPathResource("InsuranceProcessV2.bpmn"), ResourceType.BPMN2);
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
}