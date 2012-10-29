package com.salaboy.jbpm5.dev.guide;

import com.salaboy.jbpm5.dev.guide.workitems.MockWorkItemHandler;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import junit.framework.Assert;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.process.ProcessInstance;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author esteban
 */
public class EmergencyBedRequestV4Test extends EmergencyBedRequestBaseTest{

    private MockWorkItemHandler mockWorkItemHandler;
    
    @Override
    protected Map<Resource, ResourceType> getResources() {
        //return the resources used by this test.
        Map<Resource, ResourceType> resources = new HashMap<Resource, ResourceType>();
        resources.put(ResourceFactory.newClassPathResource("V4/EmergencyBedRequestV4.bpmn"), ResourceType.BPMN2);
        resources.put(ResourceFactory.newClassPathResource("V4/bedAssignmentV4.drl"), ResourceType.DRL);
        
        return resources;
    }
    
    @Before
    public void setupWorkItemHandlers(){
        
        mockWorkItemHandler = new MockWorkItemHandler();
        
        //register the same handler for all the Work Items present in the process.
        this.session.getWorkItemManager().registerWorkItemHandler("Human Task", mockWorkItemHandler);
        this.session.getWorkItemManager().registerWorkItemHandler("Notification System", mockWorkItemHandler);
        this.session.getWorkItemManager().registerWorkItemHandler("Notify Rejection to Ambulance", mockWorkItemHandler);
    }
    
    @Test
    public void doTest(){
        //prepare input parameters for the process:
        String date = DateFormat.getDateInstance().format(new Date());
        String entity = "911";
        String patientAge = "21";
        String patientGender = "F";
        String patientStatus = "Critical";
        
        Map<String, Object> inputVariables = new HashMap<String, Object>();
        inputVariables.put("bedrequest_date", date);
        inputVariables.put("bedrequest_entity", entity);
        inputVariables.put("bedrequest_patientage", patientAge);
        inputVariables.put("bedrequest_patientgender", patientGender);
        inputVariables.put("bedrequest_patientstatus", patientStatus);
        
        //Start the process using its ID and pass the input variables
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) session.startProcess("hospitalEmergencyV4", inputVariables);
        
        //**************   Coordinate Staff   **************//
        
        //The process must be in the 'Coordinate Staff' task. Let's check the
        //input parameters received by the handler associated to that task.
        Assert.assertEquals("Coordinate Staff", processInstance.getNodeInstances().iterator().next().getNodeName());
        
        Assert.assertEquals(date, mockWorkItemHandler.getInputParameter("bedrequest_date"));
        Assert.assertEquals(entity, mockWorkItemHandler.getInputParameter("bedrequest_entity"));
        Assert.assertEquals(patientAge, mockWorkItemHandler.getInputParameter("bedrequest_patientage"));
        Assert.assertEquals(patientGender, mockWorkItemHandler.getInputParameter("bedrequest_patientgender"));
        Assert.assertEquals(patientStatus, mockWorkItemHandler.getInputParameter("bedrequest_patientstatus"));
        
        //let's complete the task emulating the results of this task.
        Map<String,Object> taskResults = new HashMap<String, Object>();
        taskResults.put("checkinresults_gate", "3C");
        mockWorkItemHandler.completeWorkItem(taskResults);
        
        System.out.println("\n'Coordinate Staff' task completed \n");
        
        //**************   Notify Gate to Ambulance   **************//
        
        //Now we are at 'Notify Gate to Ambulance' task. Let's check that the input
        //parameters configured for this tasks arrived as expected.
        Assert.assertEquals("Notify Gate to Ambulance", processInstance.getNodeInstances().iterator().next().getNodeName());
        
        Assert.assertEquals("3C", mockWorkItemHandler.getInputParameter("checkinresults_gate"));
        
        //let's complete the task with a mocked resource
        taskResults = new HashMap<String, Object>();
        taskResults.put("checkinresults_notified", "true");
        mockWorkItemHandler.completeWorkItem(taskResults);
        
        System.out.println("\n'Notify Gate to Ambulance' task completed\n");
        
        //at this point, the intermediate signal event node is reached. 
        //The process is waiting in this node  for the event to happen
        Assert.assertEquals("Ambulance Arrived",processInstance.getNodeInstances().iterator().next().getNodeName());
        
        //At some point, the ambulance arrives to the gate and the process is
        //notified. (The second parameter is used when we want to send data
        //associated with the event. This is not our case)
        session.signalEvent("Ambulance Arrived", null);
        //processInstance.signalEvent("Ambulance Arrived", null);
        System.out.println("\n'Ambulance Arrived' event signaled \n");
        
        //**************   Check In Patient   **************//
        
        //In 'Check In Patient' task we are expecting a 'checkinresults_notified'
        //parameter containing the value returned by the last task
        Assert.assertEquals("Check In Patient", processInstance.getNodeInstances().iterator().next().getNodeName());
        
        Assert.assertEquals("true", mockWorkItemHandler.getInputParameter("checkinresults_notified"));
        
        //let's complete the task passing the mocked results
        taskResults = new HashMap<String, Object>();
        String checkinDate = DateFormat.getTimeInstance().format(new Date());
        taskResults.put("checkinresults_checkedin", "true");
        taskResults.put("checkinresults_time", checkinDate);
        mockWorkItemHandler.completeWorkItem(taskResults);
        
        System.out.println("\n'Check In Patient' task completed \n");
        
        //The process should be completed now. Let's check the 2 output
        //parameters of the last task: they should be mapped to process variables.
        Assert.assertEquals(ProcessInstance.STATE_COMPLETED, processInstance.getState());
        Assert.assertEquals("true", processInstance.getVariable("checkinresults_checkedin"));
        Assert.assertEquals(checkinDate, processInstance.getVariable("checkinresults_time"));
        
    }
    
    
}
