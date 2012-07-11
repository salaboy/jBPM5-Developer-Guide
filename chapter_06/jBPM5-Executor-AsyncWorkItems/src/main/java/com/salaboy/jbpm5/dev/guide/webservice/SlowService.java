package com.salaboy.jbpm5.dev.guide.webservice;

import javax.jws.WebService;

@WebService()
public interface SlowService {

    String slowMethod1(String name);

    String slowMethod2(String name);

    String slowMethod3(String name);
}
