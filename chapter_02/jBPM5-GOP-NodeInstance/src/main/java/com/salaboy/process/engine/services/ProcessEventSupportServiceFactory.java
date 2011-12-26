/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.process.engine.services;

/**
 *
 * @author salaboy
 */
public class ProcessEventSupportServiceFactory {
    public static ProcessEventSupportService getService(){
        return new DefaultProcessEventSupportService();
    }
}
