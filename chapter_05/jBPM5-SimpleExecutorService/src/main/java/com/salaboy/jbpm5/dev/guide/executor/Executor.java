
package com.salaboy.jbpm5.dev.guide.executor;


/**
 *
 * @author salaboy
 */
public interface Executor extends Service{
    public void schedule(String requestName, String key, CommandContext ctx);
    public void unschedule(String key);
}
