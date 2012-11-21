package com.salaboy.jbpm5.dev.guide.ws;

import com.salaboy.jbpm5.dev.guide.util.SessionStoreUtil;
import com.salaboy.jbpm5.dev.guide.model.Patient;
import com.salaboy.jbpm5.dev.guide.webservice.InsuranceService;
import com.salaboy.jbpm5.dev.guide.webservice.InsuranceServiceImpl;
import com.salaboy.jbpm5.dev.guide.workitems.AsyncGenericWorkItemHandler;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.xml.ws.Endpoint;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.event.rule.DefaultAgendaEventListener;
import org.drools.io.impl.ClassPathResource;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkflowProcessInstance;
import org.jbpm.executor.ExecutorModule;
import org.jbpm.executor.ExecutorServiceEntryPoint;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 * Executes "Hopistal Insurance Check-In" process introduced un Chapter 6.
 * These tests use the same instance of {@link AsyncGenericWorkItemHandler} for
 * all the tasks defined in the process. This handler will use the Executor 
 * Service component to execute different methods of a web service.
 * @author esteban
 */
public class HospitalInsuranceProcessExecutorTest {

    protected StatefulKnowledgeSession session;
    private Endpoint endpoint;
    private InsuranceService service;
    private Map<String, Patient> testPatients = new HashMap<String, Patient>();
    protected ExecutorServiceEntryPoint executor;
    
    
    @Before
    public void setUp() {
        initializeWebService();
        
        Patient salaboy = new Patient(UUID.randomUUID().toString(), "Salaboy", "SalaboyLastName", "salaboy@gmail.com", "555-15151-515151", 28);
        testPatients.put("salaboy", salaboy);
        Patient nonInsuredBrotha = new Patient(UUID.randomUUID().toString(), "John", "Doe", "gangsta1980@gmail.com", "333-333131-13131", 40);
        testPatients.put("brotha", nonInsuredBrotha);

        this.service.getPatients().put(testPatients.get("salaboy").getId(), testPatients.get("salaboy"));
        this.service.getPatients().put(testPatients.get("brotha").getId(), testPatients.get("brotha"));
        this.service.getInsuredPatients().put(testPatients.get("salaboy").getId(), Boolean.TRUE);
        this.service.getInsuredPatients().put(testPatients.get("brotha").getId(), Boolean.FALSE);
        
        System.out.println(" >>> Insured Patients: "+this.service.getInsuredPatients());
        
        executor = ExecutorModule.getInstance().getExecutorServiceEntryPoint();
        executor.setThreadPoolSize(1);
        executor.setInterval(3);
        executor.init();
        
        
        initializeSession();
    }

    @After
    public void tearDown() {
        executor.clearAllRequests();
        executor.clearAllErrors();
        executor.destroy();
        stopWebService();
    }

    /**
     * Tests the execution path for a patient having a valid insurance.
     */
    @Test
    public void testPatientInsuredProcessWithExecutor() throws InterruptedException {
        HashMap<String, Object> input = new HashMap<String, Object>();

        Patient salaboy = testPatients.get("salaboy");
        input.put("patientName", salaboy.getId());
        SessionStoreUtil.sessionCache.put("sessionId="+session.getId(), session);

        WorkflowProcessInstance pI = (WorkflowProcessInstance) session.startProcess("NewPatientInsuranceCheck", input);
        //Our application can continue doing other things, the executor will do the rest
        Thread.sleep(25000);
        assertEquals(ProcessInstance.STATE_COMPLETED, pI.getState());
        assertEquals(Boolean.TRUE, pI.getVariable("patientNotified"));
        assertEquals(50, ((BigDecimal)pI.getVariable("finalAmount")).intValue());
        
    }
    
    /**
     * Tests the execution path for a patient NOT having a valid insurance.
     */
    @Test
    public void testPatientNonInsuredProcessWithExecutor() throws InterruptedException {
        HashMap<String, Object> input = new HashMap<String, Object>();

        Patient brotha = testPatients.get("brotha");
        input.put("patientName", brotha.getId());
        SessionStoreUtil.sessionCache.put("sessionId="+session.getId(), session);
       
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

        session.addEventListener(new DefaultAgendaEventListener(){

            @Override
            public void afterRuleFlowGroupActivated(org.drools.event.rule.RuleFlowGroupActivatedEvent event) {
                session.fireAllRules();
            }
            
        });
        
        //Registers the same generic handler for all the tasks. The handler will
        //schedule commands into the Executor Service. Each task in the process
        //is configured to use a different command.
        AsyncGenericWorkItemHandler genericHandler = new AsyncGenericWorkItemHandler(executor,session.getId());
        session.getWorkItemManager().registerWorkItemHandler("Gather Patient Data", genericHandler);
        session.getWorkItemManager().registerWorkItemHandler("Insurance Service", genericHandler);
        session.getWorkItemManager().registerWorkItemHandler("External Insurance Company Service", genericHandler);
        session.getWorkItemManager().registerWorkItemHandler("Rates Service", genericHandler);
        session.getWorkItemManager().registerWorkItemHandler("Invoice Service", genericHandler);

    }
}
