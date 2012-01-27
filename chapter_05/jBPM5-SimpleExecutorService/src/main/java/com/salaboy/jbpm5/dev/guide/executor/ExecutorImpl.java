/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.salaboy.jbpm5.dev.guide.executor;

import com.salaboy.jbpm5.dev.guide.executor.entities.RequestInfo;
import com.salaboy.jbpm5.dev.guide.executor.entities.STATUS;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 *
 * @author salaboy
 */
public class ExecutorImpl implements Executor {

    private static EntityManagerFactory emf;
    private EntityManager em;

    public ExecutorImpl() {
        emf = Persistence.createEntityManagerFactory("org.jbpm.executor");
        em = emf.createEntityManager();
    }

    public void init() {
        
        new Thread() {

            @Override
            public void run() {
                while (true) {
                    try {
                        System.out.println("Sleeping ...");
                        Thread.sleep(5000);
                        System.out.println("Waking Up! ...");
                        try {
                            List resultList = em.createQuery("Select r from RequestInfo as r where r.status ='QUEUED'").getResultList();
                            
                            System.out.println("Number of request pending for execution = "+resultList.size());
                            if (resultList.size() > 0) {
                                
                                RequestInfo r = (RequestInfo) resultList.get(0);
                                System.out.println("Request Status =" +r.getStatus());
                                Command cmd = (Command) Class.forName(r.getCommandName()).newInstance();
                                cmd.execute();
                                em.getTransaction().begin();
                                r.setStatus(STATUS.DONE);
                                em.getTransaction().commit();
                            }
                        } catch (InstantiationException ex) {
                            Logger.getLogger(ExecutorImpl.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IllegalAccessException ex) {
                            Logger.getLogger(ExecutorImpl.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(ExecutorImpl.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ExecutorImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }.start();
    }

    public void schedule(String requestName) {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setCommandName(requestName);
        requestInfo.setKey("HI");
        requestInfo.setStatus(STATUS.QUEUED);
        requestInfo.setMessage("Ready to execute");
        em.getTransaction().begin();
        em.persist(requestInfo);
        em.getTransaction().commit();



    }

    public void destroy() {
        em.close();
        emf.close();
    }
}
