/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.salaboy.jbpm5.dev.guide;

import com.salaboy.jbpm5.dev.guide.util.SessionStoreUtil;
import com.salaboy.jbpm5.dev.guide.commands.CheckInCommand;
import com.salaboy.jbpm5.dev.guide.workitems.AsyncGenericWorkItemHandler;
import java.util.HashMap;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.event.rule.DefaultAgendaEventListener;
import org.drools.io.impl.ClassPathResource;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkflowProcessInstance;
import org.jbpm.executor.ExecutorModule;
import org.jbpm.executor.ExecutorServiceEntryPoint;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author salaboy
 */
public class AsyncWorkItemWaitForCompletionTest {

    protected ExecutorServiceEntryPoint executor;
    protected StatefulKnowledgeSession session;
    

    public AsyncWorkItemWaitForCompletionTest() {
    }

    @Before
    public void setUp() throws Exception {
       
        initializeExecutionEnvironment();
        initializeSession();
    }

    @After
    public void tearDown() {
        executor.clearAllRequests();
        executor.clearAllErrors();
        executor.destroy();
        SessionStoreUtil.clean();
        
        SessionStoreUtil.clean();
        
    }

    protected void initializeExecutionEnvironment() throws Exception {
        
        CheckInCommand.reset();
        executor = ExecutorModule.getInstance().getExecutorServiceEntryPoint();
        executor.setThreadPoolSize(1);
        executor.setInterval(3);
        executor.init();
    }

    

    private void initializeSession() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        //We are using a proces containing a single Task. The task is configured
        //to wait until the completion of the external system. A specialized
        //callback (CompleteWorkItemCallback) is used to complete the work item 
        //after the command was executed.
        kbuilder.add(new ClassPathResource("async-work-item-wait.bpmn"), ResourceType.BPMN2);
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

        session.addEventListener(new DefaultAgendaEventListener(){

            @Override
            public void afterRuleFlowGroupActivated(org.drools.event.rule.RuleFlowGroupActivatedEvent event) {
                session.fireAllRules();
            }
            
        });
        
        //Registers the session in SessionStoreUtil. The callback we are using
        //for the process' Task will retrieve this session later and completes
        //the work item using it.
        SessionStoreUtil.sessionCache.put("sessionId="+session.getId(), session);

    }

    /**
     * AsyncWorkItemWaitForCompletionTest: Test executing a process with a 
     * single Task using the Executor Service component to interact with a (mocked) 
     * external service. The process is configured to tell the work item handler being 
     * used (AsyncGenericWorkItemHandler) to wait until the external system comes 
     * back before completing the Task. The result will be a process that will is not 
     * going to be completed until the external system comes back with a response.
     * @throws InterruptedException 
     */
    @Test
    public void executorCheckInTestFinishesWithoutHandler() throws InterruptedException {
        HashMap<String, Object> input = new HashMap<String, Object>();

        String patientName = "John Doe";
        input.put("bedrequest_patientname", patientName);

        //Registers an instance of AsyncGenericWorkItemHandler as a handler for
        //all the 'Async Work' tasks in the processes.
        AsyncGenericWorkItemHandler asyncHandler = new AsyncGenericWorkItemHandler(executor, session.getId());
        session.getWorkItemManager().registerWorkItemHandler("Async Work", asyncHandler);

        WorkflowProcessInstance pI = (WorkflowProcessInstance) session.startProcess("PatientCheckIn", input);

        //No command was executed yet.
        assertEquals(0, CheckInCommand.getCheckInCount());
        
        //Since we are waiting for the execution of the external system to be done,
        //the process is still ACTIVE.
        assertEquals(ProcessInstance.STATE_ACTIVE, pI.getState());

        
        Thread.sleep(1000);
        
        //After 1 second, the command is not yet executed meaning that the process
        //is still ACTIVE.
        assertEquals(0, CheckInCommand.getCheckInCount());
        assertEquals(ProcessInstance.STATE_ACTIVE, pI.getState());
        
        
        Thread.sleep(executor.getInterval()*1000);

        //After a reasonable time, the command must be executed. This should
        //have caused the execution of the callback and hence the completion
        //of the work item and thus the process.
        assertEquals(1, CheckInCommand.getCheckInCount());
        assertEquals(ProcessInstance.STATE_COMPLETED, pI.getState());
    }

}
