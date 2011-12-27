/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.process.engine.services;

import com.salaboy.process.engine.structures.NodeInstance;

/**
 *
 * @author salaboy
 */
public interface ProcessEventSupportService extends Service{
    public void fireBeforeTaskTriggered(NodeInstance node);
    public void fireAfterTaskTriggered(NodeInstance node);
    public void fireBeforeTaskLeft(NodeInstance node);
    public void fireAfterTaskLeft(NodeInstance node);
}
