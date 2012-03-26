/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.salaboy.jbpm5.dev.guide.executor;

import com.salaboy.jbpm5.dev.guide.executor.entities.ErrorInfo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;


import com.salaboy.jbpm5.dev.guide.executor.entities.RequestInfo;
import com.salaboy.jbpm5.dev.guide.executor.entities.STATUS;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;
import org.hibernate.exception.ExceptionUtils;

/**
 *
 * @author salaboy
 */
public class ExecutorImpl implements Executor {

    private int waitTime = 5000;
    private EntityManagerFactory emf;
    private int nroOfThreads = 1;
    private int defaultNroOfRetries = 0;
    private static ScheduledExecutorService scheduler;

    public ExecutorImpl() {
    }

    public int getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public int getDefaultNroOfRetries() {
        return defaultNroOfRetries;
    }

    public void setDefaultNroOfRetries(int defaultNroOfRetries) {
        this.defaultNroOfRetries = defaultNroOfRetries;
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
                System.out.println(System.currentTimeMillis()+" >>> Waking Up!!!");
                EntityManager em = emf.createEntityManager();
                List<?> resultList = em.createQuery("Select r from RequestInfo as r where r.status ='QUEUED' or r.status = 'RETRYING' ORDER BY r.time DESC").getResultList();

                System.out.println(" >>> Number of request pending for execution = " + resultList.size());
                if (resultList.size() > 0) {
                    RequestInfo r = null;
                    Throwable exception = null;
                    try {
                        r = (RequestInfo) resultList.get(0);
                        System.out.println(" >> Processing Request Id: " + r.getId());
                        System.out.println(" >> Request Status =" + r.getStatus());
                        System.out.println(" >> Command Name to execute = "+r.getCommandName());
                        Command cmd = (Command) Class.forName(r.getCommandName()).newInstance();
                        CommandContext ctx = null;
                        byte[] reqData = r.getRequestData();
                        if (reqData != null) {
                            try {
                                ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(reqData));
                                ctx = (CommandContext) in.readObject();
                            } catch (IOException e) {
                                ctx = null;
                                e.printStackTrace();
                            }
                        }
                        ExecutionResults results = cmd.execute(ctx);
                        if (ctx != null && ctx.getData("callbacks") != null) {
                            System.out.println(" ### Callback: " + ctx.getData("callbacks"));
                            String[] callbacksArray = ((String)ctx.getData("callbacks")).split(",");;
                            List<String> callbacks = (List<String>) Arrays.asList(callbacksArray);
                            for (String callback : callbacks) {
                                CommandCallback handler = (CommandCallback) Class.forName(callback).newInstance();
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
                    
                    } catch (Exception e) {
                        e.printStackTrace();
                        exception = e;
                    }
                    if(exception != null){
                        System.out.println(System.currentTimeMillis()+" >>> Before - Error Found!!!"+exception.getMessage());
                        
                        em.getTransaction().begin();
                        
                        ErrorInfo errorInfo = new ErrorInfo(exception.getMessage(), ExceptionUtils.getFullStackTrace(exception.fillInStackTrace()));
                        errorInfo.setRequestInfo(r);
                        r.getErrorInfo().add(errorInfo);
                        System.out.println(" >>> Number of Error: "+r.getErrorInfo().size());
                        if( r.getRetries() > 0 ){
                            r.setStatus(STATUS.RETRYING);
                            r.setRetries(r.getRetries()-1);
                            r.setExecutions(r.getExecutions()+1);
                            System.out.println(System.currentTimeMillis()+" >>> Retrying ("+r.getRetries()+") still available!");
                        }else{
                            System.out.println(System.currentTimeMillis()+" >>> Error no retries left!");
                            r.setStatus(STATUS.ERROR);
                            r.setExecutions(r.getExecutions()+1);
                        }
                        
                        em.merge(r);
                        //em.persist(errorInfo);
                        em.getTransaction().commit();
                        em.close();
                        System.out.println(System.currentTimeMillis()+" >>> After - Error Found!!!"+exception.getMessage());
                        
                    
                    }else{
                        
                        em.getTransaction().begin();
                        r.setStatus(STATUS.DONE);
                        em.merge(r);
                        em.getTransaction().commit();
                        em.close();
                    }
                }
                System.out.println(" >>> Going to Sleep!!!");

            }
        };


        System.out.println(" >>> Starting Executor Component with Thread Pool Size: "+THREAD_COUNT);
        scheduler = Executors.newScheduledThreadPool(THREAD_COUNT);
        scheduler.scheduleAtFixedRate(task, 0, waitTime, TimeUnit.MILLISECONDS);

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
        if(ctx.getData("retries")!= null){
            requestInfo.setRetries((Integer)ctx.getData("retries"));
        }else{
            requestInfo.setRetries(defaultNroOfRetries);
        }
        if (ctx != null) {
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                ObjectOutputStream oout = new ObjectOutputStream(bout);
                oout.writeObject(ctx);
                requestInfo.setRequestData(bout.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
                requestInfo.setRequestData(null);
            }
        }

        em.getTransaction().begin();
        em.persist(requestInfo);
        em.getTransaction().commit();
        em.close();
        System.out.println(" >>> Scheduling request for Command: " + commandId + " - requestId: " + requestInfo.getId()+" with "+requestInfo.getRetries()+" retries");
        return requestInfo.getId();
    }

    public void cancelRequest(Long requestId) {
        System.out.println(" >>> Before - Cancelling Request with Id: " + requestId);
        EntityManager em = emf.createEntityManager();
        
        String eql = "Select r from RequestInfo as r where (r.status ='QUEUED' or r.status ='RETRYING') and id = :id";
        List<?> result = em.createQuery(eql).setParameter("id", requestId).getResultList();
        if (result.isEmpty()) {
            return;
        }
        RequestInfo r = (RequestInfo) result.iterator().next();
        
        em.getTransaction().begin();
        em.lock(r, LockModeType.READ);
        r.setStatus(STATUS.CANCELLED);
        em.merge(r);
        em.getTransaction().commit();
        
        em.close();
        System.out.println(" >>> After - Cancelling Request with Id: " + requestId);
    }

    public void destroy() {
        System.out.println(" >>>>> Destroying Executor!!!!");
        if (emf.isOpen()) {
            emf.close();
        }
        if(scheduler != null){
            scheduler.shutdownNow();
        }
        


    }
}
