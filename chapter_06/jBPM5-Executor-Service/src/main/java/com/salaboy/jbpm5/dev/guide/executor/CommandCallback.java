package com.salaboy.jbpm5.dev.guide.executor;

public interface CommandCallback {

    void onCommandDone(CommandContext ctx, ExecutionResults results);
}
