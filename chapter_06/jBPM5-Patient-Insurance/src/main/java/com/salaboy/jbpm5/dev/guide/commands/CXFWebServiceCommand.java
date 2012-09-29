package com.salaboy.jbpm5.dev.guide.commands;

import java.io.Serializable;
import javax.inject.Named;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.jbpm.executor.api.Command;
import org.jbpm.executor.api.CommandContext;
import org.jbpm.executor.api.ExecutionResults;

@Named("cxfwebServiceCommand")
public class CXFWebServiceCommand implements Command {

	public ExecutionResults execute(CommandContext ctx) {
		String wsdlUrl = (String) ctx.getData("wsdlUrl");
		String methodName = (String) ctx.getData("methodName");
		String argumentNamesString = (String) ctx.getData("webServiceParameters");
		String outputName = (String) ctx.getData("outputName");
		String[] argumentNames = argumentNamesString.split(",");
		Object[] arguments = new Object[argumentNames.length];
		for (int index = 0; index < argumentNames.length; index++) {
			Object argument = ctx.getData(argumentNames[index]);
			arguments[index] = argument;
		}
		
		JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
		Client client = dcf.createClient(wsdlUrl, CXFWebServiceCommand.class.getClassLoader());
		ExecutionResults results = new ExecutionResults();
		try {
			Object[] result = client.invoke(methodName, arguments);
			if (result == null) {
				System.out.println("Null response");
				results.setData(outputName, null);
			} else {
				System.out.println("Echo response: " + result[0]);
				results.setData(outputName, (Serializable) result[0]);
			}
		} catch (Exception e) {
			results.setData(outputName, e);
                        System.out.println("Exception inside CXFCmd: "+e.getMessage());
			e.printStackTrace(System.out);
		}
		return results;
	}
}
