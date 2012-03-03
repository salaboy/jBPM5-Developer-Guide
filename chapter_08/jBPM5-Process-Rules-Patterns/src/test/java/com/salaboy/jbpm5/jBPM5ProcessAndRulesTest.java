package com.salaboy.jbpm5;

import com.salaboy.model.Person;
import com.salaboy.model.RatesToday;
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
import org.drools.io.impl.ClassPathResource;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.junit.Test;

public class jBPM5ProcessAndRulesTest {

    @Test
    public void testSimpleDecision() {

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(new ClassPathResource("process-decision.bpmn"), ResourceType.BPMN2);
        if (kbuilder.hasErrors()) {
            for (KnowledgeBuilderError error : kbuilder.getErrors()) {
                System.out.println(">>> Error:" + error.getMessage());

            }
            fail(">>> Knowledge couldn't be parsed! ");
        }

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        // Uncomment to see all the logs
        // KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);

        Person person = new Person("Salaboy", 28);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", person);

        ProcessInstance processInstance = ksession.createProcessInstance("com.salaboy.process.SimpleDecision", params);

        ksession.insert(processInstance);
        ksession.insert(person);

        assertEquals(processInstance.getState(), ProcessInstance.STATE_PENDING);

        ksession.startProcessInstance(processInstance.getId());

        assertEquals(processInstance.getState(), ProcessInstance.STATE_COMPLETED);
    }

    @Test
    public void testSimpleDecisionWithRules() {

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(new ClassPathResource("scoring.drl"), ResourceType.DRL);
        kbuilder.add(new ClassPathResource("process-decision.bpmn"), ResourceType.BPMN2);

        if (kbuilder.hasErrors()) {
            for (KnowledgeBuilderError error : kbuilder.getErrors()) {
                System.out.println(">>> Error:" + error.getMessage());

            }
            fail(">>> Knowledge couldn't be parsed! ");
        }

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        // Uncomment to see all the logs
        // KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);

        Person person = new Person("Salaboy", 28);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", person);

        ProcessInstance processInstance = ksession.createProcessInstance("com.salaboy.process.SimpleDecision", params);

        ksession.insert(processInstance);
        ksession.insert(person);
        ksession.insert(new RatesToday(3, 5));

        assertEquals(processInstance.getState(), ProcessInstance.STATE_PENDING);

        ksession.startProcessInstance(processInstance.getId());
        ksession.fireAllRules();
        
        
        assertEquals(84, person.getScore());
        assertEquals(processInstance.getState(), ProcessInstance.STATE_COMPLETED);
    }
    
    @Test
    public void testSimpleDecisionWithReactiveRules() throws InterruptedException {

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(new ClassPathResource("scoring.drl"), ResourceType.DRL);
        kbuilder.add(new ClassPathResource("process-decision.bpmn"), ResourceType.BPMN2);

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
        // KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
        new Thread(new Runnable() {

            public void run() {
                ksession.fireUntilHalt();
            }
        } ).start();
        
        Person person = new Person("Salaboy", 28);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", person);

        ProcessInstance processInstance = ksession.createProcessInstance("com.salaboy.process.SimpleDecision", params);

        ksession.insert(processInstance);
        ksession.insert(person);
        ksession.insert(new RatesToday(3, 5));

        assertEquals(processInstance.getState(), ProcessInstance.STATE_PENDING);

        ksession.startProcessInstance(processInstance.getId());
        ksession.fireAllRules();
        
        Thread.sleep(1000); // We need to wait a little because we are in reactive mode
        
        assertEquals(84, person.getScore());
        assertEquals(processInstance.getState(), ProcessInstance.STATE_COMPLETED);
    }
    
}
