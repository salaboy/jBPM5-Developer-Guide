/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.patterns;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;
import com.salaboy.sessions.patterns.BusinessEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.impl.EnvironmentFactory;
import org.drools.io.Resource;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Base class with utility methods used by the tests of this project
 * @author esteban
 */
public abstract class SessionsPatternsTestsBase implements KnowledgeSessionSupport {
    
    private PoolingDataSource ds = new PoolingDataSource();
    private Map<String, KnowledgeBase> kbases;
    private EntityManagerFactory emf;
    
    
    /**
     * Configure the data source used to persist the sessions used in these
     * tests.
     */
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
    
    /**
     * Retrieves the {@link BusinessEntity} related to a workItemId from the 
     * database. 
     * @param workItemId the workItemId.
     * @param em the EntityManager to be used.
     * @return the {@link BusinessEntity} related to a workItemId from the 
     * database
     */
    public BusinessEntity getBusinessEntityByWorkItemId(long workItemId, EntityManager em) {
        return (BusinessEntity) em.createQuery("select be from BusinessEntity be where be.workItemId = :workItemId and be.active = true")
                .setParameter("workItemId", workItemId)
                .getSingleResult();
    }

    /**
     * Returns the list of all active BusinessEntities in the database.
     * @param em the EntityManager to be used.
     * @return the list of all active BusinessEntities in the database
     */
    public List<BusinessEntity> getActiveBusinessEntities(EntityManager em) {
        List<BusinessEntity> businessEntities = em.createQuery("select be from BusinessEntity be where be.active = true").getResultList();
        return businessEntities;
    }

    /**
     * Returns the list of all inactive BusinessEntities in the database.
     * @param em the EntityManager to be used.
     * @return the list of all inactive BusinessEntities in the database
     */
    public List<BusinessEntity> getInactiveBusinessEntities(EntityManager em) {
        List<BusinessEntity> businessEntities = em.createQuery("select be from BusinessEntity be where be.active = false").getResultList();
        return businessEntities;
    }

    /**
     * Queries the database to retrieve a {@link BusinessEntity} given its key.
     * Only active BusinessEntities are returned.
     * @param key the key of the BusinessEntity.
     * @param em the EntityManager to be used.
     * @return the BusinessEntity with the given key.
     */
    public BusinessEntity getBusinessEntity(String key, EntityManager em) {
        BusinessEntity businessEntity = (BusinessEntity) em.createQuery("select be from BusinessEntity be where be.businessKey = :key "
                + "and be.active = true")
                .setParameter("key", key)
                .getSingleResult();

        return businessEntity;
    }

    /**
     * Queries the database to retrieve all the {@link BusinessEntity} belonging
     * to a process instance. Only active BusinessEntities are returned.
     * @param processId the process id
     * @param em the EntityManager to be used.
     * @return the {@link BusinessEntity} belonging to a process instance
     */
    public List<BusinessEntity> getBusinessEntitiesProcessId(long processId, EntityManager em) {
        List<BusinessEntity> businessEntities = em.createQuery("select be from BusinessEntity be where  "
                + " be.processId = :processId"
                + " and be.active = true")
                .setParameter("processId", processId)
                .getResultList();
        return businessEntities;
    }

    /**
     * Sets the 'active' property of the businessEntity as 'false' and persists
     * it into the database.
     * @param businessEntity the buisnessEntity to be marked as not active.
     * @param em the EntityManager to be used.
     */
    public void markBusinessEntityAsCompleted(Long businessEntityId, EntityManager em) {
        em.joinTransaction();
        BusinessEntity businessEntity = em.find(BusinessEntity.class, businessEntityId);
        businessEntity.setActive(false);
        System.out.println("Merging Business Entity: " + businessEntity);
        em.merge(businessEntity);
    }
    
