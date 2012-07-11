/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.dev.guide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.drools.SystemEventListenerFactory;
import org.jbpm.task.*;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.ContentData;
import org.jbpm.task.service.PermissionDeniedException;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.TaskServiceSession;
import org.jbpm.task.service.local.LocalTaskService;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author salaboy
 */
public class HumanTasksLifecycleAPITest {

    private EntityManagerFactory emf;
    private TaskService taskService;
    private TaskServiceSession taskSession;
    private Map<String, User> users = new HashMap<String, User>();
    private Map<String, Group> groups = new HashMap<String, Group>();

    public HumanTasksLifecycleAPITest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        // Create an EntityManagerFactory based on the PU configuration
        emf = Persistence.createEntityManagerFactory("org.jbpm.task");
        // The Task Service will use the EMF to store our Task Status
        taskService = new TaskService(emf, SystemEventListenerFactory.getSystemEventListener());
        // We can uset the Task Service to get an instance of the Task Session which
        // allows us to introduce to our database the users and groups information before 
        // running our tests
        taskSession = taskService.createSession();
        // Adds 1 Administrator, 2 users and 1 Group
        addUsersAndGroups(taskSession);

        //We need to set up an user to represent the user that is making the requests
        MockUserInfo userInfo = new MockUserInfo();
        taskService.setUserinfo(userInfo);
    }

    @After
    public void tearDown() {
        taskSession.dispose();
        emf.close();
    }

    @Test
    public void regularFlowTest() {


        // Create a local instance of the TaskService
        LocalTaskService localTaskService = new LocalTaskService(taskService);
       
        List<User> potentialOwners = new ArrayList<User>();
        potentialOwners.add(users.get("salaboy"));
        // Create a Task Definition 
        Task task = createSimpleTask(potentialOwners, users.get("administrator"));

        // Deploy the Task Definition to the Task Component
        localTaskService.addTask(task, new ContentData());

        // Because the Task contains a direct assignment we can query it for its Potential Owner
        // Notice that we obtain a list of TaskSummary (a lightweight representation of a task)
        List<TaskSummary> tasksAssignedAsPotentialOwner = localTaskService.getTasksAssignedAsPotentialOwner("salaboy", "en-UK");

        // We know that there is just one task available so we get the first one
        Long taskId = tasksAssignedAsPotentialOwner.get(0).getId();

        // In order to check the task status we need to get the real task
        // The task is in a Reserved status because it already have a well-defined Potential Owner
        Task simpleTask = localTaskService.getTask(taskId);
        assertEquals(Status.Reserved, simpleTask.getTaskData().getStatus());

        // In order start working with this task we call the start() method
        localTaskService.start(simpleTask.getId(), "salaboy");

        // The task is now In Progress
        simpleTask = localTaskService.getTask(taskId);
        assertEquals(Status.InProgress, simpleTask.getTaskData().getStatus());

        // PERFORM THE TASK ACTIVITY HERE, the user need to perform the required activities here

        // Once the Task activity is performed the user can complete the task,
        // Notice that we are completing this task without results
        localTaskService.complete(simpleTask.getId(), "salaboy", null);

        // We can check the task status after completion
        simpleTask = localTaskService.getTask(taskId);
        assertEquals(Status.Completed, simpleTask.getTaskData().getStatus());


    }

    @Test
    public void claimConflictAndRetry() {


        // Create a local instance of the TaskService
        LocalTaskService localTaskService = new LocalTaskService(taskService);
        List<User> potentialOwners = new ArrayList<User>();
        potentialOwners.add(users.get("salaboy"));
        potentialOwners.add(users.get("watman"));
        // Create a Task Definition 
        Task task = createSimpleTask(potentialOwners, users.get("administrator"));

        // Deploy the Task Definition to the Task Component
        localTaskService.addTask(task, new ContentData());

        // Because the Task contains a direct assignment we can query it for its Potential Owner
        // Notice that we obtain a list of TaskSummary (a lightweight representation of a task)
        List<TaskSummary> salaboyTasks = localTaskService.getTasksAssignedAsPotentialOwner("salaboy", "en-UK");

        // We know that there is just one task available so we get the first one
        Long salaboyTaskId = salaboyTasks.get(0).getId();

        // In order to check the task status we need to get the real task
        // The task is in a Reserved status because it already have a well-defined Potential Owner
        Task salaboyTask = localTaskService.getTask(salaboyTaskId);
        assertEquals(Status.Ready, salaboyTask.getTaskData().getStatus());

        // Because the Task contains a direct assignment we can query it for its Potential Owner
        // Notice that we obtain a list of TaskSummary (a lightweight representation of a task)
        List<TaskSummary> watmanTasks = localTaskService.getTasksAssignedAsPotentialOwner("watman", "en-UK");

        // We know that there is just one task available so we get the first one
        Long watmanTaskId = watmanTasks.get(0).getId();
        assertEquals(watmanTaskId, salaboyTaskId);
        // In order to check the task status we need to get the real task
        // The task is in a Reserved status because it already have a well-defined Potential Owner
        Task watmanTask = localTaskService.getTask(watmanTaskId);
        assertEquals(Status.Ready, watmanTask.getTaskData().getStatus());

        
        localTaskService.claim(watmanTask.getId(), "watman");
        
        try{
            localTaskService.claim(salaboyTask.getId(), "salaboy");
        } catch(PermissionDeniedException ex){
            // The Task is gone.. salaboy needs to retry
            assertNotNull(ex);
        }
        
        
        
        



    }

    @Test
    public void claimNextAvailable() {


        // Create a local instance of the TaskService
        LocalTaskService localTaskService = new LocalTaskService(taskService);
        List<User> potentialOwners = new ArrayList<User>();
        potentialOwners.add(users.get("salaboy"));
        potentialOwners.add(users.get("watman"));
        // Create a Task Definition 
        Task task = createSimpleTask(potentialOwners, users.get("administrator"));

        // Deploy the Task Definition to the Task Component
        localTaskService.addTask(task, new ContentData());

        // we don't need to query for our task to see what we will claim, just claim the next one available for us
  
        localTaskService.claimNextAvailable("watman", "en-UK");
        
        List<Status> status = new ArrayList<Status>();
        status.add(Status.Ready);
        List<TaskSummary> salaboyTasks = localTaskService.getTasksAssignedAsPotentialOwnerByStatus("salaboy",status,  "en-UK");
        assertEquals(0, salaboyTasks.size());
        
        
        



    }
    private void addUsersAndGroups(TaskServiceSession taskSession) {
        User user = new User("salaboy");
        User watman = new User("watman");
        taskSession.addUser(user);
        taskSession.addUser(watman);
        User administrator = new User("Administrator");
        taskSession.addUser(administrator);
        users.put("salaboy", user);
        users.put("watman", watman);
        users.put("administrator", administrator);
        Group myGroup = new Group("group1");
        taskSession.addGroup(myGroup);
        groups.put("group1", myGroup);

    }

    private Task createSimpleTask(List<User> users, User administrator) {
        Task task = new Task();
        task.setPriority(1);
        TaskData data = new TaskData();
        data.setWorkItemId(1);
        PeopleAssignments peopleAssignments = new PeopleAssignments();
        List<OrganizationalEntity> usersEntities = new ArrayList<OrganizationalEntity>();
        for (User user : users) {
            usersEntities.add(user);
        }
        List<OrganizationalEntity> adminsEntities = new ArrayList<OrganizationalEntity>();
        adminsEntities.add(administrator);
        peopleAssignments.setBusinessAdministrators(adminsEntities);
        peopleAssignments.setPotentialOwners(usersEntities);
        task.setPeopleAssignments(peopleAssignments);
        List<I18NText> names = new ArrayList<I18NText>();
        names.add(new I18NText("en-UK", "My Simple Task"));
        task.setNames(names);
        task.setDescriptions(names);
        task.setSubjects(names);
        data.setProcessInstanceId(1);
        data.setProcessSessionId(1);
        task.setTaskData(data);
        return task;
    }
}
