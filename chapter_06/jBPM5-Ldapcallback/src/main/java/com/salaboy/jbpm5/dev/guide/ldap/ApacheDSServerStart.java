/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.dev.guide.ldap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.ldap.server.ApacheDSContainer;

/**
 *
 * @author salaboy
 */
public class ApacheDSServerStart {

    private static ApacheDSContainer container;

    private static Logger logger = LoggerFactory.getLogger(ApacheDSServerStart.class);
    
    public static void main(String[] args) throws Exception {
        logger.info(" >>> Starting Apache DS LDAP Server ... ");
        container = new ApacheDSContainer("o=mojo", "classpath:test.ldif");
        container.setPort(9898);
        container.afterPropertiesSet();
        logger.info(" >>> Apache DS LDAP Server Started !");
        
        
    }
}
