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
public class CompanyGatewayWorkItemHandler implements WorkItemHandler {

    public void executeWorkItem(WorkItem wi, WorkItemManager wim) {
        String patientId = (String) wi.getParameter("company_patientName");
        BigDecimal finalAmount = BigDecimal.ZERO;
        InsuranceService client = getClient();
        //Fixed rate for insured patients
        finalAmount = client.notifyInsuranceCompany("Company 1", patientId, new BigDecimal(100));

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("company_finalAmount", finalAmount);
        List<ConceptCode> concepts = new ArrayList<ConceptCode>(1);
        concepts.add(new ConceptCode("CO-9999", finalAmount, " Insured Flat Rate", 1));
        result.put("company_concepts", concepts);
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
