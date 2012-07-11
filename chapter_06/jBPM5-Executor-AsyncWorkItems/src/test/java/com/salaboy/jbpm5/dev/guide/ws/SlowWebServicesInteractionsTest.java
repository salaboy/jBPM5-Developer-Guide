package com.salaboy.jbpm5.dev.guide.ws;


import com.salaboy.jbpm5.dev.guide.SessionStoreUtil;
import com.salaboy.jbpm5.dev.guide.executor.Executor;
import com.salaboy.jbpm5.dev.guide.executor.wih.AsyncGenericWorkItemHandler;
import com.salaboy.jbpm5.dev.guide.webservice.SlowService;
import com.salaboy.jbpm5.dev.guide.webservice.SlowServiceImpl;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.xml.ws.Endpoint;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.WorkingMemory;
import org.drools.builder.*;
import org.drools.event.RuleFlowGroupActivatedEvent;
import org.drools.event.RuleFlowGroupDeactivatedEvent;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.io.impl.ClassPathResource;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkflowProcessInstance;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Server;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class SlowWebServicesInteractionsTest {

    protected Executor executor;
    protected EntityManagerFactory emf;
    protected StatefulKnowledgeSession session;
    private Endpoint endpoint;
    private SlowService service;
    private ApplicationContext ctx;
    private Server server;
    public SlowWebServicesInteractionsTest() {
    }

    @Before
    public void setUp() throws Exception {
        DeleteDbFiles.execute("~", "mydb", false);
        try {
            server = Server.createTcpServer(new String[]{"-tcp", "-tcpAllowOthers", "-tcpDaemon", "-trace"}).start();
        } catch (SQLException ex) {
            System.out.println("ex: " + ex);
        }
        initializeExecutionEnvironment();
        initializeWebService();
        
        
    }

    @After
    public void tearDown() {
        executor.destroy();
        server.stop();
        this.endpoint.stop();
    }

    private void initializeWebService() {
        this.service = new SlowServiceImpl();
        this.endpoint = Endpoint.publish(
                "http://127.0.0.1:19999/SlowServiceImpl/slow",
                service);
    }

   

    protected void initializeExecutionEnvironment() throws Exception {
        ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        executor = (Executor) ctx.getBean("executorService");
        executor.init();
        emf = ((EntityManagerFactory)ctx.getBean("entityManagerFactory"));
    }

    

    private void initializeSession(String processName) {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(new ClassPathResource(processName), ResourceType.BPMN2);
        if (kbuilder.hasErrors()) {
            KnowledgeBuilderErrors errors = kbuilder.getErrors();

            for (KnowledgeBuilderError error : errors) {
                System.out.println(">>> Error:" + error.getMessage());

            }
            throw new IllegalStateException(">>> Knowledge couldn't be parsed! ");
        }

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        session = kbase.newStatefulKnowledgeSession();
        KnowledgeRuntimeLoggerFactory.newConsoleLogger(session);

        ((StatefulKnowledgeSessionImpl) session).session.addEventListener(new org.drools.event.AgendaEventListener() {

            public void activationCreated(org.drools.event.ActivationCreatedEvent event, WorkingMemory workingMemory) {
            }

            public void activationCancelled(org.drools.event.ActivationCancelledEvent event, WorkingMemory workingMemory) {
            }

            public void beforeActivationFired(org.drools.event.BeforeActivationFiredEvent event, WorkingMemory workingMemory) {
            }

            public void afterActivationFired(org.drools.event.AfterActivationFiredEvent event, WorkingMemory workingMemory) {
            }

            public void agendaGroupPopped(org.drools.event.AgendaGroupPoppedEvent event, WorkingMemory workingMemory) {
            }

            public void agendaGroupPushed(org.drools.event.AgendaGroupPushedEvent event, WorkingMemory workingMemory) {
            }

            public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event, WorkingMemory workingMemory) {
            }

            public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event, WorkingMemory workingMemory) {
                workingMemory.fireAllRules();
            }

            public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event, WorkingMemory workingMemory) {
            }

            public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event, WorkingMemory workingMemory) {
            }
        });
    }

    @Test
    public void testSlowWebServicesNoWait() throws InterruptedException {
        
        initializeSession("three-systems-interactions-nowait.bpmn");
        
        HashMap<String, Object> input = new HashMap<String, Object>();

        String patientName = "John Doe";
        input.put("bedrequest_patientname", patientName);
       
        
        AsyncGenericWorkItemHandler webServiceHandler = new AsyncGenericWorkItemHandler(executor,session.getId());
        session.getWorkItemManager().registerWorkItemHandler("Slow Web Service", webServiceHandler);

        WorkflowProcessInstance pI = (WorkflowProcessInstance) session.startProcess("Three Systems Interactions", input);

        assertEquals(ProcessInstance.STATE_COMPLETED, pI.getState());
        
        Thread.sleep(25000);
        
        EntityManager em = emf.createEntityManager();
        List resultList = em.createNamedQuery("ExecutedRequests").getResultList();
        assertEquals(3, resultList.size());
    }
    
     @Test
    public void testSlowWebServicesWait() throws InterruptedException {
         
        initializeSession("three-systems-interactions-wait.bpmn"); 
        
        SessionStoreUtil.sessionCache.put("sessionId="+session.getId(), session);
        HashMap<String, Object> input = new HashMap<String, Object>();
        
        String patientName = "John Doe";
        input.put("bedrequest_patientname", patientName);
        
        
        AsyncGenericWorkItemHandler webServiceHandler = new AsyncGenericWorkItemHandler(executor,session.getId());
        session.getWorkItemManager().registerWorkItemHandler("Slow Web Service", webServiceHandler);

        WorkflowProcessInstance pI = (WorkflowProcessInstance) session.startProcess("Three Systems Interactions", input);

        assertEquals(ProcessInstance.STATE_ACTIVE, pI.getState());
        
        Thread.sleep(25000);
        
        EntityManager em = emf.createEntityManager();
        List resultList = em.createNamedQuery("ExecutedRequests").getResultList();
        assertEquals(3, resultList.size());
        
        assertEquals(ProcessInstance.STATE_COMPLETED, pI.getState());
    }
}
