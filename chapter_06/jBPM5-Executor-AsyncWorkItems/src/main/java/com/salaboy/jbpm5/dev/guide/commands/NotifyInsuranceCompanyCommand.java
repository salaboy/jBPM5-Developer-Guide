/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.dev.guide.commands;

import com.salaboy.jbpm5.dev.guide.model.ConceptCode;
import com.salaboy.jbpm5.dev.guide.webservice.InsuranceService;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
public class NotifyInsuranceCompanyCommand implements Command{

    public ExecutionResults execute(CommandContext ctx) {
        String patientId = (String) ctx.getData("company_patientName");
        BigDecimal finalAmount = BigDecimal.ZERO;
        try {
            InsuranceService client = getClient();
            //Fixed rate for insured patients
            finalAmount = client.notifyInsuranceCompany("Company 1", patientId, new BigDecimal(100));
            
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        ExecutionResults results = new ExecutionResults();
        results.setData("company_finalAmount", finalAmount);
        List<ConceptCode> concepts = new ArrayList<ConceptCode>(1);
        concepts.add(new ConceptCode("CO-9999", finalAmount, " Insured Flat Rate", 1));
        results.setData("company_concepts", concepts);
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
