package com.salaboy.jbpm5.dev.guide.webservice;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService()
public interface SlowService {

	@WebMethod(operationName = "slowCallOne")
	Boolean slowCallOne(@WebParam(name = "name") String name);
	
	@WebMethod(operationName = "slowCallTwo")
	Boolean slowCallTwo(@WebParam(name = "name") String name);
	
	@WebMethod(operationName = "slowCallThree")
	Boolean slowCallThree(@WebParam(name = "name") String name);

}
