package com.salaboy.jbpm5.dev.guide.executor;

import javax.persistence.EntityManager;

public class ExecutorListenerBuilder {

	private final EntityManager em;
	private final CommandDoneHandler handler;
	
	public ExecutorListenerBuilder(EntityManager em, CommandDoneHandler handler) {
		super();
		this.em = em;
		this.handler = handler;
	}

	public ExecutorListener build() {
		ExecutorListenerImpl listener = new ExecutorListenerImpl();
		listener.setEntityManager(em);
		listener.setHandler(handler);
		return listener;
	}
}
