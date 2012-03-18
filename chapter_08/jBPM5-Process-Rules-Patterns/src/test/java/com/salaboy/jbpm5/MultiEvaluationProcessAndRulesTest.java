package com.salaboy.jbpm5;

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
import org.drools.io.impl.ClassPathResource;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.junit.Test;

public class MultiEvaluationProcessAndRulesTest {

    @Test
    public void testMultiProcessEvaluation() {

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(new ClassPathResource("resources.drl"), ResourceType.DRL);
        kbuilder.add(new ClassPathResource("multi-process-decision.bpmn"), ResourceType.BPMN2);
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
        try {
            new Thread(new Runnable() {

                public void run() {
                    ksession.fireUntilHalt();
                }
            }).start();

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
        } catch (IllegalStateException e) {
            assertEquals(true, e.getMessage().contains("No More Resources Available = "));
        }
    }
}
