/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.dev.guide.commands;

import com.salaboy.jbpm5.dev.guide.executor.Command;
import com.salaboy.jbpm5.dev.guide.executor.CommandContext;
import com.salaboy.jbpm5.dev.guide.executor.ExecutionResults;
import com.salaboy.jbpm5.dev.guide.model.Patient;
import com.salaboy.jbpm5.dev.guide.webservice.InsuranceService;
import com.salaboy.jbpm5.dev.guide.workitems.PatientDataServiceWorkItemHandler;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

/**
 *
 * @author salaboy
 */
public class GetPatientDataCommand implements Command{

    public ExecutionResults execute(CommandContext ctx) {
        System.out.println("patientName = "+ctx.getData("gatherdata_patientName"));
        String patientId = (String) ctx.getData("gatherdata_patientName");
        Patient patientData = null;
        try {
            InsuranceService client = getClient();
            patientData = client.getPatientData(patientId);
        } catch (MalformedURLException ex) {
            Logger.getLogger(PatientDataServiceWorkItemHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        ExecutionResults executionResults = new ExecutionResults();
        executionResults.setData("gatherdata_patient", patientData);
        return executionResults;
    }
    
     private InsuranceService getClient() throws MalformedURLException {
        URL wsdlURL = new URL(
                        "http://127.0.0.1:19999/InsuranceServiceImpl/insurance?WSDL");
        QName SERVICE_QNAME = new QName(
                        "http://webservice.guide.dev.jbpm5.salaboy.com/", 
                        "InsuranceServiceImplService");
        Service service = Service.create(wsdlURL, SERVICE_QNAME);
        InsuranceService client = service.getPort(InsuranceService.class);
        return client;
    }
    
}
