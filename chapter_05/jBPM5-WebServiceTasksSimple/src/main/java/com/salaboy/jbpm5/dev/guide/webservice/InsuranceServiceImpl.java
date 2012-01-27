package com.salaboy.jbpm5.dev.guide.webservice;

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService()
public class InsuranceServiceImpl implements InsuranceService {

	private static final List<String> INSURANCES = new ArrayList<String>();

	@WebMethod(operationName = "isValid")
	public Boolean isValid(@WebParam(name = "name") String name) {
		return (INSURANCES.contains(name));
	}
	
	@WebMethod(operationName = "insurance")
	public void insurance(@WebParam(name = "name") String name) {
		INSURANCES.add(name);
	}
	
	@WebMethod(operationName = "revoke")
	public void revoke(@WebParam(name = "name") String name) {
		INSURANCES.remove(name);
	}
	
	@WebMethod(operationName = "revokeAll")
	public void revokeAll() {
		INSURANCES.clear();
	}
}
