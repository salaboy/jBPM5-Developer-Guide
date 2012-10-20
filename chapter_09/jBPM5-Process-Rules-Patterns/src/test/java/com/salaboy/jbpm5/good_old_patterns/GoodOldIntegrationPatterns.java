/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.good_old_patterns;

import com.salaboy.jbpm5.DefineCarPriceWorkItemHandler;
import com.salaboy.jbpm5.RankCarWorkItemHandler;
import com.salaboy.model.Car;
import com.salaboy.model.MarketMetric;
import com.salaboy.model.Person;
import java.util.Date;
import java.util.HashMap;
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
import org.junit.*;
import static org.junit.Assert.*;

/*
 * For a more detailed description about these example look at: 
 * https://github.com/Salaboy/jBPM5-Developer-Guide/blob/master/chapter_09/README
 */
public class GoodOldIntegrationPatterns {
    
    public GoodOldIntegrationPatterns() {
    }

    @Test
    public void javaBasedDecisionTest() {
    

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(new ClassPathResource("good_old_patterns/process-java-decision.bpmn"), ResourceType.BPMN2);
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

        assertEquals(processInstance.getState(), ProcessInstance.STATE_PENDING);

        ksession.startProcessInstance(processInstance.getId());

        assertEquals(processInstance.getState(), ProcessInstance.STATE_COMPLETED);
    

    }
    
    @Test
    public void statelessDecorationTest() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(new ClassPathResource("good_old_patterns/process-stateless-rule-evaluation.bpmn"), ResourceType.BPMN2);
        if (kbuilder.hasErrors()) {
            for (KnowledgeBuilderError error : kbuilder.getErrors()) {
                System.out.println(">>> Error:" + error.getMessage());

            }
            fail(">>> Knowledge couldn't be parsed! ");
        }

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        
        ksession.getWorkItemManager().registerWorkItemHandler("Rank Car", new RankCarWorkItemHandler());
        
        ksession.getWorkItemManager().registerWorkItemHandler("Define Car Price", new DefineCarPriceWorkItemHandler());
        
        Car car = new Car("AUDI 78", new Date(), 5, "manual", "gas", 285, 15000);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("car", car);

        ProcessInstance processInstance = ksession.createProcessInstance("com.salaboy.process.stateless-rules-decoration", params);

        assertEquals(processInstance.getState(), ProcessInstance.STATE_PENDING);

        ksession.startProcessInstance(processInstance.getId());
        
        System.out.println("Car : "+car);
        
    }
    
    
 
    @Test
    public void statelessDecisionTest() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(new ClassPathResource("good_old_patterns/process-stateless-rule-evaluation-java-gateway.bpmn"), ResourceType.BPMN2);
        if (kbuilder.hasErrors()) {
            for (KnowledgeBuilderError error : kbuilder.getErrors()) {
                System.out.println(">>> Error:" + error.getMessage());

            }
            fail(">>> Knowledge couldn't be parsed! ");
        }

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        
        ksession.getWorkItemManager().registerWorkItemHandler("Rank Car", new RankCarWorkItemHandler());
        
        ksession.getWorkItemManager().registerWorkItemHandler("Define Car Price", new DefineCarPriceWorkItemHandler());
        
        Car car = new Car("AUDI 78", new Date(), 5, "manual", "gas", 285, 25000);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("car", car);

        ProcessInstance processInstance = ksession.createProcessInstance("com.salaboy.process.stateless-rules-decoration", params);

        assertEquals(processInstance.getState(), ProcessInstance.STATE_PENDING);

        ksession.startProcessInstance(processInstance.getId());
        
        System.out.println("Car : "+car);
        
       
        
    } 
    
    
    @Test 
    public void statelessGatewayCallTest() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(new ClassPathResource("good_old_patterns/process-stateless-rule-evaluation-rules-gateway.bpmn"), ResourceType.BPMN2);
        if (kbuilder.hasErrors()) {
            for (KnowledgeBuilderError error : kbuilder.getErrors()) {
                System.out.println(">>> Error:" + error.getMessage());

            }
            fail(">>> Knowledge couldn't be parsed! ");
        }

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        
        ksession.getWorkItemManager().registerWorkItemHandler("Rank Car", new RankCarWorkItemHandler());
        
        ksession.getWorkItemManager().registerWorkItemHandler("Define Car Price", new DefineCarPriceWorkItemHandler());
        
        Car car = new Car("AUDI 78", new Date(), 5, "manual", "gas", 285, 25000);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("car", car);

        ProcessInstance processInstance = ksession.createProcessInstance("com.salaboy.process.stateless-rules-decoration", params);

        assertEquals(processInstance.getState(), ProcessInstance.STATE_PENDING);

        ksession.startProcessInstance(processInstance.getId());
        
        assertEquals(new Double(17500), new Double(car.getCurrentPrice()));
        assertEquals(6, car.getRanking());
        
        
        
    } 
    @Test
    public void theRuleEngineWay(){
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(new ClassPathResource("good_old_patterns/car-evaluations.drl"), ResourceType.DRL);
        
        
        if (kbuilder.hasErrors()) {
            for (KnowledgeBuilderError error : kbuilder.getErrors()) {
                System.out.println(">>> Error:" + error.getMessage());

            }
            fail(">>> Knowledge couldn't be parsed! ");
        }

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        //KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
        
        Car car = new Car("AUDI 78", new Date(), 5, "manual", "gas", 285, 25000);
        MarketMetric metric = new MarketMetric(0.8);
        ksession.insert(metric);
        ksession.insert(car);
        
        ksession.fireAllRules();
        
        assertEquals(new Double(17500), new Double(car.getCurrentPrice()));
        assertEquals(6, car.getRanking());
        assertTrue(metric.getResult());
        
    } 
    
}
