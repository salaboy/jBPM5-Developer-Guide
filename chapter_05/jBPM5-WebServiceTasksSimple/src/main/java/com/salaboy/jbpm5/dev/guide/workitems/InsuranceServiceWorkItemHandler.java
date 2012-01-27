package com.salaboy.jbpm5.dev.guide.workitems;

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

public class InsuranceServiceWorkItemHandler implements WorkItemHandler {

	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		long workItemId = workItem.getId();
		Map<String, Object> input = workItem.getParameters();
		
		String outputName = (String) input.get("outputName");
		String clientNameVariable = (String) input.get("clientNameVariable");
		String clientName = (String) input.get(clientNameVariable);
		try {
			URL wsdlURL = new URL(
					"http://127.0.0.1:9999/WebServiceExample/insurance?WSDL");
			QName SERVICE_QNAME = new QName(
					"http://webservice.guide.dev.jbpm5.salaboy.com/", 
					"InsuranceServiceImplService");
			Service service = Service.create(wsdlURL, SERVICE_QNAME);
			InsuranceService client = service.getPort(InsuranceService.class);
			Boolean result = client.isValid(clientName);
			Map<String, Object> output = new HashMap<String, Object>();
			if (result == null) {
				System.out.println("Null response");
				output.put(outputName, null);
			} else {
				System.out.println("Echo response: " + result);
				output.put(outputName, result);
			}
			manager.completeWorkItem(workItemId, output);
		} catch (MalformedURLException e) {
			e.printStackTrace(System.out);
			//This shouldn't happen. {wsdlURL} is a hardcoded URL
		}
	}

	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		//Do nothing, cannot be aborted
	}

}
