package com.salaboy.jbpm5.dev.guide.executor;

public interface CommandDoneHandler {

    void onCommandDone(CommandContext ctx, ExecutionResults results);
}