    /**
     * Completes the work item handler associated with a {@link BusinessEntity}.
     * This method is typically invoked from within the rules of this test.
     * @param ksession the session to be used
     * @param em the Entity Manager to be used
     * @param entity the BuisnessEntity
     * @param results the results to use in the completion
     * @throws Exception 
     */
    @Override
    public void completeInteraction(StatefulKnowledgeSession ksession, EntityManager em, BusinessEntity entity, Map<String, Object> results) throws Exception {
        ksession.getWorkItemManager().completeWorkItem(entity.getWorkItemId(), results);
        markBusinessEntityAsCompleted(entity.getId(), em);
    }
    
    /**
     * Creates a new Knowledge Base with the passed resources and returns a fresh
     * ksession from it. This method register the created kbase in {@link #kbases}
     * with the key passed as parameter. The returned session is configured as
     * persistent.
     * @param key the key used to register the generated kbase in {@link #kbases}.
     * @param resources The resources to be placed inside the generated kbase.
     * @return 
     */
    protected StatefulKnowledgeSession createKnowledgeSession(String key, Map<Resource, ResourceType> resources) {
        
        //Creates a new kbuilder
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        
        //Adds al the given resources
        for (Map.Entry<Resource, ResourceType> entry : resources.entrySet()) {
            kbuilder.add(entry.getKey(), entry.getValue());
        }

        //If there is any compilation error then fail!
        if (kbuilder.hasErrors()) {
            for (KnowledgeBuilderError error : kbuilder.getErrors()) {
                System.out.println(">>> Error:" + error.getMessage());

            }
            Assert.fail(">>> Knowledge couldn't be parsed! ");
        }

        //Creates a new kbase
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        
        //Add the generated knowledge packages from kbuilder.
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        
        //Register the kbase in this.kbases
        kbases.put(key, kbase);

        //Creates a Persistence Knowledge Session
        System.out.println(" >>> Let's create a Persistent Knowledge Session");
        final StatefulKnowledgeSession ksession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, null, createEnvironment());

        return ksession;
    }
    
    /**
     * Same as {@link #loadKnowldgeSession(int, java.lang.String, java.lang.String, javax.persistence.EntityManager) 
     * loadKnowldgeSession(id, sessionName, sessionName, em)}
     */
    @Override
    public StatefulKnowledgeSession loadKnowldgeSession(int id, String sessionName, EntityManager em){
        StatefulKnowledgeSession ksession = 
            JPAKnowledgeService
              .loadStatefulKnowledgeSession(id, 
                getKbase(sessionName), 
                null, createEnvironment());
        
        registerWorkItemHandlers(ksession, sessionName, em);
        
        return ksession;
    }
    
    /**
     * Loads a knowledge session from the database and registers its work item
     * handlers.
     * @param id The session id.
     * @param sessionName The sessionName used to retrieve the correct kbase
     * for the session.
     * @param businessKey the business key used to register the work item
     * handlers.
     * @param em The entity manager that will be used by the registered work
     * item handlers.
     * @return 
     */
    @Override
    public StatefulKnowledgeSession loadKnowldgeSession(int id, String sessionName, String businessKey, EntityManager em){
        StatefulKnowledgeSession ksession = 
            JPAKnowledgeService
              .loadStatefulKnowledgeSession(id, 
                getKbase(sessionName), 
                null, createEnvironment());
        
        registerWorkItemHandlers(ksession, businessKey, em);
        
        return ksession;
    }
    
    /**
     * Creates the persistence environment used by jBPM to handle persistent
     * sessions.
     * @return a new environment.
     */
    public Environment createEnvironment() {

        Environment env = EnvironmentFactory.newEnvironment();
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
        env.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager());
        return env;
    }

    public EntityManagerFactory getEmf() {
        return emf;
    }
    
    protected KnowledgeBase getKbase(String key){
        return kbases.get(key);
    }
    
    protected abstract void registerWorkItemHandlers(StatefulKnowledgeSession ksession, String key, EntityManager em);
    
}
