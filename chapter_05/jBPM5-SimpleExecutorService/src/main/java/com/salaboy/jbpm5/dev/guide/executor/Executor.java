
package com.salaboy.jbpm5.dev.guide.executor;


/**
 *
 * @author salaboy
 */
public interface Executor extends Service{
    public Long scheduleRequest(String commandName, CommandContext ctx);
    public void cancelRequest(Long requestId);
}
