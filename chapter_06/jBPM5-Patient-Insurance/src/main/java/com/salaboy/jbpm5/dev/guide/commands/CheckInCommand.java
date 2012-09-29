package com.salaboy.jbpm5.dev.guide.commands;

import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Named;
import org.jbpm.executor.api.Command;
import org.jbpm.executor.api.CommandContext;
import org.jbpm.executor.api.ExecutionResults;

@Named
public class CheckInCommand implements Command {

	private static AtomicInteger checkInCount = new AtomicInteger();
	
	public ExecutionResults execute(CommandContext ctx) {
		String patientName = (String) ctx.getData("bedrequest_patientname");
		System.out.println("Check In for patient " + patientName + " happening NOW!!!");
		checkInCount.incrementAndGet();
		return null;
	}
	
	public static int getCheckInCount() {
		return checkInCount.get();
	}

	public static void reset() {
		checkInCount.set(0);
	}
	
}
