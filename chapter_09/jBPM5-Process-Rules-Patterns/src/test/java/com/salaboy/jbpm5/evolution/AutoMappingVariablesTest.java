/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.evolution;

import com.salaboy.jbpm5.ProcessVariable;
import com.salaboy.model.Person;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.event.process.DefaultProcessEventListener;
import org.drools.event.process.ProcessVariableChangedEvent;
import org.drools.io.impl.ClassPathResource;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
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
public class AutoMappingVariablesTest {

    public AutoMappingVariablesTest() {
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
    public void processVariablesAutoMappingTest() throws InterruptedException {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(new ClassPathResource("evolution/mapping.drl"), ResourceType.DRL);
        kbuilder.add(new ClassPathResource("evolution/scoring_processVariables.drl"), ResourceType.DRL);
        kbuilder.add(new ClassPathResource("evolution/process-automapping-decision.bpmn"), ResourceType.BPMN2);

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

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", person);

        ProcessInstance processInstance = ksession.createProcessInstance("com.salaboy.process.SimpleDecision", params);
        System.out.println("Variables: " + ((WorkflowProcessInstanceImpl) processInstance).getVariables());
        assertEquals(processInstance.getState(), ProcessInstance.STATE_PENDING);
        FactHandle processtHandle = ksession.insert(processInstance);

        ksession.fireAllRules();
        
        ksession.startProcessInstance(processInstance.getId());
        
        
        
        assertEquals(processInstance.getState(), ProcessInstance.STATE_COMPLETED);
        QueryResults queryResults = ksession.getQueryResults("allProcessVariables", new Object[]{});
        Iterator<QueryResultsRow> iterator = queryResults.iterator();
        while (iterator.hasNext()) {
            QueryResultsRow next = iterator.next();
            assertEquals(person, ((ProcessVariable) next.get("$pv")).getValue());
        }

        ksession.retract(processtHandle);


    }

    @Test
    @Ignore // Add data mappings and finish example
    public void processVariablesAutoMappingWithListenerTest() throws InterruptedException {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(new ClassPathResource("evolution/mapping.drl"), ResourceType.DRL);
        kbuilder.add(new ClassPathResource("evolution/scoring_processVariables.drl"), ResourceType.DRL);
        kbuilder.add(new ClassPathResource("evolution/process-automapping-change-decision.bpmn"), ResourceType.BPMN2);

        if (kbuilder.hasErrors()) {
            for (KnowledgeBuilderError error : kbuilder.getErrors()) {
                System.out.println(">>> Error:" + error.getMessage());

            }
            fail(">>> Knowledge couldn't be parsed! ");
        }

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        final StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        ksession.addEventListener(new DefaultProcessEventListener() {

            @Override
            public void afterVariableChanged(ProcessVariableChangedEvent event) {
                System.out.println(" ### Variable has been changed  -> " + event.getVariableId());
                System.out.println(" ###\t  old -> " + event.getOldValue());
                System.out.println(" ###\t  new -> " + event.getNewValue());
            }

            @Override
            public void beforeVariableChanged(ProcessVariableChangedEvent event) {
                System.out.println(" ### Variable is going to change  -> " + event.getVariableId());
                System.out.println(" ###\t  old -> " + event.getOldValue());
                System.out.println(" ###\t  new -> " + event.getNewValue());
            }
        });

        KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
        new Thread(new Runnable() {

            public void run() {
                ksession.fireUntilHalt();
            }
        }).start();
        Person person = new Person("Salaboy", 28);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", person);

        ProcessInstance processInstance = ksession.createProcessInstance("com.salaboy.process.SimpleDecision", params);
        System.out.println("Variables: " + ((WorkflowProcessInstanceImpl) processInstance).getVariables());
        assertEquals(processInstance.getState(), ProcessInstance.STATE_PENDING);
        FactHandle processtHandle = ksession.insert(processInstance);

        ksession.startProcessInstance(processInstance.getId());

        assertEquals(processInstance.getState(), ProcessInstance.STATE_COMPLETED);
        QueryResults queryResults = ksession.getQueryResults("allProcessVariables", new Object[]{});
        Iterator<QueryResultsRow> iterator = queryResults.iterator();
        while (iterator.hasNext()) {
            QueryResultsRow next = iterator.next();
            assertEquals(person, ((ProcessVariable) next.get("$pv")).getValue());
        }

        ksession.retract(processtHandle);


    }
}
