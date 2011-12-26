/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.process.engine.structures;

/**
 *
 * @author salaboy
 */
public interface SequenceFlow extends Flow {
    public static final String FLOW_DEFAULT_TYPE = "DEFAULT_FLOW";
    
    public Task getFrom();

    public Task getTo();

    public String getFromType();

    public String getToType();
}
