package com.salaboy.patterns;

import com.salaboy.model.Data;
import com.salaboy.model.Person;
import com.salaboy.patterns.handler.MockAsyncExternalServiceWorkItemHandler;
import com.salaboy.patterns.handler.MockSyncHTWorkItemHandler;
import com.salaboy.sessions.patterns.BusinessEntity;
import com.salaboy.sessions.patterns.SessionLocator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.Test;
import org.drools.persistence.jpa.JPAKnowledgeService;
import static org.junit.Assert.*;

/**
 * Test that shows how multiple sessions can collaborate to fulfill a business 
 * goal.
 * @author salaboy
 */
public class MultiSessionsPatternsTest extends SessionsPatternsTestsBase{


    public MultiSessionsPatternsTest() {
    }

    /*
     * This test shows how a master session can automatically complete a work 
     * item handler in a slave session when the required information is present. 
     */
    @Test
    public void multiSessionsCollaboration() throws Exception {
        //Creates an entity manager and get the user transaction. We are going
        //to need them later to interact with the business entities persisted
        //by the work item handlers we have configured in our session.
        EntityManager em = getEmf().createEntityManager();
        UserTransaction ut = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
        
        //Creates and persists a new BusinessEntity containing the information
        //required by this test.
        StatefulKnowledgeSession interactionSession = null;
        BusinessEntity interactionSessionEntity = null;
        try {
            // This needs to happen in the same transaction if I want to keep it consistent
            ut.begin();
            //Creates a new session.
            interactionSession = createProcessInteractionKnowledgeSession("InteractionSession", em);
            //persists the required business entity.
            interactionSessionEntity = new BusinessEntity(interactionSession.getId(), 0, 0, "InteractionSession");
            em.joinTransaction(); // I need to join the Drools/jBPM transaction 
            em.persist(interactionSessionEntity);
            ut.commit();
        } catch (Exception e) {
            System.out.println("Rolling Back because of: " + e.getMessage());
            ut.rollback();
            fail(e.getMessage());
        }
        assertNotNull(interactionSessionEntity);
        assertNotNull(interactionSessionEntity.getId());
        
        //Dispose the session.
        interactionSession.dispose();


        //Initial parameters for process instance #1
        Person person = new Person("Salaboy", 29);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", person);

        //Creates the ksession for process instance #1
        StatefulKnowledgeSession ksession = createProcessOneKnowledgeSession(person.getId());
        registerWorkItemHandlers(ksession, person.getId(), em);

        //Starts process instance #1
        ksession.startProcess("com.salaboy.process.AsyncInteractions", params);

        //Disposes the session.
        ksession.dispose();


        // The interaction Session has the responsability of mapping the WorkItems Ids with their corresponding
        // Business Interactions. This can be done with a Map/Registry or with a Knowledge Session to 
        // describe more complex patterns
        BusinessEntity sessionInteractionKey = getBusinessEntity("InteractionSession", em);
        interactionSession = loadKnowldgeSession(sessionInteractionKey.getSessionId(), "InteractionSession", em);
        interactionSession.setGlobal("em", em);
        interactionSession.setGlobal("ksessionSupport", this);

        // Look for all the pending Business Keys which represent an interaction and insert them into the interaction session
        List<BusinessEntity> pendingBusinessEntities = getActiveBusinessEntities(em);
        for(BusinessEntity be : pendingBusinessEntities){
            if(!be.getBusinessKey().equals("InteractionSession")){
                interactionSession.insert(be);
            }
        }
        
        // As soon as we add Data the completion will be triggered and the process will continue
        interactionSession.insert(new Data());
        interactionSession.fireAllRules();

        ksession.dispose();
    }

