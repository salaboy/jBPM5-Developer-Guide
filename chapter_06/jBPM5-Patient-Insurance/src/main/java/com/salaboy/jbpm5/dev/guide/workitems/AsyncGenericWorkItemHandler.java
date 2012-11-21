package com.salaboy.jbpm5.dev.guide.workitems;

import java.util.Map;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;
import org.jbpm.executor.ExecutorServiceEntryPoint;
import org.jbpm.executor.api.CommandContext;

/**
 * Work Item Handler that uses an Executor Service to deal with external 
 * interactions.
 * @author esteban
 */
public class AsyncGenericWorkItemHandler implements WorkItemHandler {
	
	private final ExecutorServiceEntryPoint executor;
	private String execKey;
        private int sessionId;
        
	public AsyncGenericWorkItemHandler(ExecutorServiceEntryPoint executor, int sessionId) {
		this.executor = executor;
                this.sessionId = sessionId;
	}
	
        /**
         * This handler expects 3 input parameters: 
         *  command: the command to be scheduled using Executor Service component.
         *  callbacks: the callbacks that should be executed by Executor Service
         * after the command is performed.
         *  waitTillComplete: Should the work item be completed or not right 
         * after the command is scheduled? In the case it should wait, the 
         * completion of the work item should be delegated to the executor service
         * by using a command or callback that completes it.
         * @param workItem
         * @param manager 
         */
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		long workItemId = workItem.getId();
		String command = (String) workItem.getParameter("command");
                String callbacks = (String) workItem.getParameter("callbacks");
		this.execKey = workItem.getName() + "_" + workItem.getProcessInstanceId() + "_" + workItemId + "@sessionId="+this.sessionId;
		CommandContext ctx = new CommandContext();
		for (Map.Entry<String, Object> entry : workItem.getParameters().entrySet()) {
			if (entry.getValue() instanceof Object) {
				ctx.setData(entry.getKey(), entry.getValue());
			}
		}
		ctx.setData("_workItemId", String.valueOf(workItemId));
                ctx.setData("callbacks", callbacks);
                ctx.setData("businessKey", this.execKey);     
                Long requestId = this.executor.scheduleRequest(command, ctx);
                workItem.getParameters().put("requestId", requestId);
		String sWaitTillComplete = (String) workItem.getParameter("waitTillComplete");
		Boolean waitTillComplete = sWaitTillComplete == null ? null : Boolean.valueOf(sWaitTillComplete);
		if (waitTillComplete == null || !waitTillComplete.booleanValue()) {
			manager.completeWorkItem(workItemId, workItem.getResults());
		}
		
	}

	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
                Long requestId = (Long) workItem.getParameter("requestId");    
                executor.cancelRequest(requestId);
		String sWaitTillComplete = (String) workItem.getParameter("waitTillComplete");
		Boolean waitTillComplete = sWaitTillComplete == null ? null : Boolean.valueOf(sWaitTillComplete);
		if (waitTillComplete == null || !waitTillComplete.booleanValue()) {
			manager.abortWorkItem(workItem.getId());
		}
		
	}
	
	public ExecutorServiceEntryPoint getExecutor() {
		return executor;
	}
	
	
}
