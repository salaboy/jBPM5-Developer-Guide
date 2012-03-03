/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5;

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
import org.drools.io.impl.ClassPathResource;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.rule.QueryResults;
import org.drools.runtime.rule.QueryResultsRow;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author salaboy
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
    public void processVariablesAutoMappingTest() throws InterruptedException{
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(new ClassPathResource("mapping.drl"), ResourceType.DRL);  
        kbuilder.add(new ClassPathResource("scoring_processVariables.drl"), ResourceType.DRL);
        kbuilder.add(new ClassPathResource("process-automapping-decision.bpmn"), ResourceType.BPMN2);

        if (kbuilder.hasErrors()) {
            for (KnowledgeBuilderError error : kbuilder.getErrors()) {
                System.out.println(">>> Error:" + error.getMessage());

            }
            fail(">>> Knowledge couldn't be parsed! ");
        }

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        final StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        new Thread(new Runnable() {

            public void run() {
                ksession.fireUntilHalt();
            }
        } ).start();
        Person person = new Person("Salaboy", 28);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", person);

        ProcessInstance processInstance = ksession.createProcessInstance("com.salaboy.process.SimpleDecision", params);
        System.out.println("Variables: "+((WorkflowProcessInstanceImpl)processInstance).getVariables());
        assertEquals(processInstance.getState(), ProcessInstance.STATE_PENDING);
        ksession.insert(processInstance);
        
        ksession.startProcessInstance(processInstance.getId());
        
        assertEquals(processInstance.getState(), ProcessInstance.STATE_COMPLETED);
        QueryResults queryResults = ksession.getQueryResults("allProcessVariables", new Object[]{});
        Iterator<QueryResultsRow> iterator = queryResults.iterator();
        while(iterator.hasNext()){
            QueryResultsRow next = iterator.next();
            assertEquals(person, ((ProcessVariable)next.get("$pv")).getValue());
        }
    }
}
