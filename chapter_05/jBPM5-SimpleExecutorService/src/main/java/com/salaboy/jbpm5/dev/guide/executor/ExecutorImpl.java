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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author salaboy
 */
public class ExecutorImpl implements Executor {

    private static boolean running = false;
    private int waitTime = 5000;
    private EntityManagerFactory emf;
    private int nroOfThreads = 1;
    private static ExecutorService executorService;

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

        running = true;

        final Runnable task = new Runnable() {

            public void run() {

                EntityManager em = emf.createEntityManager();
                List<?> resultList = em.createQuery("Select r from RequestInfo as r where r.status ='QUEUED'").getResultList();

                System.out.println("Number of request pending for execution = " + resultList.size());
                if (resultList.size() > 0) {
                    try {
                        RequestInfo r = (RequestInfo) resultList.get(0);
                        System.out.println("Request Status =" + r.getStatus());
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
                        if(ctx != null && ctx.getData("callback") != null){
                            System.out.println(" ### Callback: "+ctx.getData("callback"));
                            CommandDoneHandler handler = (CommandDoneHandler) Class.forName(ctx.getData("callback").toString()).newInstance();
                            ctx.setData("key", r.getKey());
                            handler.onCommandDone(ctx, results);
                        }else{
                            System.out.println(" ### Callback: NULL");
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

            }
        };



        // Execute tasks
        //
        executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        new Thread() {

            @Override
            public void run() {
                while (running) {

                    
                    System.out.println("Submiting Task ..." + task + "in executor = "+executorService.toString());
                    executorService.execute(task);
                    System.out.println("Going to sleep for ..."+waitTime/THREAD_COUNT+" ms.");
                    try {
                        Thread.currentThread().sleep(waitTime/THREAD_COUNT);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ExecutorImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.println("Waking Up! ...");

                }
            }
        }.start();



    }

    public void scheduleRequest(String requestName, CommandContext ctx) {
        if(ctx == null){
            throw new IllegalStateException("A Context Must Be Provided! ");
        }
        String businessKey = (String) ctx.getData("businessKey");
        EntityManager em = emf.createEntityManager();
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setCommandName(requestName);
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
    }

    public void cancelRequest(String key) {
        EntityManager em = emf.createEntityManager();
        String eql = "Select r from RequestInfo as r where r.status ='QUEUED' and key = :key";
        List<?> result = em.createQuery(eql).setParameter("key", key).getResultList();
        if (result.isEmpty()) {
            return;
        }
        RequestInfo r = (RequestInfo) result.iterator().next();
        em.getTransaction().begin();
        em.remove(r);
        em.getTransaction().commit();
        em.close();
    }

    public void destroy() {
        System.out.println(" >>>>> Destroying Executor!!!!");
        running = false;
        executorService.shutdown();
        if (emf.isOpen()) {
            emf.close();
        }
        
        
    }
}
