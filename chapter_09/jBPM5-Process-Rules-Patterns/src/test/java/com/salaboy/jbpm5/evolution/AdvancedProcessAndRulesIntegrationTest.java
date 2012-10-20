/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.evolution;

import com.salaboy.model.Person;
import com.salaboy.model.RatesToday;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.WorkingMemory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.event.process.DefaultProcessEventListener;
import org.drools.event.process.ProcessCompletedEvent;
import org.drools.event.process.ProcessStartedEvent;
import org.drools.event.rule.ActivationCreatedEvent;
import org.drools.event.rule.DefaultAgendaEventListener;
import org.drools.event.rule.RuleFlowGroupActivatedEvent;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.io.impl.ClassPathResource;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.process.instance.WorkItemHandler;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;
import org.drools.runtime.rule.FactHandle;
import org.drools.runtime.rule.QueryResults;
import org.drools.runtime.rule.QueryResultsRow;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.junit.*;
import static org.junit.Assert.*;

/*
 * For a more detailed description about these example look at: 
 * https://github.com/Salaboy/jBPM5-Developer-Guide/blob/master/chapter_09/README
 */
public class AdvancedProcessAndRulesIntegrationTest {

    public AdvancedProcessAndRulesIntegrationTest() {
    }

    @Test
    public void processVariablesAutoMappingPlusCastNoSafeCheckTest() throws InterruptedException {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(new ClassPathResource("evolution/mapping.drl"), ResourceType.DRL);
        kbuilder.add(new ClassPathResource("evolution/scoring_processVariables_wider.drl"), ResourceType.DRL);
        kbuilder.add(new ClassPathResource("evolution/process-variables-decision.bpmn"), ResourceType.BPMN2);

        if (kbuilder.hasErrors()) {
            for (KnowledgeBuilderError error : kbuilder.getErrors()) {
                System.out.println(">>> Error:" + error.getMessage());

            }
            fail(">>> Knowledge couldn't be parsed! ");
        }

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        final StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);

        Person person = new Person("Salaboy", 28);
        RatesToday ratesToday = new RatesToday(1, 100);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", person);
        params.put("ratesToday", ratesToday);

        ProcessInstance processInstance = ksession.createProcessInstance("com.salaboy.process.SimpleDecision", params);
        System.out.println("Variables: " + ((WorkflowProcessInstanceImpl) processInstance).getVariables());
        assertEquals(processInstance.getState(), ProcessInstance.STATE_PENDING);
        final FactHandle processHandle = ksession.insert(processInstance);

        ksession.addEventListener(
                new DefaultAgendaEventListener() {
                    @Override
                    public void activationCreated(ActivationCreatedEvent event) {
                        System.out.println("Firing All the Rules! " + event);
                        ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
                    }

                    @Override
                    public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
                        System.out.println("Firing All the Rules! " + event);
                        ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
                    }
                });

        ksession.addEventListener(new DefaultProcessEventListener() {
            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            }

            @Override
            public void afterProcessStarted(ProcessStartedEvent event) {
                ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            }

            @Override
            public void afterProcessCompleted(ProcessCompletedEvent event) {
                ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).retract(processHandle);
            }
        });


        ksession.startProcessInstance(processInstance.getId());

//        // If you want to query the process variables while the process Instance is running you can do: 
//        //  But remember that the activities inside the process are all sync. 
//        QueryResults queryResults = ksession.getQueryResults("allProcessVariables", new Object[]{});
//        Iterator<QueryResultsRow> iterator = queryResults.iterator();
//
//        QueryResultsRow ratesRow = iterator.next();
//        assertEquals(ratesToday, ((ProcessVariable) ratesRow.get("$pv")).getValue());
//
//        QueryResultsRow personRow = iterator.next();
//        assertEquals(person, ((ProcessVariable) personRow.get("$pv")).getValue());
        
        assertEquals(processInstance.getState(), ProcessInstance.STATE_COMPLETED);
        QueryResults queryResults = ksession.getQueryResults("allProcessVariables", new Object[]{});
        // The Process Variables are automatically retracted when the Process Instance is Completed
        assertEquals(0, queryResults.size());


    }

    @Test
    public void processEventsTest() throws InterruptedException {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(new ClassPathResource("evolution/mapping.drl"), ResourceType.DRL);
        kbuilder.add(new ClassPathResource("evolution/scoring_processVariables_wider.drl"), ResourceType.DRL);
        kbuilder.add(new ClassPathResource("evolution/process-events-decision.bpmn"), ResourceType.BPMN2);

        if (kbuilder.hasErrors()) {
            for (KnowledgeBuilderError error : kbuilder.getErrors()) {
                System.out.println(">>> Error:" + error.getMessage());

            }
            fail(">>> Knowledge couldn't be parsed! ");
        }

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        final StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new WorkItemHandler() {
            public void executeWorkItem(WorkItem wi, WorkItemManager wim) {
                System.out.println(" >>> Completing Task! -> " + wi.getName() + " - id: " + wi.getId());
                wim.completeWorkItem(wi.getId(), null);
            }

            public void abortWorkItem(WorkItem wi, WorkItemManager wim) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });

        KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);


        ksession.addEventListener(
                new DefaultAgendaEventListener() {
                    @Override
                    public void activationCreated(ActivationCreatedEvent event) {
                        ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
                    }

                    @Override
                    public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
                        ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
                    }
                });


        Person person = new Person("Salaboy", 28);
        RatesToday ratesToday = new RatesToday(1, 100);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", person);
        params.put("ratesToday", ratesToday);

        ProcessInstance processInstance = ksession.createProcessInstance("com.salaboy.process.SimpleDecision", params);
        System.out.println("Variables: " + ((WorkflowProcessInstanceImpl) processInstance).getVariables());
        assertEquals(processInstance.getState(), ProcessInstance.STATE_PENDING);
        final FactHandle processHandle = ksession.insert(processInstance);

        ((StatefulKnowledgeSessionImpl) ksession).addEventListener(new DefaultProcessEventListener() {
            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            }

            @Override
            public void afterProcessStarted(ProcessStartedEvent event) {
                ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            }

            @Override
            public void afterProcessCompleted(ProcessCompletedEvent event) {
                ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).retract(processHandle);
            }
        });
        ksession.startProcessInstance(processInstance.getId());

        Thread.sleep(1000);

//        // If you want to query the process variables while the process Instance is running you can do: 
//        QueryResults queryResults = ksession.getQueryResults("allProcessVariables", new Object[]{});
//        Iterator<QueryResultsRow> iterator = queryResults.iterator();
//
//        QueryResultsRow ratesRow = iterator.next();
//        assertEquals(ratesToday, ((ProcessVariable) ratesRow.get("$pv")).getValue());
//
//        QueryResultsRow personRow = iterator.next();
//        assertEquals(person, ((ProcessVariable) personRow.get("$pv")).getValue());

        assertEquals(processInstance.getState(), ProcessInstance.STATE_COMPLETED);
        QueryResults queryResults = ksession.getQueryResults("allProcessVariables", new Object[]{});
        // The Process Variables are automatically retracted when the Process Instance is Completed
        assertEquals(0, queryResults.size());


    }
}
