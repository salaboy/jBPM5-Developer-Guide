package com.salaboy.jbpm5.dev.guide.webservice;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService()
public class SlowServiceImpl implements SlowService {

	@WebMethod(operationName = "slowCallOne")
	public Boolean slowCallOne(@WebParam(name = "name") String name) {
		return null;
	}
	
	@WebMethod(operationName = "slowCallTwo")
	public Boolean slowCallTwo(@WebParam(name = "name") String name) {
		return null;
	}
	
	@WebMethod(operationName = "slowCallThree")
	public Boolean slowCallThree(@WebParam(name = "name") String name) {
		return null;
	}

}
