/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.salaboy.jbpm5.dev.guide.executor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;


import com.salaboy.jbpm5.dev.guide.executor.entities.RequestInfo;
import com.salaboy.jbpm5.dev.guide.executor.entities.STATUS;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author salaboy
 */
public class ExecutorImpl implements Executor {

    private int waitTime = 5000;
    private EntityManagerFactory emf;
    private int nroOfThreads = 1;
    private static ScheduledExecutorService scheduler;

    public ExecutorImpl() {
    }

    public int getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public EntityManagerFactory getEmf() {
        return emf;
    }

    public void setEmf(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public int getNroOfThreads() {
        return nroOfThreads;
    }

    public void setNroOfThreads(int nroOfThreads) {
        this.nroOfThreads = nroOfThreads;
    }

    public void init() {
        final int THREAD_COUNT = nroOfThreads;



        final Runnable task = new Runnable() {

            public void run() {
                System.out.println(" >>> Waking Up!!!");
                EntityManager em = emf.createEntityManager();
                List<?> resultList = em.createQuery("Select r from RequestInfo as r where r.status ='QUEUED'").getResultList();

                System.out.println(" >>> Number of request pending for execution = " + resultList.size());
                if (resultList.size() > 0) {
                    try {
                        RequestInfo r = (RequestInfo) resultList.get(0);
                        System.out.println(" >> Processing Request Id: " + r.getId());
                        System.out.println(" >> Request Status =" + r.getStatus());
                        Command cmd = (Command) Class.forName(r.getCommandName()).newInstance();
                        CommandContext ctx = null;
                        byte[] reqData = r.getRequestData();
                        if (reqData != null) {
                            try {
                                ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(reqData));
                                ctx = (CommandContext) in.readObject();
                            } catch (IOException e) {
                                ctx = null;
                            }
                        }
                        ExecutionResults results = cmd.execute(ctx);
                        if (ctx != null && ctx.getData("callbacks") != null) {
                            System.out.println(" ### Callback: " + ctx.getData("callbacks"));
                            List<String> callbacks = (List<String>) ctx.getData("callbacks");
                            for (String callback : callbacks) {
                                CommandDoneHandler handler = (CommandDoneHandler) Class.forName(callback).newInstance();
                                handler.onCommandDone(ctx, results);
                            }
                        } else {
                            System.out.println(" ### Callbacks: NULL");
                        }
                        if (results != null) {
                            try {
                                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                                ObjectOutputStream out = new ObjectOutputStream(bout);
                                out.writeObject(results);
                                byte[] respData = bout.toByteArray();
                                r.setResponseData(respData);
                            } catch (IOException e) {
                                r.setResponseData(null);
                            }
                        }
                        em.getTransaction().begin();
                        r.setStatus(STATUS.DONE);
                        em.merge(r);
                        em.getTransaction().commit();
                        em.close();
                    } catch (InstantiationException ex) {
                        Logger.getLogger(ExecutorImpl.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(ExecutorImpl.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(ExecutorImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                System.out.println(" >>> Going to Sleep!!!");

            }
        };



        scheduler = Executors.newScheduledThreadPool(THREAD_COUNT);
        // The scheduler will starts in 3 seconds to allow the applicaiton to initialize
        scheduler.scheduleAtFixedRate(task, 3, waitTime, TimeUnit.MILLISECONDS);

    }

    public Long scheduleRequest(String commandId, CommandContext ctx) {
        
        if (ctx == null) {
            throw new IllegalStateException("A Context Must Be Provided! ");
        }
        String businessKey = (String) ctx.getData("businessKey");
        EntityManager em = emf.createEntityManager();
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setCommandName(commandId);
        requestInfo.setKey(businessKey);
        requestInfo.setStatus(STATUS.QUEUED);
        requestInfo.setMessage("Ready to execute");

        if (ctx != null) {
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                ObjectOutputStream oout = new ObjectOutputStream(bout);
                oout.writeObject(ctx);
                requestInfo.setRequestData(bout.toByteArray());
            } catch (IOException e) {
                requestInfo.setRequestData(null);
            }
        }

        em.getTransaction().begin();
        em.persist(requestInfo);
        em.getTransaction().commit();
        em.close();
        System.out.println(" >>> Scheduling request for Command: "+commandId + " - requestId: "+requestInfo.getId());
        return requestInfo.getId();
    }

    public void cancelRequest(Long requestId) {
        System.out.println(" >>> Before - Cancelling Request with Id: "+requestId);
        EntityManager em = emf.createEntityManager();
        String eql = "Select r from RequestInfo as r where r.status ='QUEUED' and id = :id";
        List<?> result = em.createQuery(eql).setParameter("id", requestId).getResultList();
        if (result.isEmpty()) {
            return;
        }
        RequestInfo r = (RequestInfo) result.iterator().next();
        em.getTransaction().begin();
        r.setStatus(STATUS.CANCELLED);
        em.merge(r);
        em.getTransaction().commit();
        em.close();
        System.out.println(" >>> After - Cancelling Request with Id: "+requestId);
    }

    public void destroy() {
        System.out.println(" >>>>> Destroying Executor!!!!");
        scheduler.shutdown();
        if (emf.isOpen()) {
            emf.close();
        }


    }
}
