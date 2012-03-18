package com.salaboy.jbpm5.dev.guide.workitems;

import java.io.Serializable;
import java.util.Map;

import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;

import com.salaboy.jbpm5.dev.guide.executor.CommandContext;
import com.salaboy.jbpm5.dev.guide.executor.Executor;


public class AbstractAsyncWorkItemHandler implements WorkItemHandler {
	
	private final Executor executor;
	
	
	private String execKey;
	private String callback;
        
	public AbstractAsyncWorkItemHandler(Executor executor, String execKey, String callback) {
		this.executor = executor;
		this.execKey = execKey;
                this.callback = callback;
	}
	
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		
		long workItemId = workItem.getId();
		String command = (String) workItem.getParameter("commandClass");
		//this.execKey = workItem.getName() + "_" + workItem.getProcessInstanceId() + "_" + workItemId;
		CommandContext ctx = new CommandContext();
		for (Map.Entry<String, Object> entry : workItem.getParameters().entrySet()) {
			if (entry.getValue() instanceof Serializable) {
				ctx.setData(entry.getKey(), (Serializable) entry.getValue());
			}
		}
		ctx.setData("_workItemId", String.valueOf(workItemId));
                ctx.setData("callback", callback);
		this.executor.scheduleRequest(command, this.execKey, ctx);
		String sWaitTillComplete = (String) workItem.getParameter("waitTillComplete");
		Boolean waitTillComplete = sWaitTillComplete == null ? null : Boolean.valueOf(sWaitTillComplete);
		if (waitTillComplete == null || !waitTillComplete.booleanValue()) {
			manager.completeWorkItem(workItemId, workItem.getResults());
		}
		
	}

	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		executor.cancelRequest(this.execKey);
		String sWaitTillComplete = (String) workItem.getParameter("waitTillComplete");
		Boolean waitTillComplete = sWaitTillComplete == null ? null : Boolean.valueOf(sWaitTillComplete);
		if (waitTillComplete == null || !waitTillComplete.booleanValue()) {
			manager.abortWorkItem(workItem.getId());
		}
		
	}
	
	public Executor getExecutor() {
		return executor;
	}
	
	public String getExecutionKey() {
		return execKey;
	}
}
