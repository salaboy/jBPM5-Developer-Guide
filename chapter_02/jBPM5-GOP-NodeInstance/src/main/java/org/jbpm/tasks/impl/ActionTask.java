/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jbpm.tasks.impl;

import org.jbpm.api.Action;

/**
 *
 * @author salaboy
 */
public class ActionTask extends AbstractBaseTask {
    private Action action;

    public ActionTask(Action action) {
        this.action = action;
    }

    public Action getAction() {
        return action;
    }
    
    
}
