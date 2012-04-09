package com.salaboy.jbpm5.dev.guide.workitems;

import com.salaboy.jbpm5.dev.guide.model.Patient;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;

import com.salaboy.jbpm5.dev.guide.webservice.InsuranceService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InsuranceServiceWorkItemHandler implements WorkItemHandler {

    public void executeWorkItem(WorkItem wi, WorkItemManager wim) {

        String patientId = (String) wi.getParameter("insured_patientName");
        boolean isPatientInsured = false;

        InsuranceService client = getClient();
        isPatientInsured = client.isPatientInsured(patientId);

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("insured_isPatientInsured", isPatientInsured);
        wim.completeWorkItem(wi.getId(), result);


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
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        //Do nothing, cannot be aborted
    }
}
