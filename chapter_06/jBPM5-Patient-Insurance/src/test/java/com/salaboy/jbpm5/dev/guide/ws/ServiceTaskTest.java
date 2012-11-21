package com.salaboy.jbpm5.dev.guide.ws;

import com.salaboy.jbpm5.dev.guide.model.Patient;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.impl.ClassPathResource;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkflowProcessInstance;
import org.jbpm.bpmn2.handler.ServiceTaskHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.salaboy.jbpm5.dev.guide.webservice.SimpleValidationServiceImpl;
import java.util.Map;
import java.util.UUID;
import org.drools.event.rule.DefaultAgendaEventListener;

/**
 * Similar to {@link GenericWebServiceTaskTest} but using jBPM5' out-of-the box
 * Service Task Handler. The process being used by this test uses a Service Task
 * node instead of an Abstract Task node.
 * @author esteban
 */
public class ServiceTaskTest {

    protected StatefulKnowledgeSession session;
    private Map<String, Patient> testPatients = new HashMap<String, Patient>();

    @Before
    public void setUp() {
        initializeSession();

        Patient salaboy = new Patient(UUID.randomUUID().toString(), "Salaboy", "SalaboyLastName", "salaboy@gmail.com", "555-15151-515151", 28);
        testPatients.put("salaboy", salaboy);
        Patient nonInsuredBrotha = new Patient(UUID.randomUUID().toString(), "John", "Doe", "gangsta1980@gmail.com", "333-333131-13131", 40);
        testPatients.put("brotha", nonInsuredBrotha);

        SimpleValidationServiceImpl.insuredPatients.put(testPatients.get("salaboy").getId(), Boolean.TRUE);
        SimpleValidationServiceImpl.insuredPatients.put(testPatients.get("brotha").getId(), Boolean.FALSE);

    }

    @After
    public void tearDown() {
    }

    /**
     * Simple test showing the interaction between a process and a web service.
     * This method tests the case of an invalid response from the web-service.
     */
    @Test
    public void testPatientInsuranceCheckProcessFalse() {
        HashMap<String, Object> input = new HashMap<String, Object>();


        input.put("bedrequest_patientname", testPatients.get("brotha").getId());

        //Register a synchronous work item handler that will invoke a web service.
        //This work item handler is provided by jBPM5.
        session.getWorkItemManager().registerWorkItemHandler("Service Task", new ServiceTaskHandler());

        WorkflowProcessInstance pI = (WorkflowProcessInstance) session.startProcess("NewPatientInsuranceCheck", input);

        //'brotha' is not inssured according to the web service we have configured.
        //The response should be invalid.
        assertEquals(ProcessInstance.STATE_COMPLETED, pI.getState());
        assertEquals(Boolean.FALSE, pI.getVariable("checkinresults_patientInsured"));
        System.out.println("-> Insurance Valid = " + pI.getVariable("checkinresults_patientInsured"));


    }
    
    /**
     * Simple test showing the interaction between a process and a web service.
     * This method tests the case of an invalid response from the web-service.
     */
    @Test
    public void testPatientInsuranceCheckProcessTrue() {
        HashMap<String, Object> input = new HashMap<String, Object>();

        input.put("bedrequest_patientname", testPatients.get("salaboy").getId());

        //Register a synchronous work item handler that will invoke a web service.
        //This work item handler is provided by jBPM5.
        session.getWorkItemManager().registerWorkItemHandler("Service Task", new ServiceTaskHandler());

        WorkflowProcessInstance pI = (WorkflowProcessInstance) session.startProcess("NewPatientInsuranceCheck", input);

        //'salaboy' is correctly inssured according to the web service we have configured.
        //The response should be valid.
        assertEquals(ProcessInstance.STATE_COMPLETED, pI.getState());
        assertEquals(Boolean.TRUE, pI.getVariable("checkinresults_patientInsured"));
        System.out.println("-> Insurance Valid = " + pI.getVariable("checkinresults_patientInsured"));
    }

    private void initializeSession() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(new ClassPathResource("HospitalInvokeInsuranceScenarioV1-data.bpmn"), ResourceType.BPMN2);
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

    }
}
