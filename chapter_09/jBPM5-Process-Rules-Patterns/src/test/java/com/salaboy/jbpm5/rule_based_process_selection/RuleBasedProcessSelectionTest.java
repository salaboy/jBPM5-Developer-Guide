package com.salaboy.jbpm5.rule_based_process_selection;

import com.salaboy.model.Customer;
import com.salaboy.model.Resources;
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
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;
import org.drools.runtime.rule.QueryResults;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;

import org.junit.Test;

/*
 * For a more detailed description about these example look at: 
 * https://github.com/Salaboy/jBPM5-Developer-Guide/blob/master/chapter_09/README
 */

public class RuleBasedProcessSelectionTest {

    @Test
    public void processSelectionForPlatinumCustomersWithResources() {

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(new ClassPathResource("rule_based_process_selection/smart-process-selection.drl"), ResourceType.DRL);
        kbuilder.add(new ClassPathResource("rule_based_process_selection/platinum-customer-process.bpmn"), ResourceType.BPMN2);
        kbuilder.add(new ClassPathResource("rule_based_process_selection/regular-customer-process.bpmn"), ResourceType.BPMN2);
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
        ksession.addEventListener(new DefaultAgendaEventListener() {

            @Override
            public void activationCreated(ActivationCreatedEvent event) {

                ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            }
        });
        ksession.addEventListener(new DefaultProcessEventListener() {

            @Override
            public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {

                ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            }

            @Override
            public void beforeNodeLeft(ProcessNodeLeftEvent event) {

                ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            }
        });

        ksession.insert(new Resources(10));
        Customer platinumCustomer = new Customer("Customer One", Customer.CustomerType.PLATINUM);
        ksession.insert(platinumCustomer);
        

        QueryResults queryResults = ksession.getQueryResults("getResources", (Object[]) null);

        assertEquals(5, ((Resources) queryResults.iterator().next().get("$r")).getAvailable());
        
        queryResults = ksession.getQueryResults("getProcessByCustomer", platinumCustomer);
        assertEquals(1, queryResults.size());
        
        assertEquals("Platinum Customer Process", ((WorkflowProcessInstanceImpl)queryResults.iterator().next().get("$w")).getProcessName());

    }

    @Test
    public void processSelectionForPlatinumCustomersNotEnoughResources() {

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(new ClassPathResource("rule_based_process_selection/smart-process-selection.drl"), ResourceType.DRL);
        kbuilder.add(new ClassPathResource("rule_based_process_selection/platinum-customer-process.bpmn"), ResourceType.BPMN2);
        kbuilder.add(new ClassPathResource("rule_based_process_selection/regular-customer-process.bpmn"), ResourceType.BPMN2);
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
        ksession.addEventListener(new DefaultAgendaEventListener() {

            @Override
            public void activationCreated(ActivationCreatedEvent event) {

                ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            }
        });
        ksession.addEventListener(new DefaultProcessEventListener() {

            @Override
            public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {

                ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            }

            @Override
            public void beforeNodeLeft(ProcessNodeLeftEvent event) {

                ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            }
        });

        ksession.insert(new Resources(4));
        Customer platinumCustomer = new Customer("Customer One", Customer.CustomerType.PLATINUM);
        ksession.insert(platinumCustomer);

        QueryResults queryResults = ksession.getQueryResults("getResources", (Object[]) null);

        assertEquals(1, ((Resources) queryResults.iterator().next().get("$r")).getAvailable());

        queryResults = ksession.getQueryResults("getProcessByCustomer", platinumCustomer);
        assertEquals(1, queryResults.size());
        
        assertEquals("Regular Customer Process", ((WorkflowProcessInstanceImpl)queryResults.iterator().next().get("$w")).getProcessName());
    }
    @Test
    public void processSelectionNotEnoughResources() {

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(new ClassPathResource("rule_based_process_selection/smart-process-selection.drl"), ResourceType.DRL);
        kbuilder.add(new ClassPathResource("rule_based_process_selection/platinum-customer-process.bpmn"), ResourceType.BPMN2);
        kbuilder.add(new ClassPathResource("rule_based_process_selection/regular-customer-process.bpmn"), ResourceType.BPMN2);
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
        ksession.addEventListener(new DefaultAgendaEventListener() {

            @Override
            public void activationCreated(ActivationCreatedEvent event) {

                ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            }
        });
        ksession.addEventListener(new DefaultProcessEventListener() {

            @Override
            public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {

                ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            }

            @Override
            public void beforeNodeLeft(ProcessNodeLeftEvent event) {

                ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            }
        });

        ksession.insert(new Resources(2));
        Customer platinumCustomer = new Customer("Customer One", Customer.CustomerType.PLATINUM);
        ksession.insert(platinumCustomer);

        QueryResults queryResults = ksession.getQueryResults("getResources", (Object[]) null);
        // No Process Was Started
        
        
        assertEquals(2, ((Resources) queryResults.iterator().next().get("$r")).getAvailable());

        queryResults = ksession.getQueryResults("getProcessByCustomer", platinumCustomer);
        assertEquals(0, queryResults.size());

    }
    
     @Test
    public void processSelectionNotEnoughResourcesPlusResourceInjection() {

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(new ClassPathResource("rule_based_process_selection/smart-process-selection.drl"), ResourceType.DRL);
        kbuilder.add(new ClassPathResource("rule_based_process_selection/platinum-customer-process.bpmn"), ResourceType.BPMN2);
        kbuilder.add(new ClassPathResource("rule_based_process_selection/regular-customer-process.bpmn"), ResourceType.BPMN2);
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
        ksession.addEventListener(new DefaultAgendaEventListener() {

            @Override
            public void activationCreated(ActivationCreatedEvent event) {

                ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            }
        });
        ksession.addEventListener(new DefaultProcessEventListener() {

            @Override
            public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {

                ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            }

            @Override
            public void beforeNodeLeft(ProcessNodeLeftEvent event) {

                ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            }
        });
        Resources resources = new Resources(2);
        FactHandle resourcesHandle = ksession.insert(resources);
        Customer platinumCustomer = new Customer("Customer One", Customer.CustomerType.PLATINUM);
        ksession.insert(platinumCustomer);

        QueryResults queryResults = ksession.getQueryResults("getResources", (Object[]) null);
        // No Process Was Started
        
        
        assertEquals(2, ((Resources) queryResults.iterator().next().get("$r")).getAvailable());

        queryResults = ksession.getQueryResults("getProcessByCustomer", platinumCustomer);
        assertEquals(0, queryResults.size());
        
        resources.setAvailable(6);
        
        ksession.update(resourcesHandle, resources);
        
        queryResults = ksession.getQueryResults("getResources", (Object[]) null);
        assertEquals(1, ((Resources) queryResults.iterator().next().get("$r")).getAvailable());

        queryResults = ksession.getQueryResults("getProcessByCustomer", platinumCustomer);
        assertEquals(1, queryResults.size());
        assertEquals("Platinum Customer Process", ((WorkflowProcessInstanceImpl)queryResults.iterator().next().get("$w")).getProcessName());
    }
}
