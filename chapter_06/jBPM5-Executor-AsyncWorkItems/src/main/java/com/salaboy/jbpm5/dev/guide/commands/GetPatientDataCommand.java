/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.dev.guide.commands;

import com.salaboy.jbpm5.dev.guide.model.Patient;
import com.salaboy.jbpm5.dev.guide.webservice.InsuranceService;
import java.net.MalformedURLException;
import java.net.URL;
import javax.inject.Named;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import org.jbpm.executor.api.Command;
import org.jbpm.executor.api.CommandContext;
import org.jbpm.executor.api.ExecutionResults;

/**
 *
 * @author salaboy
 */
@Named
public class GetPatientDataCommand implements Command {

    public ExecutionResults execute(CommandContext ctx) throws MalformedURLException {
        String patientId = (String) ctx.getData("gatherdata_patientName");
        InsuranceService client = getClient();
        Patient patientData = client.getPatientData(patientId);
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
