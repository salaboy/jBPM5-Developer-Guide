package com.salaboy.jbpm5.dev.guide.webservice;

import com.salaboy.jbpm5.dev.guide.model.ConceptCode;
import com.salaboy.jbpm5.dev.guide.model.Patient;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.jws.WebMethod;

import javax.jws.WebService;

@WebService()
public class InsuranceServiceImpl implements InsuranceService {

    private final Map<String, Patient> patients = new HashMap<String, Patient>();
    private final Map<String, Boolean> insuredPatients = new HashMap<String, Boolean>();

    public InsuranceServiceImpl() {
    }

    
    
    public Patient getPatientData(String patientId) {
        return patients.get(patientId);
    }

    public boolean isPatientInsured(String patientId) {
        return insuredPatients.get(patientId);
    }

    public BigDecimal notifyInsuranceCompany(String company, String patientId, BigDecimal amount) {
        System.out.println("Notifying company:" + company + " - Patient Id: " + patientId + " - Amount: " + amount);
        BigDecimal finalAmount = amount.divide(new BigDecimal(2));
        return finalAmount;

    }

    public BigDecimal calculateHospitalRates(String patientId, List<ConceptCode> concepts) {
        BigDecimal lastAmount = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        
        for (ConceptCode concept : concepts) {
            for(int i = 0; i < concept.getUnits(); i++){
                total = lastAmount.add(concept.getRate());
                lastAmount = total;
            }
        }

        return total;
    }

    public boolean notifyAndChargePatient(Patient patient, BigDecimal amount, List<ConceptCode> concepts) {
        System.out.println("+--------------------------------------------------------------+");
        System.out.println("| ### Hospital Invoice                                         |");
        System.out.println("+--------------------------------------------------------------+");
        DateFormat df = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.UK);
        System.out.println("| Date: "+ df.format(new Date())+"|");
        System.out.println("|--------------------------------------------------------------|");
        System.out.println("| Patient: "+ patient.getFirstName() +","+patient.getLastName()+"|");
        System.out.println("|--------------------------------------------------------------|");
        //System.out.println("| Insured: "++"                                                |");
        System.out.println("|--------------------------------------------------------------|");
        System.out.println("| Concepts:                                                    |");
        System.out.println("|--------------------------------------------------------------|");
        for(ConceptCode code : concepts){
            System.out.println("| -> Concept:  "+code.getDesc()+" ---------  | "+code.getUnits()+" X  |"+ code.getRate()+"");
        }
        System.out.println("|--------------------------------------------------+-----------|");
        System.out.println("| Total                                            |        "+amount+"|");
        System.out.println("|--------------------------------------------------+-----------|");
        
        System.out.println(" -> Sending Invoce Via Email to: "+ patient.getEmail());
        System.out.println(" -> Charging Credit Card ("+patient.getCreditCardNumber()+") - Total: "+amount);
        
        return true;
    }
    @WebMethod(exclude=true)
    public Map<String, Boolean> getInsuredPatients() {
        return insuredPatients;
    }
    @WebMethod(exclude=true)
    public Map<String, Patient> getPatients() {
        return patients;
    }
}
