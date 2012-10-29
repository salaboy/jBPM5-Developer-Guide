/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.salaboy.jbpm5.dev.guide;


import java.util.Map;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.event.process.DefaultProcessEventListener;
import org.drools.event.process.ProcessStartedEvent;
import org.drools.event.rule.DefaultAgendaEventListener;
import org.drools.io.Resource;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.After;
import org.junit.Before;

/**
 * Base test class used by the rest of the tests in this project.
 * @author esteban.aliverti
 */
public abstract class EmergencyBedRequestBaseTest {

    /**
     * The internal ksession where the processes will run.
     */
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

    /**
     * Compiles the resources indicated by the concrete implementation of 
     * this class (using {@link #getResources()} abstract method.
     * If there is any compilation error an {@link IllegalStateException} is
     * thrown.
     * After the resources are compiled, a kbase is created and populated with
     * the resulting knowledge package/s.
     * From this kbase, a new StatefulKnowledgeSession is created. The session
     * is configured with 2 listeners:
     * 1.- An Agenda event listener that will fireAllRules() every time an
     * activation happens
     * 2.- A Process event listener that will insert the process instance as
     * a Fact once a process is started.
     */
    private void initializeSession() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        for (Map.Entry<Resource, ResourceType> entry : this.getResources().entrySet()) {
            kbuilder.add(entry.getKey(), entry.getValue());
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

            @Override
            public void afterRuleFlowGroupActivated(org.drools.event.rule.RuleFlowGroupActivatedEvent event) {
                session.fireAllRules();
            }
            
        });
        
        session.addEventListener(new DefaultProcessEventListener(){

            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                session.insert(event.getProcessInstance());
            }
            
        });
    }
    
    
    protected abstract Map<Resource, ResourceType> getResources();
    
}
