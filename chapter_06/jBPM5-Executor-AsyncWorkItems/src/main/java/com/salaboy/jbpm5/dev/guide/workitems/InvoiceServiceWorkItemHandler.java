/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.dev.guide.workitems;

import com.salaboy.jbpm5.dev.guide.model.ConceptCode;
import com.salaboy.jbpm5.dev.guide.model.Patient;
import com.salaboy.jbpm5.dev.guide.webservice.InsuranceService;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;

/**
 *
 * @author salaboy
 */
public class InvoiceServiceWorkItemHandler implements WorkItemHandler {

    public void executeWorkItem(WorkItem wi, WorkItemManager wim) {
        Patient patient = (Patient) wi.getParameter("invoice_patient");
        BigDecimal finalAmount = (BigDecimal) wi.getParameter("invoice_finalAmount");
        List<ConceptCode> concepts = (List<ConceptCode>) wi.getParameter("invoice_concepts");
        boolean patientNotified = false;

        InsuranceService client = getClient();
        patientNotified = client.notifyAndChargePatient(patient, finalAmount, concepts);


        System.out.println(" >>> Patient Notified = " + patientNotified);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("invoice_patientNotified", patientNotified);
        wim.completeWorkItem(wi.getId(), result);
    }

    public void abortWorkItem(WorkItem wi, WorkItemManager wim) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

     private InsuranceService getClient() {
        InsuranceService client = null;
        try {
            URL wsdlURL = new URL(
                "http://127.0.0.1:19999/InsuranceServiceImpl/insurance?WSDL");
            QName SERVICE_QNAME = new QName(
                "http://webservice.guide.dev.jbpm5.salaboy.com/",
                "InsuranceServiceImplService");
            Service service = Service.create(wsdlURL, SERVICE_QNAME);
            client = service.getPort(InsuranceService.class);
        } catch (MalformedURLException ex) {
            Logger.getLogger(CompanyGatewayWorkItemHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return client;
    }
}
