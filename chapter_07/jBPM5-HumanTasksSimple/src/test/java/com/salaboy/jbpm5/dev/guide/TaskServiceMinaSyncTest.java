/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.salaboy.jbpm5.dev.guide;

import java.util.ArrayList;
import java.util.List;
import org.jbpm.task.*;
import org.jbpm.task.TaskService;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.*;

import org.jbpm.task.service.local.LocalTaskService;

import org.jbpm.task.service.mina.AsyncMinaTaskClient;
import org.jbpm.task.service.mina.MinaTaskServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TaskServiceMinaSyncTest extends BaseTest {
    protected TaskServer server;
    protected TaskService client;
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        server = new MinaTaskServer( taskService );
        Thread thread = new Thread( server );
        thread.start();
        System.out.println("Waiting for the MinaTask Server to come up");
        while (!server.isRunning()) {
        	System.out.print(".");
        	Thread.sleep( 50 );
        }
        client = new SyncTaskServiceWrapper(new AsyncMinaTaskClient());
        client.connect("127.0.0.1", 9123);
    }
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        client.disconnect();
        server.stop();
    }
    
     @Test
    public void regularFlowTest() {

       
        List<User> potentialOwners = new ArrayList<User>();
        potentialOwners.add(users.get("salaboy"));
        // Create a Task Definition 
        Task task = createSimpleTask(potentialOwners, users.get("administrator"));

        // Deploy the Task Definition to the Task Component
        client.addTask(task, new ContentData());

        // Because the Task contains a direct assignment we can query it for its Potential Owner
        // Notice that we obtain a list of TaskSummary (a lightweight representation of a task)
        List<TaskSummary> tasksAssignedAsPotentialOwner = client.getTasksAssignedAsPotentialOwner("salaboy", "en-UK");

        // We know that there is just one task available so we get the first one
        Long taskId = tasksAssignedAsPotentialOwner.get(0).getId();

        // In order to check the task status we need to get the real task
        // The task is in a Reserved status because it already have a well-defined Potential Owner
        Task simpleTask = client.getTask(taskId);
        assertEquals(Status.Reserved, simpleTask.getTaskData().getStatus());

        // In order start working with this task we call the start() method
        client.start(simpleTask.getId(), "salaboy");

        // The task is now In Progress
        simpleTask = client.getTask(taskId);
        assertEquals(Status.InProgress, simpleTask.getTaskData().getStatus());

        // PERFORM THE TASK ACTIVITY HERE, the user need to perform the required activities here

        // Once the Task activity is performed the user can complete the task,
        // Notice that we are completing this task without results
        client.complete(simpleTask.getId(), "salaboy", null);

        // We can check the task status after completion
        simpleTask = client.getTask(taskId);
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
