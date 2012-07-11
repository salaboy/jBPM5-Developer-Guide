/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.dev.guide.webservice;

import java.util.HashMap;
import java.util.Map;
import javax.jws.WebService;

/**
 *
 * @author salaboy
 */
@WebService()
public class SimpleValidationServiceImpl implements SimpleValidationService {
    public static Map<String, Boolean> insuredPatients = new HashMap<String, Boolean>();

    public SimpleValidationServiceImpl() {
    }
    
    
    @Override
    public boolean validate(String patientId){
        return insuredPatients.get(patientId);
    }
}
