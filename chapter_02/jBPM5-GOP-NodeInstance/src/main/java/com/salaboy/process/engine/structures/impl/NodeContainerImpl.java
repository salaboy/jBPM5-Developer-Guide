/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.process.engine.structures.impl;

import java.util.ArrayList;
import java.util.List;
import com.salaboy.process.engine.structures.Task;
import com.salaboy.process.engine.structures.NodeInstanceContainer;
import com.salaboy.process.engine.structures.NodeInstance;
import com.salaboy.process.engine.taskinstances.impl.EndTaskNodeInstance;

/**
 *
 * @author salaboy
 */
public class NodeContainerImpl implements NodeInstanceContainer {

    private List<NodeInstance> nodeInstances;

    public NodeContainerImpl() {
        this.nodeInstances = new ArrayList<NodeInstance>();
    }

    public List<NodeInstance> getNodeInstances() {
        return nodeInstances;
    }

    public void setNodeInstances(List<NodeInstance> nodeInstances) {
        this.nodeInstances = nodeInstances;
    }

    @Override
    public void addNodeInstance(NodeInstance nodeInstance) {
        if (this.nodeInstances == null) {
            this.nodeInstances = new ArrayList<NodeInstance>();
        }
        nodeInstances.add(nodeInstance);
    }

    @Override
    public void removeNodeInstance(NodeInstance nodeInstance) {
        nodeInstances.remove(nodeInstance);
    }

    @Override
    public NodeInstance getNodeInstance(Task node) {
        for (NodeInstance nodeInstance : this.nodeInstances) {
            if (nodeInstance.getTask() == node) {
                return nodeInstance;
            }
        }
        return null;

    }

    @Override
    public void nodeInstanceCompleted(NodeInstance nodeInstance, String outType) {
        if (nodeInstance instanceof EndTaskNodeInstance) {

            if (nodeInstances.isEmpty()) {
                nodeInstance.getProcessInstance().triggerCompleted();
            }

        }


    }
}
