/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.dev.guide.workitems;

import com.salaboy.jbpm5.dev.guide.model.ConceptCode;
import com.salaboy.jbpm5.dev.guide.webservice.InsuranceService;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
public class RatesServiceWorkItemHandler implements WorkItemHandler {

    public void executeWorkItem(WorkItem wi, WorkItemManager wim) {
        String patientId = (String) wi.getParameter("rates_patientName");
        BigDecimal finalAmount = BigDecimal.ZERO;
        //Mock Data
        List<ConceptCode> concepts = new ArrayList<ConceptCode>(2);
        concepts.add(new ConceptCode("CO-123", new BigDecimal(125), "Dialy Hospital Bed Rate", 4));
        concepts.add(new ConceptCode("CO-123", new BigDecimal(100), "Nurse Service", 1));
        InsuranceService client = getClient();
        //Fixed rate for insured patients
        finalAmount = client.calculateHospitalRates(patientId, concepts);

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("rates_finalAmount", finalAmount);
        result.put("rates_concepts", concepts);
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
