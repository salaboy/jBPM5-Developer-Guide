package com.salaboy.jbpm5.dev.guide.jbpm5_first_project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.impl.ClassPathResource;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.junit.Test;

public class MyFirstjBPM5ProcessTest {

	@Test
	public void test() {
		
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(new ClassPathResource("firstDummyProcess.bpmn"), ResourceType.BPMN2);
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
        // We have our Session, now let's play with our Dummy Process
        
        ProcessInstance processInstance = ksession.createProcessInstance("DummyProcess", null);
        
        assertEquals(processInstance.getState(), ProcessInstance.STATE_PENDING );
        
        ksession.startProcessInstance(processInstance.getId());
        
        assertEquals(processInstance.getState(), ProcessInstance.STATE_COMPLETED );
	}

}
