package com.salaboy.jbpm5.dev.guide.webservice;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService()
public interface SlowService {

	@WebMethod(operationName = "slowCallOne")
	String slowMethod1(@WebParam(name = "name") String name);
	
	@WebMethod(operationName = "slowCallTwo")
	String slowMethod2(@WebParam(name = "name") String name);
	
	@WebMethod(operationName = "slowCallThree")
	String slowMethod3(@WebParam(name = "name") String name);

}
