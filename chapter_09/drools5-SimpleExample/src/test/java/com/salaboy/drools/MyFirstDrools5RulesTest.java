package com.salaboy.drools;

import com.salaboy.model.Person;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.impl.ClassPathResource;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.Test;
import static org.junit.Assert.*;

public class MyFirstDrools5RulesTest {

    @Test
    public void testSimpleRules() {

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(new ClassPathResource("simpleRules.drl"), ResourceType.DRL);
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
        // We have our Session, now let's play with our Simple Rules
        
        Person person = new Person("Salaboy", 28);
        ksession.insert(person);
        
        int fired = ksession.fireAllRules();
        
        assertEquals(4, fired);
        assertEquals(true, person.isEnabledToDrive());
        assertEquals(true, person.isEnabledToVote());
        assertEquals(true, person.isHappy());
        
        
        Person person2 = new Person("John", 19);
        ksession.insert(person2);
        
        fired = ksession.fireAllRules();
        
        assertEquals(3, fired);
        
        assertEquals(true, person2.isEnabledToDrive());
        assertEquals(false, person2.isEnabledToVote());
        assertEquals(true, person2.isHappy());
        
        
        Person person3 = new Person("Marie", 17);
        ksession.insert(person3);
        
        fired = ksession.fireAllRules();
        assertEquals(0, fired);
        
        assertEquals(false, person3.isEnabledToDrive());
        assertEquals(false, person3.isEnabledToVote());
        assertEquals(false, person3.isHappy());
        
        ksession.dispose();
        
    }
}
