package com.salaboy.jbpm5.dev.guide.workitems;

import java.util.Map;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;
import org.jbpm.executor.ExecutorServiceEntryPoint;
import org.jbpm.executor.api.CommandContext;


public class AsyncGenericWorkItemHandler implements WorkItemHandler {
	
	private final ExecutorServiceEntryPoint executor;
	private String execKey;
        private int sessionId;
        
	public AsyncGenericWorkItemHandler(ExecutorServiceEntryPoint executor, int sessionId) {
		this.executor = executor;
                this.sessionId = sessionId;
	}
	
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
