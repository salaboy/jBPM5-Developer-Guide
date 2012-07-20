/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5;

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

/**
 *
 * @author salaboy
 */
public class AdvancedProcessAndRulesIntegrationTest {

    public AdvancedProcessAndRulesIntegrationTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void processVariableCreationTest() {
        Person person = new Person("salaboy", 28);
        ProcessVariable<Person> var = new ProcessVariable<Person>(1, "", person);
        Person person2 = var.getValue();

    }

    @Test
    public void processVariablesAutoMappingPlusCastNoSafeCheckTest() throws InterruptedException {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(new ClassPathResource("mapping.drl"), ResourceType.DRL);
        kbuilder.add(new ClassPathResource("scoring_processVariables_wider.drl"), ResourceType.DRL);
        kbuilder.add(new ClassPathResource("process-variables-decision.bpmn"), ResourceType.BPMN2);

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
        });


        Person person = new Person("Salaboy", 28);
        RatesToday ratesToday = new RatesToday(1, 100);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", person);
        params.put("ratesToday", ratesToday);

        ProcessInstance processInstance = ksession.createProcessInstance("com.salaboy.process.SimpleDecision", params);
        System.out.println("Variables: " + ((WorkflowProcessInstanceImpl) processInstance).getVariables());
        assertEquals(processInstance.getState(), ProcessInstance.STATE_PENDING);
        FactHandle processtHandle = ksession.insert(processInstance);

        ksession.startProcessInstance(processInstance.getId());

        assertEquals(processInstance.getState(), ProcessInstance.STATE_COMPLETED);
        QueryResults queryResults = ksession.getQueryResults("allProcessVariables", new Object[]{});
        Iterator<QueryResultsRow> iterator = queryResults.iterator();

        QueryResultsRow ratesRow = iterator.next();
        assertEquals(ratesToday, ((ProcessVariable) ratesRow.get("$pv")).getValue());

        QueryResultsRow personRow = iterator.next();
        assertEquals(person, ((ProcessVariable) personRow.get("$pv")).getValue());

        ksession.retract(processtHandle);


    }

    @Test
    public void processEventsTest() throws InterruptedException {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(new ClassPathResource("mapping.drl"), ResourceType.DRL);
        kbuilder.add(new ClassPathResource("scoring_processVariables_wider.drl"), ResourceType.DRL);
        kbuilder.add(new ClassPathResource("process-events-decision.bpmn"), ResourceType.BPMN2);

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
        ((StatefulKnowledgeSessionImpl) ksession).addEventListener(new DefaultProcessEventListener() {
            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            }
            @Override
            public void afterProcessStarted(ProcessStartedEvent event) {
                ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
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
        FactHandle processtHandle = ksession.insert(processInstance);

        ksession.startProcessInstance(processInstance.getId());

        Thread.sleep(1000);

        assertEquals(processInstance.getState(), ProcessInstance.STATE_COMPLETED);
        QueryResults queryResults = ksession.getQueryResults("allProcessVariables", new Object[]{});
        Iterator<QueryResultsRow> iterator = queryResults.iterator();

        QueryResultsRow ratesRow = iterator.next();
        assertEquals(ratesToday, ((ProcessVariable) ratesRow.get("$pv")).getValue());

        QueryResultsRow personRow = iterator.next();
        assertEquals(person, ((ProcessVariable) personRow.get("$pv")).getValue());

        ksession.retract(processtHandle);


    }
}
