package com.salaboy.jbpm5.dev.guide.workitems;

import java.io.Serializable;
import java.util.Map;

import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;

import com.salaboy.jbpm5.dev.guide.executor.CommandContext;
import com.salaboy.jbpm5.dev.guide.executor.Executor;
import com.salaboy.jbpm5.dev.guide.executor.ExecutorListener;

public class AbstractAsyncWorkItemHandler implements WorkItemHandler {
	
	private final Executor executor;
	private final ExecutorListener listener;
	
	private String execKey;
	
	public AbstractAsyncWorkItemHandler(Executor executor, ExecutorListener listener) {
		this.executor = executor;
		this.listener = listener;
	}
	
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		long workItemId = workItem.getId();
		String command = (String) workItem.getParameter("commandClass");
		this.execKey = workItem.getName() + "_" + workItem.getProcessInstanceId() + "_" + workItemId;
		CommandContext ctx = new CommandContext();
		for (Map.Entry<String, Object> entry : workItem.getParameters().entrySet()) {
			if (entry.getValue() instanceof Serializable) {
				ctx.setData(entry.getKey(), (Serializable) entry.getValue());
			}
		}
		ctx.setData("_workItemId", String.valueOf(workItemId));
		this.executor.schedule(command, this.execKey, ctx);
		String sWaitTillComplete = (String) workItem.getParameter("waitTillComplete");
		Boolean waitTillComplete = sWaitTillComplete == null ? null : Boolean.valueOf(sWaitTillComplete);
		if (listener == null) {
			if (waitTillComplete == null || !waitTillComplete.booleanValue()) {
				manager.completeWorkItem(workItemId, workItem.getResults());
			}
		} else {
			listener.setExecutionKey(this.execKey);
			listener.init();
		} 
	}

	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		executor.unschedule(this.execKey);
		String sWaitTillComplete = (String) workItem.getParameter("waitTillComplete");
		Boolean waitTillComplete = sWaitTillComplete == null ? null : Boolean.valueOf(sWaitTillComplete);
		if (listener == null) {
			if (waitTillComplete == null || !waitTillComplete.booleanValue()) {
				manager.abortWorkItem(workItem.getId());
			}
		} else {
			listener.destroy();
		} 
	}
	
	public Executor getExecutor() {
		return executor;
	}
	
	public String getExecutionKey() {
		return execKey;
	}
}
