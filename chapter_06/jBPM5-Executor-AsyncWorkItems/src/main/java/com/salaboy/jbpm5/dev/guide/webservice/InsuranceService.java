package com.salaboy.jbpm5.dev.guide.webservice;

import com.salaboy.jbpm5.dev.guide.model.ConceptCode;
import com.salaboy.jbpm5.dev.guide.model.Patient;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService()
public interface InsuranceService {

        @WebMethod(operationName = "getPatientData")
        public Patient getPatientData(@WebParam(name = "patientId")String patientId);
        
        @WebMethod(operationName = "isPatientInsured")
        public boolean isPatientInsured(@WebParam(name = "patientId")String patientId);
        
        @WebMethod(operationName = "notifyInsuranceCompany")
        public BigDecimal notifyInsuranceCompany(@WebParam(name = "company")String company, 
                                                 @WebParam(name = "patientId")String patientId, 
                                                 @WebParam(name = "amount")BigDecimal amount);
        
        @WebMethod(operationName = "calculateHospitalRates")
        public BigDecimal calculateHospitalRates(@WebParam(name = "patientId")String patientId, 
                                                 @WebParam(name = "concepts")List<ConceptCode> concepts);
        @WebMethod(operationName = "notifyAndChargePatient")
        public boolean notifyAndChargePatient(@WebParam(name = "patient")Patient patient, 
                                              @WebParam(name = "amount")BigDecimal amount, 
                                              @WebParam(name = "concepts")List<ConceptCode> concepts);
        
        public Map<String, Boolean> getInsuredPatients();
        
        public Map<String, Patient> getPatients();
        

}
