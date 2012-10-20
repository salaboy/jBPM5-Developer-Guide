package com.salaboy.jbpm5.multi_process_evaluation;

import com.salaboy.model.Customer;
import com.salaboy.model.Resources;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;

import org.drools.event.process.DefaultProcessEventListener;
import org.drools.event.process.ProcessNodeLeftEvent;
import org.drools.event.process.ProcessNodeTriggeredEvent;
import org.drools.event.rule.*;
import org.drools.io.impl.ClassPathResource;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.rule.ConsequenceException;
import org.drools.runtime.rule.QueryResults;
import org.jbpm.workflow.instance.WorkflowRuntimeException;

import org.junit.Test;

/*
 * For a more detailed description about these example look at: 
 * https://github.com/Salaboy/jBPM5-Developer-Guide/blob/master/chapter_09/README
 */

public class MultiProcessEvaluationTest {

    @Test
    public void testMultiProcessEvaluation() {

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(new ClassPathResource("multi_process_evaluation/resources.drl"), ResourceType.DRL);
        kbuilder.add(new ClassPathResource("multi_process_evaluation/multi-process-decision.bpmn"), ResourceType.BPMN2);
        if (kbuilder.hasErrors()) {
            for (KnowledgeBuilderError error : kbuilder.getErrors()) {
                System.out.println(">>> Error:" + error.getMessage());

            }
            fail(">>> Knowledge couldn't be parsed! ");
        }

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        final StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        // Uncomment to see all the logs
        KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);

        try {

            ksession.addEventListener(new DefaultAgendaEventListener() {

                @Override
                public void activationCreated(ActivationCreatedEvent event) {
                    System.out.println(">>> Firing All the Rules on Activation Created " + event);
                    ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
                }
            });
            ksession.addEventListener(new DefaultProcessEventListener() {

                @Override
                public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
                    System.out.println(">>> Firing All the Rules before node triggered! " + event);
                    ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
                }
                @Override
                public void beforeNodeLeft(ProcessNodeLeftEvent event) {
                    System.out.println(">>> Firing All the Rules before node triggered! " + event);
                    ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
                }
            });


            Map<String, Object> params = new HashMap<String, Object>();


            ksession.insert(new Resources(50));


            for (int i = 1; i < 16; i++) {

                ProcessInstance processInstance = ksession.createProcessInstance("com.salaboy.process.SimpleDecision", params);
                ksession.insert(processInstance);

                assertEquals(processInstance.getState(), ProcessInstance.STATE_PENDING);
                System.out.println(" ----------------- ##### -----------------");
                System.out.println(" ----- Starting Process Number : " + i + " ----");
                System.out.println(" ----------------- ##### -----------------");
                ksession.startProcessInstance(processInstance.getId());

                assertEquals(processInstance.getState(), ProcessInstance.STATE_COMPLETED);
                System.out.println(" ----------------- ##### -----------------");
                System.out.println(" ----- Process Number : " + i + " Completed ----");
                System.out.println(" ----------------- ##### -----------------");
            }
        } catch (WorkflowRuntimeException e) {
            assertEquals(true, e.getCause().getMessage().contains("No More Resources Available = "));
        }
    }

    @Test
    public void testProcessCreationDelegation() {

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(new ClassPathResource("multi_process_evaluation/resources.drl"), ResourceType.DRL);
        kbuilder.add(new ClassPathResource("multi_process_evaluation/simple-process-trigger.drl"), ResourceType.DRL);
        kbuilder.add(new ClassPathResource("multi_process_evaluation/multi-process-decision-customer.bpmn"), ResourceType.BPMN2);
        if (kbuilder.hasErrors()) {
            for (KnowledgeBuilderError error : kbuilder.getErrors()) {
                System.out.println(">>> Error:" + error.getMessage());

            }
            fail(">>> Knowledge couldn't be parsed! ");
        }

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        final StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        // Uncomment to see all the logs
        //KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);

        ksession.addEventListener(new DefaultAgendaEventListener() {

            @Override
            public void activationCreated(ActivationCreatedEvent event) {
                
                
                ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            }
        });

        ksession.addEventListener(new DefaultWorkingMemoryEventListener() {

           @Override
            public void objectInserted(ObjectInsertedEvent event) {
                
                ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            }

            @Override
            public void objectUpdated(ObjectUpdatedEvent event) {
                
                ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            }
        });

        ksession.addEventListener(new DefaultProcessEventListener() {

            @Override
            public void afterNodeLeft(ProcessNodeLeftEvent event) {
                //System.out.println(">>> Firing All the Rules on afterNodeLeft! " + event);
                ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            }

          
        });
        QueryResults queryResults = null;
        try {
            ksession.insert(new Resources(10));

            ksession.insert(new Customer("salaboy", Customer.CustomerType.GOLD));

            queryResults = ksession.getQueryResults("getResources", (Object[]) null);

            assertEquals(7, ((Resources) queryResults.iterator().next().get("$r")).getAvailable());

            ksession.insert(new Customer("platinum-customer", Customer.CustomerType.PLATINUM));

            queryResults = ksession.getQueryResults("getResources", (Object[]) null);

            assertEquals(2, ((Resources) queryResults.iterator().next().get("$r")).getAvailable());

            ksession.insert(new Customer("starter", Customer.CustomerType.STARTER));

        } catch (ConsequenceException e) {
            assertEquals(true, e.getCause().getMessage().contains("No More Resources Available = "));
            queryResults = ksession.getQueryResults("getResources", (Object[]) null);
            // In this case, because the rules are starting the process, the Global Rule that checks
            // the Resources Object will kick in just after the process ends.
            // This is usually not a problem due to we will usually have long-running processes
            // which will include a lot of wait states where the queued activations can kick in.
            assertEquals(-1, ((Resources) queryResults.iterator().next().get("$r")).getAvailable());
        }

    }
}
