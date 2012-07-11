package com.salaboy.jbpm5.dev.guide.commands;

import com.salaboy.jbpm5.dev.guide.executor.Command;
import com.salaboy.jbpm5.dev.guide.executor.CommandContext;
import com.salaboy.jbpm5.dev.guide.executor.ExecutionResults;

public class CheckInCommand implements Command {

	private static int checkInCount = 0;
	
	public ExecutionResults execute(CommandContext ctx) {
		String patientName = (String) ctx.getData("bedrequest_patientname");;
		System.out.println("Check In for patient " + patientName + " happening NOW!!!");
		checkInCount++;
		return null;
	}
	
	public static int getCheckInCount() {
		return checkInCount;
	}

	public static void reset() {
		checkInCount = 0;
	}
	
}
