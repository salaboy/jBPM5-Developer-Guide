/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5;

import com.salaboy.model.Person;
import com.salaboy.model.TaskSpeed;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.WorkingMemory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.conf.EventProcessingOption;
import org.drools.event.*;
import org.drools.event.process.DefaultProcessEventListener;
import org.drools.event.process.ProcessCompletedEvent;
import org.drools.event.process.ProcessStartedEvent;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.io.impl.ClassPathResource;
import org.drools.process.instance.WorkItemHandler;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;
import org.drools.runtime.rule.FactHandle;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author salaboy
 */
public class ProcessAndEventMultiThreadIntegrationTest {

    public ProcessAndEventMultiThreadIntegrationTest() {
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
    public void processEventsWithListenerTest() throws InterruptedException {
        // Let's create a kbuilder, kbase and ksession for running the process


        final StatefulKnowledgeSession processKsession = createProcessSession();

        // Let's create a kbuilder, kbase and ksession for analyzing the process events

        final StatefulKnowledgeSession eventsKsession = createEventsSession();
        final TaskSpeed taskSpeed = new TaskSpeed(1000L);
        eventsKsession.setGlobal("taskSpeed", taskSpeed);

        processKsession.getWorkItemManager().registerWorkItemHandler("Human Task", new WorkItemHandler() {

            public void executeWorkItem(WorkItem wi, WorkItemManager wim) {
                try {
                    System.out.println(" >>> Working on Task! it will take: " + taskSpeed.getAmount() / 1000 + " seconds.");
                    Thread.sleep(taskSpeed.getAmount());

                } catch (InterruptedException ex) {
                    Logger.getLogger(ProcessAndEventMultiThreadIntegrationTest.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println(" >>> Completing Task! -> " + wi.getName() + " - id: " + wi.getId());
                wim.completeWorkItem(wi.getId(), null);
            }

            public void abortWorkItem(WorkItem wi, WorkItemManager wim) {
                // do nothing
            }
        });
        ((StatefulKnowledgeSessionImpl) processKsession).addEventListener(new DefaultProcessEventListener() {

            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                System.out.println(" >>> Before Process Started: " + event.getProcessInstance().getProcessName());
                eventsKsession.insert(event);
            }

            @Override
            public void afterProcessCompleted(ProcessCompletedEvent event) {
                System.out.println(" >>> After Process Completed: " + event.getProcessInstance().getProcessName());
                eventsKsession.insert(event);
            }

            @Override
            public void beforeNodeLeft(org.drools.event.process.ProcessNodeLeftEvent event) {
                System.out.println(" >>> Before Node Left: " + event.getNodeInstance().getNodeName());
                eventsKsession.insert(event);
            }
        });
        //KnowledgeRuntimeLoggerFactory.newConsoleLogger(processKsession);





        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                final int id1 = i;
                final int id2 = j;
                new Thread(new Runnable() {

                    public void run() {
                        Person person = new Person("Salaboy-" +id1+"_"+id2, id2);
                        Map<String, Object> params = new HashMap<String, Object>();
                        params.put("person", person);
                        ProcessInstance processInstance = processKsession.createProcessInstance("com.salaboy.process.SimpleProcess", params);

                        assertEquals(processInstance.getState(), ProcessInstance.STATE_PENDING);
                        FactHandle processtHandle = processKsession.insert(processInstance);
                        System.out.println("Starting Process Instance: " + processInstance.getId());
                        processKsession.startProcessInstance(processInstance.getId());

                        assertEquals(processInstance.getState(), ProcessInstance.STATE_COMPLETED);
                        processKsession.retract(processtHandle);
                    }
                }).start();
            }
            Thread.sleep(3000);
        }





    }

    private StatefulKnowledgeSession createProcessSession() {
        KnowledgeBuilder processKbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        processKbuilder.add(new ClassPathResource("process-events-simple.bpmn"), ResourceType.BPMN2);

        if (processKbuilder.hasErrors()) {
            for (KnowledgeBuilderError error : processKbuilder.getErrors()) {
                System.out.println(">>> Error:" + error.getMessage());

            }
            fail(">>> Knowledge couldn't be parsed! ");
        }

        KnowledgeBase processKbase = KnowledgeBaseFactory.newKnowledgeBase();

        processKbase.addKnowledgePackages(processKbuilder.getKnowledgePackages());
        return processKbase.newStatefulKnowledgeSession();
    }

    private StatefulKnowledgeSession createEventsSession() {
        KnowledgeBuilder eventsKbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        eventsKbuilder.add(new ClassPathResource("analyze-process-events.drl"), ResourceType.DRL);

        if (eventsKbuilder.hasErrors()) {
            for (KnowledgeBuilderError error : eventsKbuilder.getErrors()) {
                System.out.println(">>> Error:" + error.getMessage());

            }
            fail(">>> Knowledge couldn't be parsed! ");
        }

        KnowledgeBaseConfiguration config = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
        config.setOption(EventProcessingOption.STREAM);


        KnowledgeBase eventsKbase = KnowledgeBaseFactory.newKnowledgeBase(config);

        eventsKbase.addKnowledgePackages(eventsKbuilder.getKnowledgePackages());
        StatefulKnowledgeSession eventsKsession = eventsKbase.newStatefulKnowledgeSession();

        ((StatefulKnowledgeSessionImpl) eventsKsession).getInternalWorkingMemory().addEventListener(
                new DefaultWorkingMemoryEventListener() {

                    @Override
                    public void objectInserted(ObjectInsertedEvent event) {
                        event.getWorkingMemory().fireAllRules();
                    }

                    @Override
                    public void objectUpdated(ObjectUpdatedEvent event) {
                        event.getWorkingMemory().fireAllRules();
                    }

                    @Override
                    public void objectRetracted(ObjectRetractedEvent event) {
                        event.getWorkingMemory().fireAllRules();
                    }
                });

        ((StatefulKnowledgeSessionImpl) eventsKsession).getInternalWorkingMemory().addEventListener(
                new DefaultAgendaEventListener() {

                    @Override
                    public void activationCreated(ActivationCreatedEvent event, WorkingMemory workingMemory) {
                        workingMemory.fireAllRules();
                    }
                });

        return eventsKsession;
    }
}
