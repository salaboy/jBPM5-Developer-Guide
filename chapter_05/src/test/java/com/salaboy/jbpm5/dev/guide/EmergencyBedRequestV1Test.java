package com.salaboy.jbpm5.dev.guide;

import com.salaboy.jbpm5.dev.guide.workitems.MockWorkItemHandler;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import junit.framework.Assert;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.process.ProcessInstance;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author esteban
 */
public class EmergencyBedRequestV1Test extends EmergencyBedRequestBaseTest{

    private MockWorkItemHandler mockWorkItemHandler;
    
    @Override
    protected Map<Resource, ResourceType> getResources() {
        Map<Resource, ResourceType> resources = new HashMap<Resource, ResourceType>();
        resources.put(ResourceFactory.newClassPathResource("V1/EmergencyBedRequestV1.bpmn"), ResourceType.BPMN2);
        resources.put(ResourceFactory.newClassPathResource("V1/bedAssignment.drl"), ResourceType.DRL);
        
        return resources;
    }
    
    @Before
    public void setupWorkItemHandlers(){
        
        mockWorkItemHandler = new MockWorkItemHandler();
        
        //register the same handler for all the Work Items present in the process.
        this.session.getWorkItemManager().registerWorkItemHandler("User Task", mockWorkItemHandler);
        this.session.getWorkItemManager().registerWorkItemHandler("Notification System", mockWorkItemHandler);
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
        ProcessInstance startProcess = session.startProcess("hospitalEmergencyV1", inputVariables);
        
        //The process must be in the 'Coordinate Staff' task. Let's check the
        //input parameters received by the handler associated to that task.
        Assert.assertEquals(date, mockWorkItemHandler.getInputParameter("bedrequest_date"));
        Assert.assertEquals(entity, mockWorkItemHandler.getInputParameter("bedrequest_entity"));
        Assert.assertEquals(patientAge, mockWorkItemHandler.getInputParameter("bedrequest_patientage"));
        Assert.assertEquals(patientGender, mockWorkItemHandler.getInputParameter("bedrequest_patientgender"));
        Assert.assertEquals(patientStatus, mockWorkItemHandler.getInputParameter("bedrequest_patientstatus"));
        
        //let's complete the task emulating the results of this task.
        Map<String,Object> taskResults = new HashMap<String, Object>();
        taskResults.put("checkinresults_gate", "3C");
        mockWorkItemHandler.completeWorkItem(inputVariables);
        
    }
    
    
}
