package com.salaboy.jbpm5.dev.guide.webservice;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService()
public interface InsuranceService {

	@WebMethod(operationName = "isValid")
	Boolean isValid(@WebParam(name = "name") String name);
	
	@WebMethod(operationName = "insurance")
	void insurance(@WebParam(name = "name") String name);
	
	@WebMethod(operationName = "revoke")
	void revoke(@WebParam(name = "name") String name);

	@WebMethod(operationName = "revokeAll")
	void revokeAll();

}
