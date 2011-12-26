/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.process.engine.tasks.impl;

import com.salaboy.process.engine.structures.Task;
import com.salaboy.process.engine.structures.SequenceFlow;

/**
 *
 * @author salaboy
 */
public class SequenceFlowImpl implements SequenceFlow{
    private Task from;
    private Task to;
    private String fromType;
    private String toType;

    public SequenceFlowImpl(String toType, Task to) {
        this.to = to;
        this.toType = toType;
    }
    
    @Override
    public Task getFrom() {
        return from;
    }

    @Override
    public Task getTo() {
        return to;
    }

    @Override
    public String getFromType() {
        return fromType;
    }

    @Override
    public String getToType() {
        return toType;
    }
    

    
    
    
    
}
