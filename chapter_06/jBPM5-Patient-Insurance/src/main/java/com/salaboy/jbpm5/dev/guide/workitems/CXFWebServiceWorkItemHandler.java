package com.salaboy.jbpm5.dev.guide.workitems;

import java.util.HashMap;
import java.util.Map;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;

/**
 * Synchronous Work Item Handler that invokes a web service.
 * The wsdl, methodName and parameters can be specified though its input
 * parameters.
 * @author esteban
 */
public class CXFWebServiceWorkItemHandler implements WorkItemHandler {
	
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		long workItemId = workItem.getId();
		Map<String, Object> input = workItem.getParameters();
		
		String wsdlUrl = (String) input.get("wsdlUrl");
		String methodName = (String) input.get("methodName");
		String argumentNamesString = (String) input.get("webServiceParameters");
		String outputName = (String) input.get("outputName");
		String[] argumentNames = argumentNamesString.split(",");
		Object[] arguments = new Object[argumentNames.length];
		for (int index = 0; index < argumentNames.length; index++) {
			Object argument = input.get(argumentNames[index]);
			arguments[index] = argument;
		}
		
		JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
		Client client = dcf.createClient(wsdlUrl);
		try {
			Object[] result = client.invoke(methodName, arguments);
			
			Map<String, Object> output = new HashMap<String, Object>();
			
			if (result == null) {
				System.out.println("Null response");
				output.put(outputName, null);
			} else {
				System.out.println("Echo response: " + result[0]);
				output.put(outputName, result[0]);
			}
			
			manager.completeWorkItem(workItemId, output);
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		//Do nothing, cannot be aborted
	}
}
