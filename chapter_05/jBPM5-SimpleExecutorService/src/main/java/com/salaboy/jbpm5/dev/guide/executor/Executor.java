
package com.salaboy.jbpm5.dev.guide.executor;

import org.drools.command.impl.GenericCommand;

/**
 *
 * @author salaboy
 */
public interface Executor extends Service{
    public void schedule(String requestName);
}
