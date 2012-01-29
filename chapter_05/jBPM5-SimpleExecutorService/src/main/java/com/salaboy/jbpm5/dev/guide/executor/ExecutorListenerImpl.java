package com.salaboy.jbpm5.dev.guide.executor;

import java.io.ByteArrayInputStream; 
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import com.salaboy.jbpm5.dev.guide.executor.entities.RequestInfo;
import com.salaboy.jbpm5.dev.guide.executor.entities.STATUS;

public class ExecutorListenerImpl implements ExecutorListener {

    private EntityManager em;
    private CommandDoneHandler handler;
    private Thread running = null;
    
    private int waitTime = 2000; //default is smaller than ExecutorImpl
    private String executionKey;

    public ExecutorListenerImpl() {
    }
    
    public int getWaitTime() {
		return waitTime;
	}

	public void setWaitTime(int waitTime) {
		this.waitTime = waitTime;
	}
	
	public String getExecutionKey() {
		return executionKey;
	}

	public void setExecutionKey(String executionKey) {
		this.executionKey = executionKey;
	}
	
	public void setEntityManager(EntityManager em) {
		this.em = em;
	}
	
	public void init() {
        if (this.running != null) {
        	throw new IllegalArgumentException("Executor already running");
        }
        this.running = new Thread() {
            @Override
            public void run() {
            	boolean found = false;
                while (!found) {
                    try {
                        System.out.println("Sleeping ...");
                        Thread.sleep(waitTime);
                        System.out.println("Waking Up! ...");
                        try {
                            List<?> resultList = em.createQuery("Select r from RequestInfo as r where r.status ='DONE'").getResultList();
                            
                            System.out.println("Number of request pending for execution = "+resultList.size());
                            if (resultList.size() > 0) {
                                
                                RequestInfo r = (RequestInfo) resultList.get(0);
                                System.out.println("Request Status =" +r.getStatus());
                                byte[] rData = r.getRequestData();
                                CommandContext ctx = null;
                                if (rData != null) {
                                	try {
                                		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(rData));
                                		ctx = (CommandContext) in.readObject();
                                	} catch (IOException e) {
                                		ctx = null;
                                	}
                                }
                                handler.onCommandDone(ctx);
                                em.getTransaction().begin();
                                r.setStatus(STATUS.NOTIFIED);
                                em.getTransaction().commit();
                                found = true;
                            }
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(ExecutorImpl.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ExecutorImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
        running.start();
    }

    public void schedule(String requestName, String key, CommandContext ctx) {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setCommandName(requestName);
        requestInfo.setKey(key);
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
    }

    public void setHandler(CommandDoneHandler handler) {
    	this.handler = handler;
    }
    
    public CommandDoneHandler getHandler() {
    	return handler;
    }
    
    public void unschedule(String key) {
    	String eql = "Select r from RequestInfo as r where r.status ='QUEUED' and key = :key";
		List<?> result = em.createQuery(eql).setParameter("key", key).getResultList();
		if (result.isEmpty()) {
			return;
		}
		RequestInfo r = (RequestInfo) result.iterator().next();
		em.getTransaction().begin();
		em.remove(r);
		em.getTransaction().commit();
    }
    
    public void destroy() {
        em.close();
        if (running != null && running.isAlive()) {
        	running.interrupt();
        }
    }
    
    public void join() throws InterruptedException {
    	running.join();
    }

}
