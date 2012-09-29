/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.dev.guide.commands;

import com.salaboy.jbpm5.dev.guide.model.ConceptCode;
import com.salaboy.jbpm5.dev.guide.webservice.InsuranceService;
import com.salaboy.jbpm5.dev.guide.workitems.PatientDataServiceWorkItemHandler;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class CalculateHospitalRatesCommand implements Command{

    public ExecutionResults execute(CommandContext ctx) {
        String patientId = (String) ctx.getData("rates_patientName");
        BigDecimal finalAmount = BigDecimal.ZERO;
        //Mock Data
        List<ConceptCode> concepts = new ArrayList<ConceptCode>(2);
        concepts.add(new ConceptCode("CO-123", new BigDecimal(125), "Dialy Hospital Bed Rate", 4));
        concepts.add(new ConceptCode("CO-123", new BigDecimal(100), "Nurse Service", 1));
        try {
            InsuranceService client = getClient();
            //Fixed rate for insured patients
            finalAmount = client.calculateHospitalRates(patientId, concepts);

        } catch (MalformedURLException ex) {
            Logger.getLogger(PatientDataServiceWorkItemHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        ExecutionResults results = new ExecutionResults();
        results.setData("rates_finalAmount", finalAmount);
        results.setData("rates_concepts", concepts);
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
