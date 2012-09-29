package com.salaboy.jbpm5.dev.guide.ws;

import com.salaboy.jbpm5.dev.guide.util.SessionStoreUtil;
import com.salaboy.jbpm5.dev.guide.webservice.SlowService;
import com.salaboy.jbpm5.dev.guide.webservice.SlowServiceImpl;
import com.salaboy.jbpm5.dev.guide.workitems.AsyncGenericWorkItemHandler;
import java.util.HashMap;
import java.util.List;
import javax.xml.ws.Endpoint;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.*;
import org.drools.event.rule.DefaultAgendaEventListener;
import org.drools.io.impl.ClassPathResource;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkflowProcessInstance;
import org.jbpm.executor.ExecutorModule;
import org.jbpm.executor.ExecutorServiceEntryPoint;
import org.jbpm.executor.entities.RequestInfo;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class SlowWebServicesInteractionsTest {

    protected ExecutorServiceEntryPoint executor;
    protected StatefulKnowledgeSession session;
    private Endpoint endpoint;
    private SlowService service;

    public SlowWebServicesInteractionsTest() {
    }

    @Before
    public void setUp() throws Exception {
        initializeExecutionEnvironment();
        initializeWebService();
    }

    @After
    public void tearDown() {
        executor.clearAllRequests();
        executor.clearAllErrors();
        executor.destroy();
        this.endpoint.stop();
    }

    private void initializeWebService() {
        this.service = new SlowServiceImpl();
        this.endpoint = Endpoint.publish(
                "http://127.0.0.1:19999/SlowServiceImpl/slow",
                service);
    }

    protected void initializeExecutionEnvironment() throws Exception {
        executor = ExecutorModule.getInstance().getExecutorServiceEntryPoint();
        executor.setThreadPoolSize(1);
        executor.setInterval(3);
        executor.init();
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

        session.addEventListener(new DefaultAgendaEventListener() {
            @Override
            public void afterRuleFlowGroupActivated(org.drools.event.rule.RuleFlowGroupActivatedEvent event) {
                session.fireAllRules();
            }
        });
    }

    @Test
    public void testSlowWebServicesNoWait() throws InterruptedException {

        initializeSession("three-systems-interactions-nowait.bpmn");

        HashMap<String, Object> input = new HashMap<String, Object>();

        String patientName = "John Doe";
        input.put("bedrequest_patientname", patientName);


        AsyncGenericWorkItemHandler webServiceHandler = new AsyncGenericWorkItemHandler(executor, session.getId());
        session.getWorkItemManager().registerWorkItemHandler("Slow Web Service", webServiceHandler);

        WorkflowProcessInstance pI = (WorkflowProcessInstance) session.startProcess("Three Systems Interactions", input);

        assertEquals(ProcessInstance.STATE_COMPLETED, pI.getState());

        Thread.sleep(25000);

        List<RequestInfo> resultList = executor.getExecutedRequests();
        assertEquals(3, resultList.size());
        session.dispose();
    }

    @Test
    public void testSlowWebServicesWait() throws InterruptedException {

        initializeSession("three-systems-interactions-wait.bpmn");

        SessionStoreUtil.sessionCache.put("sessionId=" + session.getId(), session);
        HashMap<String, Object> input = new HashMap<String, Object>();

        String patientName = "John Doe";
        input.put("bedrequest_patientname", patientName);


        AsyncGenericWorkItemHandler webServiceHandler = new AsyncGenericWorkItemHandler(executor, session.getId());
        session.getWorkItemManager().registerWorkItemHandler("Slow Web Service", webServiceHandler);

        WorkflowProcessInstance pI = (WorkflowProcessInstance) session.startProcess("Three Systems Interactions", input);

        assertEquals(ProcessInstance.STATE_ACTIVE, pI.getState());

        Thread.sleep(25000);

        List<RequestInfo> resultList = executor.getExecutedRequests();
        assertEquals(3, resultList.size());

        assertEquals(ProcessInstance.STATE_COMPLETED, pI.getState());
        session.dispose();
    }
}
