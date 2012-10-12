/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;
import com.salaboy.model.Person;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.UserTransaction;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.SystemEventListenerFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.impl.EnvironmentFactory;
import org.drools.io.impl.ClassPathResource;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.*;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;
import org.jbpm.process.workitem.wsht.LocalHTWorkItemHandler;
import org.jbpm.task.Group;
import org.jbpm.task.Task;
import org.jbpm.task.TaskService;
import org.jbpm.task.User;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.local.LocalTaskService;
import org.jbpm.workflow.instance.WorkflowRuntimeException;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author salaboy
 */
public class PersistentProcessTest {

    private PoolingDataSource ds = new PoolingDataSource();

    public PersistentProcessTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        //System.setProperty("java.naming.factory.initial", "bitronix.tm.jndi.BitronixInitialContextFactory");

        ds.setUniqueName("jdbc/testDS1");
        
        
        //NON XA CONFIGS
        ds.setClassName("org.h2.jdbcx.JdbcDataSource");
        ds.setMaxPoolSize(3);
        ds.setAllowLocalTransactions(true);
        ds.getDriverProperties().put("user", "sa");
        ds.getDriverProperties().put("password", "sasa");
        ds.getDriverProperties().put("URL", "jdbc:h2:mem:mydb");
        //XA CONFIGS
//        ds.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
//        ds.setMaxPoolSize(3);
//        ds.setAllowLocalTransactions(true);
//        ds.getDriverProperties().put("user", "sa");
//        ds.getDriverProperties().put("password", "sasa");
//        ds.getDriverProperties().put("Url", "jdbc:h2:mem:mydb");
//        ds.getDriverProperties().put("driverClassName", "org.h2.Driver");
        
