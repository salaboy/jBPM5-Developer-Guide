/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.dev.guide.commands;

import com.salaboy.jbpm5.dev.guide.executor.Command;
import com.salaboy.jbpm5.dev.guide.executor.CommandContext;
import com.salaboy.jbpm5.dev.guide.executor.ExecutionResults;
import com.salaboy.jbpm5.dev.guide.webservice.InsuranceService;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

/**
 *
 * @author salaboy
 */
public class IsPatientInsuredCommand implements Command{

    public ExecutionResults execute(CommandContext ctx) {
        String patientId = (String) ctx.getData("insured_patientName");
        boolean isPatientInsured = false;
        try {
            InsuranceService client = getClient();
            isPatientInsured = client.isPatientInsured(patientId);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        ExecutionResults results = new ExecutionResults();
        results.setData("insured_isPatientInsured", isPatientInsured);
        return results;
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
