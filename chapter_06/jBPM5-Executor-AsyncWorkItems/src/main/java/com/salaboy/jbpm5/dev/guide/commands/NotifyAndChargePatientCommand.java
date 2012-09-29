/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.dev.guide.commands;

import com.salaboy.jbpm5.dev.guide.model.ConceptCode;
import com.salaboy.jbpm5.dev.guide.model.Patient;
import com.salaboy.jbpm5.dev.guide.webservice.InsuranceService;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
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
public class NotifyAndChargePatientCommand implements Command {

    public ExecutionResults execute(CommandContext ctx) {
        Patient patient = (Patient) ctx.getData("invoice_patient");
        BigDecimal finalAmount = (BigDecimal) ctx.getData("invoice_finalAmount");
        List<ConceptCode> concepts = (List<ConceptCode>) ctx.getData("invoice_concepts");
        boolean patientNotified = false;
        try {
            InsuranceService client = getClient();
            patientNotified = client.notifyAndChargePatient(patient, finalAmount, concepts);

        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        System.out.println(" >>> Patient Notified = " + patientNotified);
        ExecutionResults results = new ExecutionResults();
        results.setData("invoice_patientNotified", patientNotified);
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
