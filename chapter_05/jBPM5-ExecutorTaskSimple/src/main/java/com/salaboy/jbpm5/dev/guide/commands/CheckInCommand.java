package com.salaboy.jbpm5.dev.guide.commands;

import com.salaboy.jbpm5.dev.guide.executor.Command;
import com.salaboy.jbpm5.dev.guide.executor.CommandContext;

public class CheckInCommand implements Command {

	private static int checkInCount = 0;
	
	private String patientName = null;

	public void setContext(CommandContext ctx) {
		this.patientName = (String) ctx.getData("bedrequest_patientname");
	}
	
	public void execute() {
		System.out.println("Check In for patient " + this.patientName + " happening NOW!!!");
		checkInCount++;
	}
	
	public static int getCheckInCount() {
		return checkInCount;
	}

	public static void reset() {
		checkInCount = 0;
	}
	
}
