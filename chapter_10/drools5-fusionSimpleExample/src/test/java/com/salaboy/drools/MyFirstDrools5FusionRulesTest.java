package com.salaboy.drools;


import com.salaboy.model.KeyA;
import com.salaboy.model.KeyD;
import com.salaboy.model.KeyS;
import java.util.concurrent.TimeUnit;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.conf.EventProcessingOption;
import org.drools.io.impl.ClassPathResource;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.conf.ClockTypeOption;
import org.drools.time.SessionPseudoClock;

import org.junit.Test;
import static org.junit.Assert.*;

public class MyFirstDrools5FusionRulesTest {

    @Test
    public void testSimpleEvents() {

        StatefulKnowledgeSession ksession = createKnowledgeSession();
        // Uncomment to see all the logs
        // KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
        // We have our Session, now let's play with our Events
        SessionPseudoClock clock = ksession.getSessionClock();
        int fired = 0;
        // Initial time 0s -> t0
        ksession.insert(new KeyA());
        fired = ksession.fireAllRules();
        clock.advanceTime(2, TimeUnit.SECONDS);
        assertEquals(0, fired);
        
        // t1 -> 2s 
        ksession.insert(new KeyA());
        fired = ksession.fireAllRules();
        clock.advanceTime(2, TimeUnit.SECONDS);
        assertEquals(0, fired);
        
        // t2 -> 4s 
        ksession.insert(new KeyA());
        fired = ksession.fireAllRules();
        clock.advanceTime(2, TimeUnit.SECONDS);
        assertEquals(0, fired);
        
        // t3 -> 6s 
        ksession.insert(new KeyA());
        fired = ksession.fireAllRules();
        clock.advanceTime(2, TimeUnit.SECONDS);
        assertEquals(1, fired);
        
        // t4 -> 8s
        ksession.insert(new KeyA());
        fired = ksession.fireAllRules();
        assertEquals(1, fired);
        
        ksession.dispose();
        
    }
    @Test
    public void testPatternDetectionEvents() {
        StatefulKnowledgeSession ksession = createKnowledgeSession();
        
        SessionPseudoClock clock = ksession.getSessionClock();
        int fired = 0;
        // Initial time 0s -> t0
        ksession.insert(new KeyA());
        fired = ksession.fireAllRules();
        clock.advanceTime(15, TimeUnit.MILLISECONDS);
        assertEquals(0, fired);
        
        ksession.insert(new KeyS());
        fired = ksession.fireAllRules();
        clock.advanceTime(15, TimeUnit.MILLISECONDS);
        assertEquals(0, fired);
        
        ksession.insert(new KeyD());
        fired = ksession.fireAllRules();
        clock.advanceTime(15, TimeUnit.MILLISECONDS);
        assertEquals(1, fired);
        
        ksession.dispose();
    }
    
    
    private StatefulKnowledgeSession createKnowledgeSession(){
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(new ClassPathResource("simpleEventAnalysis.drl"), ResourceType.DRL);
        if (kbuilder.hasErrors()) {
            for (KnowledgeBuilderError error : kbuilder.getErrors()) {
                System.out.println(">>> Error:" + error.getMessage());

            }
            fail(">>> Knowledge couldn't be parsed! ");
        }

        KnowledgeBaseConfiguration config = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
        config.setOption(EventProcessingOption.STREAM);
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase(config);

        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        KnowledgeSessionConfiguration sessionConfig = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        sessionConfig.setOption( ClockTypeOption.get("pseudo") );
        return kbase.newStatefulKnowledgeSession(sessionConfig, null);
    }
}
