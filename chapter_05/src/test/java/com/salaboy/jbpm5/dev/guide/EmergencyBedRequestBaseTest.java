/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.salaboy.jbpm5.dev.guide;


import java.util.Set;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.WorkingMemory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.event.RuleFlowGroupActivatedEvent;
import org.drools.event.rule.DefaultAgendaEventListener;
import org.drools.io.Resource;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author esteban.aliverti
 */
public abstract class EmergencyBedRequestBaseTest {

    protected StatefulKnowledgeSession session;

    public EmergencyBedRequestBaseTest() {
    }

    @Before
    public void setUp() throws Exception {
        initializeSession();
    }

    @After
    public void tearDown() {
    }

    private void initializeSession() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        for (Resource resource : this.getResources()) {
            kbuilder.add(resource, ResourceType.BPMN2);
        }
        
        if (kbuilder.hasErrors()) {
            KnowledgeBuilderErrors errors = kbuilder.getErrors();

            for (KnowledgeBuilderError error : errors) {
                System.out.println(">>> Error:" + error.getMessage());

            }
            throw new IllegalStateException(">>> Knowledge couldn't be parsed! ");
        }

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        session = kbase.newStatefulKnowledgeSession();
        KnowledgeRuntimeLoggerFactory.newConsoleLogger(session);

        session.addEventListener(new DefaultAgendaEventListener(){

            public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event, WorkingMemory workingMemory) {
                workingMemory.fireAllRules();
            }

        });


    }
    
    protected abstract Set<Resource> getResources();
}
