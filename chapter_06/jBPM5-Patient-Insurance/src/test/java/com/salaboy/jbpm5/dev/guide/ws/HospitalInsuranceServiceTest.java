package com.salaboy.jbpm5.dev.guide.ws;

import com.salaboy.jbpm5.dev.guide.model.ConceptCode;
import com.salaboy.jbpm5.dev.guide.model.Patient;

import javax.xml.ws.Endpoint;

import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.salaboy.jbpm5.dev.guide.webservice.InsuranceService;
import com.salaboy.jbpm5.dev.guide.webservice.InsuranceServiceImpl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Isolated tests for {@link InsuranceServiceImpl} Web Service methods.
 * This web service is used by most of the tests in this project.
 * @author esteban
 */
public class HospitalInsuranceServiceTest {

    protected StatefulKnowledgeSession session;
    private Endpoint endpoint;
    private InsuranceService service;
    private Map<String, Patient> testPatients = new HashMap<String, Patient>();

    @Before
    public void setUp() {
        initializeWebService();
        Patient salaboy = new Patient(UUID.randomUUID().toString(), "Salaboy", "SalaboyLastName", "salaboy@gmail.com", "555-15151-515151", 28);
        testPatients.put("salaboy", salaboy);
        Patient nonInsuredBrotha = new Patient(UUID.randomUUID().toString(), "John", "Doe", "gansta1980@gmail.com", "333-333131-13131", 40);
        testPatients.put("brotha", nonInsuredBrotha);

        this.service.getPatients().put(testPatients.get("salaboy").getId(), testPatients.get("salaboy"));
        this.service.getPatients().put(testPatients.get("brotha").getId(), testPatients.get("brotha"));
        this.service.getInsuredPatients().put(testPatients.get("salaboy").getId(), Boolean.TRUE);
        this.service.getInsuredPatients().put(testPatients.get("brotha").getId(), Boolean.FALSE);
    }

    @After
    public void tearDown() {
        stopWebService();
    }

    @Test
    public void patientInsuredTest() {

        Patient patient = this.service.getPatientData(testPatients.get("salaboy").getId());
        assertNotNull(patient);
        boolean isPatientInsured = this.service.isPatientInsured(patient.getId());
        assertTrue(isPatientInsured);
        BigDecimal insuredAmount = this.service.notifyInsuranceCompany("Insurance Company 1", patient.getId(), new BigDecimal(100));
        assertEquals(50, insuredAmount.intValue());
        List<ConceptCode> concepts = new ArrayList<ConceptCode>(1);
        concepts.add(new ConceptCode("CO-9999", insuredAmount, "Insured Patient Flat Rate", 1));
        boolean notifyAndChargePatient = this.service.notifyAndChargePatient(patient, insuredAmount, concepts);
        assertTrue(notifyAndChargePatient);

    }

    @Test
    public void patientNotInsuredTest() {

        Patient patient = this.service.getPatientData(testPatients.get("brotha").getId());
        assertNotNull(patient);
        boolean isPatientInsured = this.service.isPatientInsured(patient.getId());
        assertFalse(isPatientInsured);
        List<ConceptCode> concepts = new ArrayList<ConceptCode>(2);
        concepts.add(new ConceptCode("CO-123", new BigDecimal(125), "Dialy Hospital Bed Rate", 4));
        concepts.add(new ConceptCode("CO-123", new BigDecimal(100), "Nurse Service", 1));
        BigDecimal nonInsuredAmount = this.service.calculateHospitalRates(patient.getId(), concepts);
        assertEquals(600, nonInsuredAmount.intValue());
        boolean notifyAndChargePatient = this.service.notifyAndChargePatient(patient, nonInsuredAmount, concepts);
        assertTrue(notifyAndChargePatient);

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
}