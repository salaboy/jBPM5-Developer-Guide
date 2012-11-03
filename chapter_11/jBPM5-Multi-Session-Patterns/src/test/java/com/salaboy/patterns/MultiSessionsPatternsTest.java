/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.patterns;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;
import com.salaboy.model.Data;
import com.salaboy.model.Person;
import com.salaboy.sessions.patterns.BusinessEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.UserTransaction;
import org.drools.runtime.rule.FactHandle;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.impl.EnvironmentFactory;
import org.drools.io.impl.ClassPathResource;
import org.drools.persistence.jpa.*;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author salaboy
 */
public class MultiSessionsPatternsTest {

    private PoolingDataSource ds = new PoolingDataSource();
    private static EntityManagerFactory emf;
    public static Environment env;
    public static Map<String, KnowledgeBase> kbases;

    public MultiSessionsPatternsTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        ds.setUniqueName("jdbc/testDS1");

        ds.setClassName("org.h2.jdbcx.JdbcDataSource");
        ds.setMaxPoolSize(3);
        ds.setAllowLocalTransactions(true);
        ds.getDriverProperties().put("user", "sa");
        ds.getDriverProperties().put("password", "sasa");
        ds.getDriverProperties().put("URL", "jdbc:h2:mem:mydb");

        ds.init();

        emf = Persistence.createEntityManagerFactory("org.jbpm.runtime");

        kbases = new HashMap<String, KnowledgeBase>();
    }

    @After
    public void tearDown() {
        ds.close();
    }

    @Test
    public void multiSessionsSharing() {
    }

    @Test
    public void multiSessionsCollaboration() throws Exception {

        EntityManager em = emf.createEntityManager();
        UserTransaction ut = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
        StatefulKnowledgeSession interactionSession = null;
        BusinessEntity interactionSessionEntity = null;
        try {
            // This needs to happen in the same transaction if I want to keep it consistent
            ut.begin();
            interactionSession = createProcessInteractionKnowledgeSession("InteractionSession");

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


        Person person = new Person("Salaboy", 29);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", person);

        StatefulKnowledgeSession ksession = createProcessOneKnowledgeSession(person.getId());

        registerWorkItemHandlers(ksession, person.getId(), em);
        // Let's create a Process Instance

        ksession.startProcess("com.salaboy.process.AsyncInteractions", params);

        ksession.dispose();


        BusinessEntity sessionInteractionKey = (BusinessEntity) em.createQuery("select be from BusinessEntity be where be.businessKey = :key "
                + "and be.active = true").setParameter("key", "InteractionSession").getSingleResult();



        interactionSession = JPAKnowledgeService.loadStatefulKnowledgeSession(sessionInteractionKey.getSessionId(), kbases.get("InteractionSession"), null, env);
        interactionSession.setGlobal("em", em);
        interactionSession.insert(new Data());
        interactionSession.fireAllRules();

        interactionSession.dispose();

    }

    @Test
    public void multiSessionsHierarchy() {
    }

    private StatefulKnowledgeSession createProcessOneKnowledgeSession(String key) {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(new ClassPathResource("process-async-interactions.bpmn"), ResourceType.BPMN2);

        if (kbuilder.hasErrors()) {
            for (KnowledgeBuilderError error : kbuilder.getErrors()) {
                System.out.println(">>> Error:" + error.getMessage());

            }
            fail(">>> Knowledge couldn't be parsed! ");
        }

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        kbases.put(key, kbase);

        env = EnvironmentFactory.newEnvironment();
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
        env.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager());
        // Let's create a Persistence Knowledge Session
        System.out.println(" >>> Let's create a Persistent Knowledge Session");
        final StatefulKnowledgeSession ksession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, null, env);


        return ksession;
    }

    private StatefulKnowledgeSession createProcessInteractionKnowledgeSession(String key) {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(new ClassPathResource("interaction-rules.drl"), ResourceType.DRL);

        if (kbuilder.hasErrors()) {
            for (KnowledgeBuilderError error : kbuilder.getErrors()) {
                System.out.println(">>> Error:" + error.getMessage());

            }
            fail(">>> Knowledge couldn't be parsed! ");
        }

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        kbases.put(key, kbase);

        env = EnvironmentFactory.newEnvironment();
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
        env.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager());
        // Let's create a Persistence Knowledge Session
        System.out.println(" >>> Let's create a Persistent Knowledge Session");
        final StatefulKnowledgeSession ksession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, null, env);
        return ksession;
    }

    public static void registerWorkItemHandlers(StatefulKnowledgeSession ksession, String key, EntityManager em) {
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new MockAsyncHTWorkItemHandler(ksession.getId(), key));
        ksession.getWorkItemManager().registerWorkItemHandler("External Service Call", new MockAsyncExternalServiceWorkItemHandler(em, ksession.getId(), key));
    }

    private static class MockAsyncHTWorkItemHandler implements WorkItemHandler {

        private int sessionId;
        private String businessKey;

        public MockAsyncHTWorkItemHandler(int sessionId, String businessKey) {
            this.sessionId = sessionId;
            this.businessKey = businessKey;
        }

        public void executeWorkItem(WorkItem wi, WorkItemManager wim) {
            System.out.println(">>> Working on a Human Interaction");
            System.out.println(">>> Completing a Human Interaction");
            wim.completeWorkItem(wi.getId(), null);
        }

        public void abortWorkItem(WorkItem wi, WorkItemManager wim) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static class MockAsyncExternalServiceWorkItemHandler implements WorkItemHandler {

        private int sessionId;
        private String businessKey;
        private EntityManager em;

        public MockAsyncExternalServiceWorkItemHandler(EntityManager em, int sessionId, String businessKey) {
            this.sessionId = sessionId;
            this.businessKey = businessKey;
            this.em = em;
        }

        public void executeWorkItem(WorkItem wi, WorkItemManager wim) {
            System.out.println(">>> Working in an External Interaction");
            long workItemId = wi.getId();
            long processInstanceId = wi.getProcessInstanceId();
            if (businessKey == null || businessKey.equals("")) {
                //If we don't want to set the business key, the external system can 
                // give us an interaction reference that can be used later to 
                // complete this work item
                businessKey = UUID.randomUUID().toString();
            }
            BusinessEntity businessEntity = new BusinessEntity(sessionId, processInstanceId, workItemId, businessKey);
            System.out.println(" ### : Persisting: " + businessEntity.toString());
            em.persist(businessEntity);

            BusinessEntity sessionInteractionKey = (BusinessEntity) em.createQuery("select be from BusinessEntity be where be.businessKey = :key "
                    + "and be.active = true").setParameter("key", "InteractionSession").getSingleResult();


            StatefulKnowledgeSession ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(sessionInteractionKey.getSessionId(), kbases.get("InteractionSession"), null, env);
            ksession.insert(businessEntity);
            ksession.fireAllRules();



        }

        public void abortWorkItem(WorkItem wi, WorkItemManager wim) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public static void completeInteraction(StatefulKnowledgeSession ksession, EntityManager em, BusinessEntity entity, Data data) throws Exception {
            Map<String, Object> results = data.getDataMap();
            ksession.getWorkItemManager().completeWorkItem(entity.getWorkItemId(), results);
            markBusinessEntityAsCompleted(entity, em);

    }
    
    private static void markBusinessEntityAsCompleted(BusinessEntity entity, EntityManager em) {
        em.joinTransaction();
        entity.setActive(false);
        System.out.println("Merging Business Entity: " + entity);
        em.merge(entity);
    }
}