        ds.init();
        
        
    }

    @After
    public void tearDown() {
        ds.close();
    }
    @Test
    public void processInstancePersistentTest() throws Exception {
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
        Environment env = EnvironmentFactory.newEnvironment();
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.jbpm.runtime");
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
        env.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager());
        // Let's create a Persistence Knowledge Session
        System.out.println(" >>> Let's create a Persistent Knowledge Session");
        final StatefulKnowledgeSession ksession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, null, env);
        int sessionId = ksession.getId();
        assertNotNull(sessionId);
        assertTrue(sessionId != 0);
        // We need to register the WorkItems and Listeners that the session will use
        MockHTWorkItemHandler mockHTWorkItemHandler = new MockHTWorkItemHandler();
        MockExternalServiceWorkItemHandler mockExternalServiceWorkItemHandler = new MockExternalServiceWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", mockHTWorkItemHandler);
        ksession.getWorkItemManager().registerWorkItemHandler("External Service Call", mockExternalServiceWorkItemHandler);
        //KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);


        // Let's create a Process Instance
        Person person = new Person("Salaboy", 29);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", person);

        //Each Command will generate an interaction
        System.out.println(">>> Let's Create Process Instance");
        ProcessInstance processInstance = ksession.createProcessInstance("com.salaboy.process.AsyncInteractions", params);
        System.out.println(">>> Let's Start the Process Instance");
        long processInstanceOne = processInstance.getId();
        ksession.startProcessInstance(processInstanceOne);


        // We need to dispose the session, because the reference to this ksession object will no longer be valid
        //  because another thread could load the same session and execute a different command.
        System.out.println(">>> Disposing Session");
        ksession.dispose();

        // Let's reload the session and create a different process instance inside it
        StatefulKnowledgeSession loadedKsession = JPAKnowledgeService.loadStatefulKnowledgeSession(sessionId, kbase, null, env);
        // All the listeners and WorkItemHandlers are volatile, so we need to register them each time that we reload the session
        //  from the DB.
        loadedKsession.getWorkItemManager().registerWorkItemHandler("Human Task", mockHTWorkItemHandler);
        loadedKsession.getWorkItemManager().registerWorkItemHandler("External Service Call", mockExternalServiceWorkItemHandler);
        //KnowledgeRuntimeLoggerFactory.newConsoleLogger(loadedKsession);

        //Let's create another instance and start it.
        System.out.println(">>> Let's Create Process Instance");
        processInstance = loadedKsession.createProcessInstance("com.salaboy.process.AsyncInteractions", params);
        System.out.println(">>> Let's Start the Process Instance");
        long processInstanceTwo = processInstance.getId();
        loadedKsession.startProcessInstance(processInstanceTwo);
        System.out.println(">>> Disposing Session");
        loadedKsession.dispose();
        
        StatefulKnowledgeSession checkKsession = JPAKnowledgeService.loadStatefulKnowledgeSession(sessionId, kbase, null, env);
        assertEquals(0, checkKsession.getProcessInstances().size());
        checkKsession.dispose();
        
        
        
    }
    
    @Test
    public void processInstancePersistentAsyncTest() throws Exception {
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
        Environment env = EnvironmentFactory.newEnvironment();
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.jbpm.runtime");
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
        env.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager());
        // Let's create a Persistence Knowledge Session
        System.out.println(" >>> Let's create a Persistent Knowledge Session");
        final StatefulKnowledgeSession ksession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, null, env);
        int sessionId = ksession.getId();
        assertNotNull(sessionId);
        assertTrue(sessionId != 0);
        // We need to register the WorkItems and Listeners that the session will use
        MockAsyncHTWorkItemHandler mockAsyncHTWorkItemHandler = new MockAsyncHTWorkItemHandler();
        MockExternalServiceWorkItemHandler mockExternalServiceWorkItemHandler = new MockExternalServiceWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", mockAsyncHTWorkItemHandler);
        ksession.getWorkItemManager().registerWorkItemHandler("External Service Call", mockExternalServiceWorkItemHandler);
        //KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);


        // Let's create a Process Instance
        Person person = new Person("Salaboy", 29);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", person);

        //Each Command will generate an interaction
        System.out.println(" >>> Let's Create Process Instance");
        ProcessInstance processInstance = ksession.createProcessInstance("com.salaboy.process.AsyncInteractions", params);
        System.out.println(" >>> Let's Start the Process Instance");
        ksession.startProcessInstance(processInstance.getId());

        //Now we need to manually complete the Human Interaction to continue the process
        System.out.println(">>> Completing the first Human Interaction");
        ksession.getWorkItemManager().completeWorkItem(mockAsyncHTWorkItemHandler.getId(), null);

        // It will execute the automatic external interaction and then another Human Interaction will need to be completed
        System.out.println(">>> Completing the second Human Interaction");
        ksession.getWorkItemManager().completeWorkItem(mockAsyncHTWorkItemHandler.getId(), null);

        // We need to dispose the session, because the reference to this ksession object will no longer be valid
        //  because another thread could load the same session and execute a different command.
        System.out.println(">>> Disposing Session");
        ksession.dispose();

    }
    
    @Test
    public void processInstancesPersistenceFaultTest() throws Exception {
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
        Environment env = EnvironmentFactory.newEnvironment();
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.jbpm.runtime");
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
        env.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager());
        // Let's create a Persistence Knowledge Session
        System.out.println(" >>> Let's create a Persistent Knowledge Session");
        final StatefulKnowledgeSession ksession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, null, env);
        int sessionId = ksession.getId();
        assertNotNull(sessionId);
        assertTrue(sessionId != 0);
        // We need to register the WorkItems and Listeners that the session will use
        MockHTWorkItemHandler mockHTWorkItemHandler = new MockHTWorkItemHandler();
        MockFaultWorkItemHandler mockFaultWorkItemHandler = new MockFaultWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", mockHTWorkItemHandler);
        ksession.getWorkItemManager().registerWorkItemHandler("External Service Call", mockFaultWorkItemHandler);
        //KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);


        // Let's create a Process Instance
        Person person = new Person("Salaboy", 29);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", person);

        //Each Command will generate an interaction
        System.out.println(">>> Let's Create Process Instance");
        ProcessInstance processInstance = ksession.createProcessInstance("com.salaboy.process.AsyncInteractions", params);
        System.out.println(">>> Let's Start the Process Instance");
        try {
            ksession.startProcessInstance(processInstance.getId());
        } catch (Exception e) {
            assertTrue(e instanceof WorkflowRuntimeException);
            System.out.println(">>> Disposing Session");
            // We need to dispose the session, because the reference to this ksession object will no longer be valid
        //  because another thread could load the same session and execute a different command.
            ksession.dispose();
            
            // The startProcess transaction never gets commited.
            EntityManager em = emf.createEntityManager();
            // The ProcessInstance is in the same state as when it was created
            List resultList = em.createQuery("select p from ProcessInstanceInfo p").getResultList();
            assertEquals(1, resultList.size());
            assertEquals(0, ((WorkflowProcessInstanceImpl) processInstance).getNodeInstances().size());
            // No WorkItemInfo was commited.
            resultList = em.createQuery("select w from WorkItemInfo w").getResultList();
            assertEquals(0, resultList.size());


        }

    }
    
    @Test
    public void processInstancesPersistenceFaultRecoveryTest() throws Exception {
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
        Environment env = EnvironmentFactory.newEnvironment();
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.jbpm.runtime");
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
        env.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager());
        // Let's create a Persistence Knowledge Session
        System.out.println(" >>> Let's create a Persistent Knowledge Session");
        final StatefulKnowledgeSession ksession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, null, env);
        int sessionId = ksession.getId();
        assertNotNull(sessionId);
        assertTrue(sessionId != 0);
        // We need to register the WorkItems and Listeners that the session will use
        MockAsyncHTWorkItemHandler mockAsyncHTWorkItemHandler = new MockAsyncHTWorkItemHandler();
        MockFaultWorkItemHandler mockFaultWorkItemHandler = new MockFaultWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", mockAsyncHTWorkItemHandler);
        ksession.getWorkItemManager().registerWorkItemHandler("External Service Call", mockFaultWorkItemHandler);
        //KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);


        // Let's create a Process Instance
        Person person = new Person("Salaboy", 29);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", person);

        //Each Command will generate an interaction
        System.out.println(">>> Let's Create Process Instance");
        ProcessInstance processInstance = ksession.createProcessInstance("com.salaboy.process.AsyncInteractions", params);
        System.out.println(">>> Let's Start the Process Instance");
        try {
            ksession.startProcessInstance(processInstance.getId());
            System.out.println(">>> Completing the first Human Interaction");
            ksession.getWorkItemManager().completeWorkItem(mockAsyncHTWorkItemHandler.getId(), null);
            
        } catch (Exception e) {
            assertTrue(e instanceof WorkflowRuntimeException);
            System.out.println(">>> Disposing Session");
            // We need to dispose the session, because the reference to this ksession object will no longer be valid
        //  because another thread could load the same session and execute a different command.
            ksession.dispose();
            
            // The startProcess transaction never gets commited.
            EntityManager em = emf.createEntityManager();
            // The ProcessInstance is in the same state as when it was created
            List resultList = em.createQuery("select p from ProcessInstanceInfo p").getResultList();
            assertEquals(1, resultList.size());
            assertEquals(0, ((WorkflowProcessInstanceImpl) processInstance).getNodeInstances().size());
            // No WorkItemInfo was commited.
            resultList = em.createQuery("select w from WorkItemInfo w").getResultList();
            assertEquals(1, resultList.size());


        }

    }
    
    
    

    @Test
    public void processInstancesAndLocalHTTest() throws Exception {
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
        Environment env = EnvironmentFactory.newEnvironment();
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.jbpm.runtime.ht");
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
        env.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager());



        // Let's create a Persistence Knowledge Session
        System.out.println(" >>> Let's create a Persistent Knowledge Session");
        final StatefulKnowledgeSession ksession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, null, env);
        
        
        
        int sessionId = ksession.getId();
        assertNotNull(sessionId);
        assertTrue(sessionId != 0);
        // We need to register the WorkItems and Listeners that the session will use
        TaskService client = createTaskService(emf);
        LocalHTWorkItemHandler localHTWorkItemHandler = new LocalHTWorkItemHandler(client, ksession);
        MockExternalServiceWorkItemHandler mockExternalServiceWorkItemHandler = new MockExternalServiceWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", localHTWorkItemHandler);
        ksession.getWorkItemManager().registerWorkItemHandler("External Service Call", mockExternalServiceWorkItemHandler);
        //KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);


        // Let's create a Process Instance
        Person person = new Person("Salaboy", 29);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", person);

        //Each Command will generate an interaction
        System.out.println(">>> Let's Create Process Instance");
        ProcessInstance processInstance = ksession.createProcessInstance("com.salaboy.process.AsyncInteractions", params);
        System.out.println(">>> Let's Start the Process Instance");

        processInstance = ksession.startProcessInstance(processInstance.getId());
        System.out.println(" >>> Looking for Salaboy's Tasks");
        List<TaskSummary> salaboysTasks = client.getTasksAssignedAsPotentialOwner("salaboy", "en-UK");
        assertTrue(salaboysTasks.size() == 1);
        
        TaskSummary salaboyTask = salaboysTasks.get(0);
        System.out.println(" >>> Starting Salaboy's Task");
        client.start(salaboyTask.getId(), "salaboy");
        
        
        
        System.out.println(" >>> Completing Salaboy's Task");
        client.complete(salaboyTask.getId(), "salaboy", null);
        
        List<TaskSummary> adminTasks = client.getTasksAssignedAsPotentialOwner("Administrator", "en-UK");
        assertTrue(adminTasks.size() == 1);
        TaskSummary adminTask = adminTasks.get(0);
        
        client.start(adminTask.getId(), "Administrator");
        
        client.complete(adminTask.getId(), "Administrator", null);

        processInstance = ksession.getProcessInstance(processInstance.getId());
        //The process instance was completed and it should be out of the Ksession.
        assertNull(processInstance);

        // We need to dispose the session, because the reference to this ksession object will no longer be valid
        // Another thread could load the same session and execute a different command.
        System.out.println(">>> Disposing Session");
        ksession.dispose();
    }
    
    @Test
    public void processInstancesAndLocalHTPlusFailTest() throws Exception {
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
        Environment env = EnvironmentFactory.newEnvironment();
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.jbpm.runtime.ht");
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
        env.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager());



        // Let's create a Persistence Knowledge Session
        System.out.println(" >>> Let's create a Persistent Knowledge Session");
        final StatefulKnowledgeSession ksession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, null, env);
        int sessionId = ksession.getId();
        assertNotNull(sessionId);
        assertTrue(sessionId != 0);
        // We need to register the WorkItems and Listeners that the session will use
        TaskService client = createTaskService(emf);
        LocalHTWorkItemHandler localHTWorkItemHandler = new LocalHTWorkItemHandler(client, ksession);
        MockFaultWorkItemHandler mockFaultWorkItemHandler = new MockFaultWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", localHTWorkItemHandler);
        ksession.getWorkItemManager().registerWorkItemHandler("External Service Call", mockFaultWorkItemHandler);
        //KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);


        // Let's create a Process Instance
        Person person = new Person("Salaboy", 29);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", person);

        //Each Command will generate an interaction
        System.out.println(">>> Let's Create Process Instance");
        ProcessInstance processInstance = ksession.createProcessInstance("com.salaboy.process.AsyncInteractions", params);
        System.out.println(">>> Let's Start the Process Instance");
        UserTransaction ut =
        (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
        
        ut.begin();
        ksession.startProcessInstance(processInstance.getId());
        ut.commit();
        
        System.out.println(" >>> Looking for Salaboy's Tasks");
        List<TaskSummary> salaboysTasks = client.getTasksAssignedAsPotentialOwner("salaboy", "en-UK");
        assertTrue(salaboysTasks.size() == 1);
        TaskSummary salaboyTask = salaboysTasks.get(0);
        System.out.println(" >>> Starting Salaboy's Task");
        client.start(salaboyTask.getId(), "salaboy");
        System.out.println(" >>> Completing Salaboy's Task");

        
        ut.begin();
        try{
            client.complete(salaboyTask.getId(), "salaboy", null);
            ut.commit();
        } catch(WorkflowRuntimeException e){
            System.out.println("Rolling back transaction" + e);
            ut.rollback();
        }


        System.out.println(" >>> Looking for Salaboy's Tasks");
        Task salaboysTask = client.getTask(salaboyTask.getId());
        assertNotNull(salaboysTask);

        assertEquals("InProgress", salaboysTask.getTaskData().getStatus().name());
        
        // We need to dispose the session, because the reference to this ksession object will no longer be valid
        //  because another thread could load the same session and execute a different command.
        System.out.println(" >>> Disposing Session");
        ksession.dispose();
    }

    private TaskService createTaskService(EntityManagerFactory emf) {
        org.jbpm.task.service.TaskService taskService = new org.jbpm.task.service.TaskService(emf, SystemEventListenerFactory.getSystemEventListener());
        Map<String, User> users = new HashMap<String, User>();
        users.put("salaboy", new User("salaboy"));
        users.put("Administrator", new User("Administrator"));

        Map<String, Group> groups = new HashMap<String, Group>();
        taskService.addUsersAndGroups(users, groups);
        TaskService client = new LocalTaskService(taskService);

        return client;
    }

    private class MockHTWorkItemHandler implements WorkItemHandler {

        public void executeWorkItem(WorkItem wi, WorkItemManager wim) {
            System.out.println(">>> Completing a Human Interaction");
            wim.completeWorkItem(wi.getId(), null);
        }

        public void abortWorkItem(WorkItem wi, WorkItemManager wim) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private class MockAsyncHTWorkItemHandler implements WorkItemHandler {

        private long id;

        public void executeWorkItem(WorkItem wi, WorkItemManager wim) {
            System.out.println(">>> Working on a Human Interaction");
            this.id = wi.getId();
        }

        public void abortWorkItem(WorkItem wi, WorkItemManager wim) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public long getId() {
            return id;
        }
    }

    private class MockFaultWorkItemHandler implements WorkItemHandler {

        public void executeWorkItem(WorkItem wi, WorkItemManager wim) {
            System.out.println(">>> External Interaction Fault!");
            throw new IllegalStateException(" An External Fault Arise, the Service Cannot be Contacted!");
        }

        public void abortWorkItem(WorkItem wi, WorkItemManager wim) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private class MockExternalServiceWorkItemHandler implements WorkItemHandler {

        public void executeWorkItem(WorkItem wi, WorkItemManager wim) {
            System.out.println(">>> Completing an External Interaction");
            wim.completeWorkItem(wi.getId(), null);
        }

        public void abortWorkItem(WorkItem wi, WorkItemManager wim) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
