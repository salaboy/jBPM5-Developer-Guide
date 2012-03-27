package com.salaboy.jbpm5.dev.guide.webservice;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebService;

@WebService()
public class SlowServiceImpl implements SlowService {

    public SlowServiceImpl() {
    }

    public String slowMethod1( String name) {
        int i = 0;
        while (i < 10) {
            System.out.println(" >>> Remote System Processing 1 ...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(SlowServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            i++;
        }
        return "Seccess 1";
    }

    public String slowMethod2( String name) {
        try {
            System.out.println(" >>> Remote System Processing 2 ...");
            Thread.sleep(1000);
            System.out.println(" >>> Remote System Processing 2 ...");
        } catch (InterruptedException ex) {
            Logger.getLogger(SlowServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Success 2";
    }

    public String slowMethod3( String name) {
        try {
            System.out.println(" >>> Remote System Processing 2 ...");
            Thread.sleep(3000);
            System.out.println(" >>> Remote System Processing 2 ...");
        } catch (InterruptedException ex) {
            Logger.getLogger(SlowServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Success 3";
    }
}
