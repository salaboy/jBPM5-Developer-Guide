package com.salaboy.jbpm5.dev.guide.ldap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Persistence;

import junit.framework.Assert;

import org.drools.SystemEventListenerFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.impl.ClassPathResource;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.jbpm.process.workitem.wsht.GenericHTWorkItemHandler;
import org.jbpm.process.workitem.wsht.LocalHTWorkItemHandler;
import org.jbpm.task.identity.UserGroupCallbackManager;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.local.LocalTaskService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.ldap.server.ApacheDSContainer;

public class ProcessAndHumanTasksTest {

    private ApacheDSContainer container;
    private LdapQueryHelper ldapQuery;
    private LocalTaskService service;

    @Before
    public void setUp() throws Exception {
        container = new ApacheDSContainer("o=mojo", "classpath:identity-repository.ldif");
        container.setPort(9898);
        container.afterPropertiesSet();
        
        LdapContextSource cs = new LdapContextSource();
        cs.setUrl("ldap://localhost:9898/");
        cs.setBase("o=mojo");
        cs.setUserDn("uid=admin,ou=system");
        cs.setPassword("secret");
        cs.afterPropertiesSet();
        LdapTemplate ldapTemplate = new LdapTemplate(cs);
        ldapTemplate.afterPropertiesSet();
        ldapQuery = new LdapQueryHelper(ldapTemplate);

        // By Setting the jbpm.usergroup.callback property with the call
        // back class full name, task service will use this to validate the
        // user/group exists and its permissions are ok.
        System.setProperty("jbpm.usergroup.callback",
                "org.jbpm.task.identity.LDAPUserGroupCallbackImpl");
//        LdapUserGroupCallback callback = (LdapUserGroupCallback) UserGroupCallbackManager.getInstance().getCallback();
//        callback.setQuery(ldapQuery);
    }

    @Test
    public void testUsers(){
        assertEquals(true, this.ldapQuery.existsUser("calcacuervo"));
        assertEquals(false, this.ldapQuery.existsUser("calcacuervo2"));
        assertEquals(true, this.ldapQuery.existsUser("salaboy"));
        assertEquals(true, this.ldapQuery.existsUser("esteban"));
    }
    
    @Test
    public void testProcessWithHumanTasks() throws InterruptedException {

        StatefulKnowledgeSession ksession = this.initializeSession();
        
        KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
        
        GenericHTWorkItemHandler htHandler = this.createTaskHandler(ksession);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                htHandler);

        Map<String, Object> initialParams = new HashMap<String, Object>();
        initialParams.put("user_self_evaluation", "calcacuervo");
        ProcessInstance processInstance = ksession.startProcess("chapter_07_simple_review", initialParams);
        assertEquals(ProcessInstance.STATE_ACTIVE, processInstance.getState());
        // first, calcacuervo will make its own review..
        List<TaskSummary> tasks = this.service.getTasksAssignedAsPotentialOwner("calcacuervo", "en-UK");
        assertEquals(1, tasks.size());

        this.service.start(tasks.get(0).getId(), "calcacuervo");
        
        this.service.complete(tasks.get(0).getId(), "calcacuervo", null);

        

        // now, a user with role TL will see the task.. esteban is one of them
        List<TaskSummary> estebanTasks = this.service.getTasksAssignedAsPotentialOwner("esteban", "en-UK");
        Assert.assertEquals(1, estebanTasks.size());
        this.service.claim(estebanTasks.get(0).getId(), "esteban");
        this.service.start(estebanTasks.get(0).getId(), "esteban");
        this.service.complete(estebanTasks.get(0).getId(), "esteban", null);

        

        // now, a user with role HR will see the task.. mariano is one of them
        List<TaskSummary> marianoTasks = this.service.getTasksAssignedAsPotentialOwner("mariano", "en-UK");
        Assert.assertEquals(1, marianoTasks.size());
        this.service.claim(marianoTasks.get(0).getId(), "mariano");
        this.service.start(marianoTasks.get(0).getId(), "mariano");
        this.service.complete(marianoTasks.get(0).getId(), "mariano", null);

        assertEquals(ProcessInstance.STATE_COMPLETED, processInstance.getState());
        
    }

    @After
    public void tearDown() {
        container.stop();
    }

    //init the Knowledge Session
    private StatefulKnowledgeSession initializeSession() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(
                new ClassPathResource(
                "com/salaboy/jbpm5/dev/guide/controller/simple-review-process.bpmn"),
                ResourceType.BPMN2);
        if (kbuilder.hasErrors()) {
            KnowledgeBuilderErrors errors = kbuilder.getErrors();

            for (KnowledgeBuilderError error : errors) {
                System.out.println(">>> Error:" + error.getMessage());

            }
            throw new IllegalStateException(
                    ">>> Knowledge couldn't be parsed! ");
        }
        return kbuilder.newKnowledgeBase().newStatefulKnowledgeSession();
    }

    //Creates a local task service and attaches it to a human task handler
    private GenericHTWorkItemHandler createTaskHandler(StatefulKnowledgeSession ksession) {
        TaskService ts = new TaskService(
                Persistence.createEntityManagerFactory("org.jbpm.task"),
                SystemEventListenerFactory.getSystemEventListener());
        LocalTaskService taskService = new LocalTaskService(ts);
        LocalHTWorkItemHandler taskHandler = new LocalHTWorkItemHandler(
                taskService, ksession);
//        taskHandler.connect();
        this.service = taskService;
        return taskHandler;
    }
}
