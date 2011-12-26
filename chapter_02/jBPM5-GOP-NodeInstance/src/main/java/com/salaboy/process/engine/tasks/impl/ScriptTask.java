/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.process.engine.tasks.impl;

import com.salaboy.process.engine.structures.Action;

/**
 *
 * @author salaboy
 */
public class ScriptTask extends AbstractBaseTask {
    private Action action;
    private String dialect;

    public ScriptTask(String dialect, Action action) {
        this.dialect = dialect;
        this.action = action;
    }

    public Action getAction() {
        return action;
    }

    public String getDialect() {
        return dialect;
    }
    
    
    
    
}