    /**
     * This test uses the concept of a SessionLocator to register slaves sessions.
     * Based on rules, the master session decides which (process definition), 
     * when (declaratively expressed with rules) and where (in which slave session)
     * to start a process instance.
     */
    @Test
    public void multiSessionsWithSessionLocator() throws Exception {
        EntityManager em = getEmf().createEntityManager();
        UserTransaction ut = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
        StatefulKnowledgeSession interactionSession = null;
        BusinessEntity interactionSessionEntity = null;
        try {
            // This needs to happen in the same transaction if I want to keep it consistent
            ut.begin();
            interactionSession = createProcessInteractionKnowledgeSession("InteractionSession", em);

            interactionSessionEntity = new BusinessEntity(interactionSession.getId(), 0, 0, "InteractionSession");
            em.joinTransaction(); // I need to join the Drools/jBPM transaction 
            em.persist(interactionSessionEntity);

            ut.commit();


        } catch (Exception e) {
            System.out.println("Rolling Back because of: " + e.getMessage());
            ut.rollback();
        }
        assertNotNull(interactionSessionEntity);
        assertNotNull(interactionSessionEntity.getId());
        interactionSession.dispose();
        // Let's create a session which contains a process and register it in the interaction session:
        StatefulKnowledgeSession processSession = createProcessOneKnowledgeSessionAndRegister("My Business Unit Session", interactionSessionEntity, em);

        processSession.dispose();

        Person person = new Person("Salaboy", 29);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", person);


        interactionSession = loadKnowldgeSession(interactionSessionEntity.getSessionId(), "InteractionSession", em);
        KnowledgeRuntimeLoggerFactory.newConsoleLogger(interactionSession);
        interactionSession.setGlobal("em", em);
        interactionSession.setGlobal("ksessionSupport", this);
        
        
        // Let's insert a fact in the master session and let the rules choose the appropriate session for us to start a process
        interactionSession.insert(person);
        // The process will be selected and started. Because it contains an async activity a new BusinessEntity will be created
        interactionSession.fireAllRules();
   
        
         // Look for all the pending Business Keys which represent an interaction and insert them into the interaction session
        List<BusinessEntity> pendingBusinessEntities = em.createQuery("select be from BusinessEntity be where  "
                + " be.active = true").getResultList();
        for(BusinessEntity be : pendingBusinessEntities){
            if(!be.getBusinessKey().equals("InteractionSession")){
                interactionSession.insert(be);
            }
        }
        // As soon as we add Data the completion will be triggered and the process will continue
        interactionSession.insert(new Data());
        
        interactionSession.fireAllRules();
        
        interactionSession.dispose();

    }

    /**
     * Creates a new ksession containing a single process definition: 
     * 'process-async-interactions.bpmn'.
     * This method uses {@link #createKnowledgeSession(java.lang.String, java.util.Map)}
     * in order to create the ksession.
     * @param key The key used to register the kbase created with the resources
     * used by this method.
     * @return a new ksession containing a single process definition.
     */
    private StatefulKnowledgeSession createProcessOneKnowledgeSession(String key) {
        
        Map<Resource,ResourceType> resources = new HashMap<Resource, ResourceType>();
        resources.put(ResourceFactory.newClassPathResource("process-async-interactions.bpmn"), ResourceType.BPMN2);
        
        return this.createKnowledgeSession(key, resources);
    }

    private StatefulKnowledgeSession createProcessOneKnowledgeSessionAndRegister(String key, BusinessEntity interactionSessionEntity, EntityManager em) {

        final StatefulKnowledgeSession ksession = this.createProcessOneKnowledgeSession(key);

        // Registering a SessionLocator inside the interaction Session
        StatefulKnowledgeSession interactionSession = 
                loadKnowldgeSession(
                    interactionSessionEntity.getSessionId(), 
                    interactionSessionEntity.getBusinessKey(), 
                    em);

        Map<String, String> props = new HashMap<String, String>();
        props.put("process", "com.salaboy.process.AsyncInteractions");
        SessionLocator sessionLocator = new SessionLocator(ksession.getId(), key);
        sessionLocator.setProps(props);
        interactionSession.insert(sessionLocator);

        interactionSession.dispose();

        return ksession;
    }

    /**
     * Creates a new ksession containing a single rules resource: 
     * 'interaction-rules.drl'.
     * This method uses {@link #createKnowledgeSession(java.lang.String, java.util.Map)}
     * in order to create the ksession.
     * @param key The key used to register the kbase created with the resources
     * used by this method.
     * @param em The entity manager registered as a global in the created
     * session.
     * @return a new ksession containing a single process definition.
     */
    private StatefulKnowledgeSession createProcessInteractionKnowledgeSession(String key, EntityManager em) {
        Map<Resource,ResourceType> resources = new HashMap<Resource, ResourceType>();
        resources.put(ResourceFactory.newClassPathResource("interaction-rules.drl"), ResourceType.DRL);
        
        //creates the session
        StatefulKnowledgeSession ksession = this.createKnowledgeSession(key, resources);
        
        //register globals
        ksession.setGlobal("em", em);
        ksession.setGlobal("ksessionSupport", this);
        
        return ksession;
    }
    
    /**
     * Register the Work Item handler we are going to use in our processes.
     * For 'External Service Call' tasks, an instance of 
     * {@link MockAsyncExternalServiceWorkItemHandler} is used. For 'Human Task' 
     * tasks, an instance of {@link MockSyncHTWorkItemHandler} is used.
     * @param ksession the session where the handlers are registered.
     * @param key The business key used to instantiate {@link MockAsyncExternalServiceWorkItemHandler}
     * @param em The entity manager that the instance of {@link MockAsyncExternalServiceWorkItemHandler}
     * will use.
     */
    public void registerWorkItemHandlers(StatefulKnowledgeSession ksession, String key, EntityManager em) {
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new MockSyncHTWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("External Service Call", new MockAsyncExternalServiceWorkItemHandler(em, ksession.getId(), key));
    }

}
