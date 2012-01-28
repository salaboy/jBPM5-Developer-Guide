package com.salaboy.jbpm5.dev.guide.executor;

public interface ExecutorListener {

	void setExecutionKey(String executionKey);
	void init();
	void destroy();
}
