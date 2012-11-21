package com.salaboy.jbpm5.dev.guide.ws;

import com.salaboy.jbpm5.dev.guide.model.Patient;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import javax.xml.ws.Endpoint;

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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.salaboy.jbpm5.dev.guide.webservice.SimpleValidationService;
import com.salaboy.jbpm5.dev.guide.webservice.SimpleValidationServiceImpl;
import com.salaboy.jbpm5.dev.guide.workitems.CXFWebServiceWorkItemHandler;
import java.util.Map;
import java.util.UUID;
import org.drools.event.rule.DefaultAgendaEventListener;

/**
 * This test class shows the interaction between an Abstract Task and a Web Service
 * using a custom Work Item Handler: {@link CXFWebServiceWorkItemHandler}.
 * @author esteban
 */
public class GenericWebServiceTaskTest {

    protected StatefulKnowledgeSession session;
    private Endpoint endpoint;
    private SimpleValidationService service;
    private Map<String, Patient> testPatients = new HashMap<String, Patient>();
    @Before
    public void setUp() {
        initializeWebService();
        initializeSession();
    }

    @After
    public void tearDown() {
        stopWebService();
    }
    

    private void initializeWebService() {
        this.service = new SimpleValidationServiceImpl();
        this.endpoint = Endpoint.publish(
                "http://127.0.0.1:19999/WebServiceExample/insurance",
                service);
        Patient salaboy = new Patient(UUID.randomUUID().toString(), "Salaboy", "SalaboyLastName", "salaboy@gmail.com", "555-15151-515151", 28);
        testPatients.put("salaboy", salaboy);
        Patient nonInsuredBrotha = new Patient(UUID.randomUUID().toString(), "John", "Doe", "gangsta1980@gmail.com", "333-333131-13131", 40);
        testPatients.put("brotha", nonInsuredBrotha);
        SimpleValidationServiceImpl.insuredPatients.put(testPatients.get("salaboy").getId(), Boolean.TRUE);
        SimpleValidationServiceImpl.insuredPatients.put(testPatients.get("brotha").getId(), Boolean.FALSE);
    }

    private void stopWebService() {
        this.endpoint.stop();

    }

    /**
     * Simple test showing the interaction between a process and a web service.
     * This method tests the case of an invalid response from the web-service.
     */
    @Test
    public void testPatientInsuranceCheckProcessFalse() {
        HashMap<String, Object> input = new HashMap<String, Object>();

        
        input.put("bedrequest_patientname", testPatients.get("brotha").getId());

        //Register a synchronous work item handler that will invoke a web service
        CXFWebServiceWorkItemHandler webServiceHandler = new CXFWebServiceWorkItemHandler();
        session.getWorkItemManager().registerWorkItemHandler("Web Service", webServiceHandler);

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

        //Register a synchronous work item handler that will invoke a web service
        CXFWebServiceWorkItemHandler webServiceHandler = new CXFWebServiceWorkItemHandler();
        session.getWorkItemManager().registerWorkItemHandler("Web Service", webServiceHandler);

        WorkflowProcessInstance pI = (WorkflowProcessInstance) session.startProcess("NewPatientInsuranceCheck", input);

        //'salaboy' is correctly inssured according to the web service we have configured.
        //The response should be valid.
        assertEquals(ProcessInstance.STATE_COMPLETED, pI.getState());
        assertEquals(Boolean.TRUE, pI.getVariable("checkinresults_patientInsured"));
        System.out.println("-> Insurance Valid = " + pI.getVariable("checkinresults_patientInsured"));

    }
    
    private void initializeSession() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(new ClassPathResource("HospitalInvokeInsuranceScenarioV3-data.bpmn"), ResourceType.BPMN2);
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