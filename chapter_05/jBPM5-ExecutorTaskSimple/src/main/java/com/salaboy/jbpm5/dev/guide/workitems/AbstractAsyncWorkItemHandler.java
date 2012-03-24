package com.salaboy.jbpm5.dev.guide.workitems;

import java.io.Serializable;
import java.util.Map;

import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;

import com.salaboy.jbpm5.dev.guide.executor.CommandContext;
import com.salaboy.jbpm5.dev.guide.executor.Executor;
import java.util.List;


public class AbstractAsyncWorkItemHandler implements WorkItemHandler {
	
	private final Executor executor;
	private String execKey;
	private List<String> callbacks;
        private int sessionId;
        
	public AbstractAsyncWorkItemHandler(Executor executor, int sessionId, List<String> callbacks) {
		this.executor = executor;
                this.callbacks = callbacks;
                this.sessionId = sessionId;
	}
	
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		
		long workItemId = workItem.getId();
		String command = (String) workItem.getParameter("commandClass");
		this.execKey = workItem.getName() + "_" + workItem.getProcessInstanceId() + "_" + workItemId + "@sessionId="+this.sessionId;
                System.out.println(" >>> Execution Key = "+this.execKey);
		CommandContext ctx = new CommandContext();
		for (Map.Entry<String, Object> entry : workItem.getParameters().entrySet()) {
			if (entry.getValue() instanceof Serializable) {
				ctx.setData(entry.getKey(), (Serializable) entry.getValue());
			}
		}
		ctx.setData("_workItemId", String.valueOf(workItemId));
                ctx.setData("callbacks", this.callbacks);
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
	
	public Executor getExecutor() {
		return executor;
	}
	
	
}
