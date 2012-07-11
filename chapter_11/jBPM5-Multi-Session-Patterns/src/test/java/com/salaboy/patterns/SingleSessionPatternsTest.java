/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.patterns;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;
import com.salaboy.model.Person;
import com.salaboy.sessions.patterns.BusinessEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
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
public class SingleSessionPatternsTest {

    private PoolingDataSource ds = new PoolingDataSource();
    private EntityManagerFactory emf;
    private Environment env;
    private Map<String, KnowledgeBase> kbases;
    public SingleSessionPatternsTest() {
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
    public void singleSessionPerProcessInstance() {
        Person person = new Person("Salaboy", 29);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", person);
        
        StatefulKnowledgeSession ksession = createProcessOneKnowledgeSession(person.getId());

        registerWorkItemHandlers(ksession, person.getId());
         // Let's create a Process Instance
        
        ksession.startProcess("com.salaboy.process.AsyncInteractions", params);
        
        ksession.dispose();
        
        Person person2 = new Person("Salaboy2", 29);
        Map<String, Object> params2 = new HashMap<String, Object>();
        params2.put("person", person2);
        
        StatefulKnowledgeSession ksession2 = createProcessTwoKnowledgeSession(person2.getId());
        
        registerWorkItemHandlers(ksession2, person2.getId());
        ksession2.startProcess("com.salaboy.process.AsyncInteractions", params2);
        
        ksession2.dispose();
        
        EntityManager em = emf.createEntityManager();
        BusinessEntity businessEntity = (BusinessEntity)em.createQuery("select be from BusinessEntity be where be.businessKey = :key "
                                                                            + "and be.active = true")
                .setParameter("key", person2.getId())
                .getSingleResult();
        
        em.close();
        
        /*
         * We need:
         *  the sessionId
         *  the kbase which can be recreated using the same knowledge resources / packages or stored locally
         *  the environment which can be recreated or reused as well
         */ 
        ksession2 = JPAKnowledgeService.loadStatefulKnowledgeSession(businessEntity.getSessionId(), kbases.get(businessEntity.getBusinessKey()), null, env);
        registerWorkItemHandlers(ksession2, businessEntity.getBusinessKey());
        assertNotNull(ksession2);
        
        ksession2.getWorkItemManager().completeWorkItem(businessEntity.getWorkItemId(), null);
        em = emf.createEntityManager();
        businessEntity.setActive(false);
        em.merge(businessEntity);
        em.close();
        ksession2.dispose();
        
        em = emf.createEntityManager();
        businessEntity = (BusinessEntity)em.createQuery("select be from BusinessEntity be where be.businessKey = :key "
                                                            + "and be.active = true")
                .setParameter("key", person.getId())
                .getSingleResult();
        em.close();
        
        
        ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(businessEntity.getSessionId(), kbases.get(businessEntity.getBusinessKey()), null, env);
        registerWorkItemHandlers(ksession, businessEntity.getBusinessKey());
        assertNotNull(ksession);
        
        ksession.getWorkItemManager().completeWorkItem(businessEntity.getWorkItemId(), null);
        em = emf.createEntityManager();
        businessEntity.setActive(false);
        em.merge(businessEntity);
        em.close();
        
        ksession.dispose();

        
    }
    
    
    @Test
    public void singleSessionPerProcessDefinition() {
        Person person = new Person("Salaboy", 29);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", person);
        
        StatefulKnowledgeSession ksession = createProcessOneKnowledgeSession("myProcessDefinitionSession");
        
        registerWorkItemHandlers(ksession,null);
         // Let's create a Process Instance
        
        
        ksession.startProcess("com.salaboy.process.AsyncInteractions", params);
        
        ksession.dispose();
        
        EntityManager em = emf.createEntityManager();
        BusinessEntity businessEntity = (BusinessEntity)em.createQuery("select be from BusinessEntity be "
                                                                            + "where be.active = true")
                
                .getSingleResult();
        
        em.close();
        
        
        Person person2 = new Person("Salaboy", 29);
        Map<String, Object> params2 = new HashMap<String, Object>();
        params2.put("person", person2);
        
        ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(businessEntity.getSessionId(), kbases.get("myProcessDefinitionSession"), null, env);
        registerWorkItemHandlers(ksession, null);
        assertNotNull(ksession);
        
        ksession.startProcess("com.salaboy.process.AsyncInteractions", params2);
        
        ksession.dispose();
        
        
        // Getting the correct work item to finish
        //      If we don't know which workItem do we want to complete we can create a query to see which are pending work items
        //          for a process or for a more complex business key
        // If the thread that wants to notify the engine about the completion of the external interaction is the 
        //   one which create the token inside the WorkItemHandler it can use that unique value to get the related workItemId
        em = emf.createEntityManager();
        businessEntity = (BusinessEntity)em.createQuery("select be from BusinessEntity be where "
                                                            + " be.workItemId = :workItemId and be.active = true")
                .setParameter("workItemId", 1L)
                .getSingleResult();
        
        em.close();
        
        ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(businessEntity.getSessionId(), kbases.get("myProcessDefinitionSession"), null, env);
        registerWorkItemHandlers(ksession, null);
        assertNotNull(ksession);
        
        ksession.getWorkItemManager().completeWorkItem(businessEntity.getWorkItemId(), null);
        em = emf.createEntityManager();
        businessEntity.setActive(false);
        em.merge(businessEntity);
        em.close();
        
        ksession.dispose();
        
        // The only pending workItem related to the processId 2 should be 2
        // We can create queries to find out the pending workItems for a process instance or to find a process
        //      instance related to a business scenario using this approach
        em = emf.createEntityManager();
        businessEntity = (BusinessEntity)em.createQuery("select be from BusinessEntity be where "
                                                            + " be.processId = :processId"
                                                            + " and be.active = true")
                
                .setParameter("processId", 2L)        
                .getSingleResult();
        
        em.close();
        
        assertEquals(2, businessEntity.getWorkItemId());
        
    }
    
    @Test
    public void singleSessionPerProcessDefinitionWithRules() {
        
        
        StatefulKnowledgeSession ksession = createProcessWithRulesKnowledgeSession("myProcessDefinitionSession");
        
        registerWorkItemHandlers(ksession,"myProcessDefinitionSession");
         // Let's create a Process Instance
        
        Person person = new Person("Salaboy", 29);
        FactHandle handlePerson = ksession.insert(person);
        ksession.fireAllRules();
        
        ksession.dispose();
        
        EntityManager em = emf.createEntityManager();
        BusinessEntity businessEntity = (BusinessEntity)em.createQuery("select be from BusinessEntity be where be.businessKey = :key "
                                                                            + "and be.active = true")
                .setParameter("key", "myProcessDefinitionSession")
                .getSingleResult();
        
        em.close();
        
        
        
        
        ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(businessEntity.getSessionId(), kbases.get("myProcessDefinitionSession"), null, env);
        registerWorkItemHandlers(ksession, "myProcessDefinitionSession");
        assertNotNull(ksession);
        
        Person person2 = new Person("Salaboy", 29);
        FactHandle handlePerson2 = ksession.insert(person2);
        ksession.fireAllRules();
        
        
        ksession.dispose();
        
        
        // Getting the correct work item to finish
        
        //      If we don't know which workItem do we want to complete we can create a query to see which are pending work items
        //          for a process or for a more complex business key
        em = emf.createEntityManager();
        businessEntity = (BusinessEntity)em.createQuery("select be from BusinessEntity be where be.businessKey = :key "
                                                            + "and be.workItemId = :workItemId and be.active = true")
                .setParameter("key", "myProcessDefinitionSession")
                .setParameter("workItemId", 1L)
                .getSingleResult();
        
        em.close();
        
        ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(businessEntity.getSessionId(), kbases.get("myProcessDefinitionSession"), null, env);
        registerWorkItemHandlers(ksession, "myProcessDefinitionSession");
        assertNotNull(ksession);
        
        ksession.getWorkItemManager().completeWorkItem(businessEntity.getWorkItemId(), null);
        em = emf.createEntityManager();
        businessEntity.setActive(false);
        em.merge(businessEntity);
        em.close();
        
        ksession.dispose();
        
        // The only pending workItem related to the processId 2 should be 2
        // We can create queries to find out the pending workItems for a process instance or to find a process
        //      instance related to a business scenario using this approach
        em = emf.createEntityManager();
        businessEntity = (BusinessEntity)em.createQuery("select be from BusinessEntity be where be.businessKey = :key "
                                                            + " and be.processId = :processId"
                                                            + " and be.active = true")
                .setParameter("key", "myProcessDefinitionSession")
                .setParameter("processId", 2L)        
                .getSingleResult();
        
        em.close();
        
        assertEquals(2, businessEntity.getWorkItemId());
      
        
//        ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(businessEntity.getSessionId(), kbases.get("myProcessDefinitionSession"), null, env);
//        ksession.retract(handlePerson);
//        ksession.retract(handlePerson2);
//        ksession.fireAllRules();
//        ksession.dispose();
        
    }
    
    private StatefulKnowledgeSession createProcessOneKnowledgeSession(String key){
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
    
    private StatefulKnowledgeSession createProcessTwoKnowledgeSession(String key){
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(new ClassPathResource("process-async-interactions2.bpmn"), ResourceType.BPMN2);

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
    
    
    private StatefulKnowledgeSession createProcessWithRulesKnowledgeSession(String key){
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(new ClassPathResource("process-async-interactions.bpmn"), ResourceType.BPMN2);
        kbuilder.add(new ClassPathResource("start-process-rules.drl"), ResourceType.DRL);

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
    
    private void registerWorkItemHandlers(StatefulKnowledgeSession ksession, String key){
        MockAsyncHTWorkItemHandler mockHTWorkItemHandler = new MockAsyncHTWorkItemHandler(ksession.getId(), key);
        MockAsyncExternalServiceWorkItemHandler mockExternalServiceWorkItemHandler = new MockAsyncExternalServiceWorkItemHandler(ksession.getId(), key);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", mockHTWorkItemHandler);
        ksession.getWorkItemManager().registerWorkItemHandler("External Service Call", mockExternalServiceWorkItemHandler);
    }
    
    private class MockAsyncHTWorkItemHandler implements WorkItemHandler {

        private int sessionId;
        private String businessKey;

        public MockAsyncHTWorkItemHandler(int sessionId, String businessKey) {
            this.sessionId = sessionId;
            this.businessKey = businessKey;
        }

        
        public void executeWorkItem(WorkItem wi, WorkItemManager wim) {
            System.out.println(">>> Working on a Human Interaction");
            long workItemId = wi.getId();
            long processInstanceId = wi.getProcessInstanceId();
            EntityManager em = emf.createEntityManager();
            if(businessKey == null || businessKey.equals("")){
                //If we don't want to set the business key, the external system can 
                // give us an interaction reference that can be used later to 
                // complete this work item
                businessKey = UUID.randomUUID().toString();
            }
            BusinessEntity businessEntity = new BusinessEntity(sessionId, processInstanceId, workItemId, businessKey);
            System.out.println(" ### : Persisting: "+businessEntity.toString());
            em.persist(businessEntity);
            em.close();
            
        }

        public void abortWorkItem(WorkItem wi, WorkItemManager wim) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

   
    private class MockAsyncExternalServiceWorkItemHandler implements WorkItemHandler {
        
        private int sessionId;
        private String businessKey;

        public MockAsyncExternalServiceWorkItemHandler(int sessionId, String businessKey) {
            this.sessionId = sessionId;
            this.businessKey = businessKey;
        }

       
        public void executeWorkItem(WorkItem wi, WorkItemManager wim) {
            System.out.println(">>> Working in an External Interaction");
            long workItemId = wi.getId();
            long processInstanceId = wi.getProcessInstanceId();
            EntityManager em = emf.createEntityManager();
            if(businessKey == null || businessKey.equals("")){
                //If we don't want to set the business key, the external system can 
                // give us an interaction reference that can be used later to 
                // complete this work item
                businessKey = UUID.randomUUID().toString();
            }
            BusinessEntity businessEntity = new BusinessEntity(sessionId, processInstanceId, workItemId, businessKey);
            System.out.println(" ### : Persisting: "+businessEntity.toString());
            em.persist(businessEntity);
            em.close();
            
        }

        public void abortWorkItem(WorkItem wi, WorkItemManager wim) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
       
    }
    
}
