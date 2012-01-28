package com.salaboy.jbpm5.dev.guide.executor;

public class ExecutorMain {

	public static void main(String[] args) {
		String waitTimeString = args.length > 0 ? args[0] : "5000";
		int waitTime = 5000;
		try {
			waitTime = Integer.parseInt(waitTimeString);
		} catch (NumberFormatException e) {
			waitTime = 5000;
		}
		final ExecutorImpl executor = new ExecutorImpl();
		executor.setWaitTime(waitTime);
		executor.init();

		try {
			executor.join();
		} catch (InterruptedException e) {
			//do nothing
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
		    	executor.destroy();
		    }
		});
	}
}
