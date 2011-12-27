/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.process.engine.structures.impl;

import java.util.HashMap;
import java.util.Map;
import com.salaboy.process.engine.structures.ContextInstance;

/**
 *
 * @author salaboy
 */
public class ContextInstanceImpl implements ContextInstance {
    private Map<String, Object> variables = new HashMap<String, Object>();

    public ContextInstanceImpl() {
    }

    @Override
    public Map<String, Object> getVariables() {
        return variables;
    }

    @Override
    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public Object getVariable(String key) {
        return this.variables.get(key);
    }

    public void setVariable(String key, Object value) {
        this.variables.put(key, value);
    }
    
    
    
    
}
