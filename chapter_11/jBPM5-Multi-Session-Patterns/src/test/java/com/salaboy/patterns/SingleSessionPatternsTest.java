/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.patterns;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;
import com.salaboy.model.Person;
import com.salaboy.patterns.handler.MockAsyncExternalServiceWorkItemHandler;
import com.salaboy.sessions.patterns.BusinessEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        
        //Initial parameters for process instance #1
        Person person = new Person("Salaboy", 29);
        Map<String, Object> params1 = new HashMap<String, Object>();
        params1.put("person", person);
        
        //Creates the ksession for process instance #1
        StatefulKnowledgeSession ksession1 = createProcessOneKnowledgeSession(person.getId());
        registerWorkItemHandlers(ksession1, person.getId());
        int ksession1Id = ksession1.getId();
        
        //Starts process instance #1
        ksession1.startProcess("com.salaboy.process.AsyncInteractions", params1);
        
        //We don't want to use the ksession anymore so we will dispose it.
        //At this point MockAsyncExternalServiceWorkItemHandler has persisted
        //a business key that we can use later to retireve the session from
        //the database and continue with the execution of the process.
        ksession1.dispose();
        
        
        
        //Initial parameters for process instance #2
        Person person2 = new Person("Salaboy2", 29);
        Map<String, Object> params2 = new HashMap<String, Object>();
        params2.put("person", person2);
        
        //Creates a new ksession for process instance #2
        StatefulKnowledgeSession ksession2 = createProcessTwoKnowledgeSession(person2.getId());
        registerWorkItemHandlers(ksession2, person2.getId());
        int ksession2Id = ksession2.getId();
        
        //Starts process instance #2
        ksession2.startProcess("com.salaboy.process.AsyncInteractions", params2);
        
        //Dispose ksession2 as we don't want to use it anymore. Just like with
        //process instance #1, the work item handler associated to the task nodes
        //of the process has persisted a business key that we can use to continue
        //with the execution of this session later.
        ksession2.dispose();
        
        
        
        
        //Let's find the BusinessEntity persisted by process instance #2.
        //The key of the BusinessEntity is the the persnon's id.
        BusinessEntity businessEntity = getBusinessEntity(person2.getId());
        assertNotNull(businessEntity);
        //the BusinessEntity must be of session #2
        assertEquals(businessEntity.getSessionId(), ksession2Id);
        
        
        //Let' restore the session #2 using the information present in the BusinessEntity
        //Since we keep one kbase per ksession we also need to get it using
        //the information present in the BusinessEntity.
        ksession2 = JPAKnowledgeService.loadStatefulKnowledgeSession(businessEntity.getSessionId(), kbases.get(businessEntity.getBusinessKey()), null, env);
        registerWorkItemHandlers(ksession2, businessEntity.getBusinessKey());
        assertNotNull(ksession2);
        
        //Now that we have session #2 back we can complete the pending work item 
        //handler with the information present in BusinessEntity we can 
        ksession2.getWorkItemManager().completeWorkItem(businessEntity.getWorkItemId(), null);
        
        //The BusinessEntity is no longer needed so we can marked as completed
        //in the database.
        markBusinessEntityAsCompleted(businessEntity.getId());
        
        //We are done with ksession #2
        ksession2.dispose();
        
        
        
        //Now we are going to complete the pending work item handler of 
        //the process instance #1, but first we need to restore the session from
        //the database.
        businessEntity = getBusinessEntity(person.getId());
        assertNotNull(businessEntity);
        
        //the BusinessEntity must be of session #1
        assertEquals(businessEntity.getSessionId(), ksession1Id);
        
        //load the ksession using the information present in BusinessEntity
        ksession1 = JPAKnowledgeService.loadStatefulKnowledgeSession(businessEntity.getSessionId(), kbases.get(businessEntity.getBusinessKey()), null, env);
        registerWorkItemHandlers(ksession1, businessEntity.getBusinessKey());
        assertNotNull(ksession1);
        
        //complete the pending work item handler
        ksession1.getWorkItemManager().completeWorkItem(businessEntity.getWorkItemId(), null);

        //mark the BusinessEntity as completed
        markBusinessEntityAsCompleted(businessEntity.getId());
        
        //dispose ksession #1
        ksession1.dispose();
        
        //We shouldn't have more active business entities in the database.
        List<BusinessEntity> activeBusinessEntities = getActiveBusinessEntities();
        assertTrue(activeBusinessEntities.isEmpty());
        
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
    
    
    /**
     * Returns the list of all active BusinessEntities in the database.
     * @return the list of all active BusinessEntities in the database
     */
    private List<BusinessEntity> getActiveBusinessEntities(){
        EntityManager em = emf.createEntityManager();
        List<BusinessEntity> businessEntities = em.createQuery("select be from BusinessEntity be where be.active = true")
            .getResultList();
        em.close();
        
        return businessEntities;
    }
    
    /**
     * Queries the database to retrieve a {@link BusinessEntity} given its key.
     * Only active BusinessEntities are returned.
     * @param key the key of the BusinessEntity.
     * @return the BusinessEntity with the given key.
     */
    private BusinessEntity getBusinessEntity(String key){
        EntityManager em = emf.createEntityManager();
        BusinessEntity businessEntity = (BusinessEntity)em.createQuery("select be from BusinessEntity be where be.businessKey = :key "
                                                            + "and be.active = true")
                .setParameter("key", key)
                .getSingleResult();
        em.close();
        return businessEntity;
    }
    
    /**
     * Queries the database to retrieve a {@link BusinessEntity} given its key
     * and processId.
     * Only active BusinessEntities are returned.
     * @param key the key of the BusinessEntity.
     * @param processId the process id
     * @return the BusinessEntity with the given key.
     */
    private BusinessEntity getBusinessEntity(String key, long processId){
        EntityManager em = emf.createEntityManager();
        BusinessEntity businessEntity = (BusinessEntity)em.createQuery("select be from BusinessEntity be where be.businessKey = :key "
                                                        + " and be.processId = :processId"
                                                        + " and be.active = true")
            .setParameter("key", key)
            .setParameter("processId", processId)
            .getSingleResult();
        em.close();
        return businessEntity;
    }
    
    /**
     * Sets the 'active' property of the businessEntity as 'false' and persists
     * it into the database.
     * @param businessEntity 
     */
    private void markBusinessEntityAsCompleted(Long businessEntityId){
        EntityManager em = emf.createEntityManager();
        BusinessEntity businessEntity = em.find(BusinessEntity.class, businessEntityId);
        businessEntity.setActive(false);
        em.merge(businessEntity);
        em.close();
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
        MockAsyncExternalServiceWorkItemHandler mockExternalServiceWorkItemHandler = new MockAsyncExternalServiceWorkItemHandler(emf, ksession.getId(), key);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", mockExternalServiceWorkItemHandler);
        ksession.getWorkItemManager().registerWorkItemHandler("External Service Call", mockExternalServiceWorkItemHandler);
    }
    
}
