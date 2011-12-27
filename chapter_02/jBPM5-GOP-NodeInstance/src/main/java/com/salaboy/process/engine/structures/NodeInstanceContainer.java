/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.process.engine.structures;

import java.util.List;

/**
 *
 * @author salaboy
 */
public interface NodeInstanceContainer {
    public List<NodeInstance> getNodeInstances();
    public void setNodeInstances(List<NodeInstance> nodeInstances);
    public void addNodeInstance(NodeInstance nodeInstance);
    public void removeNodeInstance(NodeInstance nodeInstance);
    public NodeInstance getNodeInstance(Task node);
    public void nodeInstanceCompleted(NodeInstance nodeInstance, String outType);
}
