/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.salaboy.process.engine.structures.impl;

import com.salaboy.process.engine.factories.NodeInstanceFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import com.salaboy.process.engine.services.Service;
import com.salaboy.process.engine.structures.*;
import java.util.List;

/**
 *
 * @author salaboy
 */
public class ProcessInstanceImpl implements ProcessInstance {

    

   
    private long id;
    private ProcessDefinition process;
    private ContextInstance context;
    private NodeInstanceContainer nodeContainer;
    private STATUS status;
    
    private Map<String, Service> services = new HashMap<String, Service>();

    public ProcessInstanceImpl() {
    }

    public ProcessInstanceImpl(ProcessDefinition process) {
        this.id = new Random().nextLong();
        this.process = process;
        this.context = new ContextInstanceImpl();
        this.nodeContainer = new NodeContainerImpl();
        this.status = STATUS.CREATED;
    }

    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }


    public void setServices(Map<String, Service> services) {
        this.services = services;
    }
    
    @Override
    public ProcessDefinition getProcessDefinition() {
        return process;
    }

    @Override
    public void start() {
        // We should check that the first task inside the process.tasks is a startTask
        NodeInstance startTask = NodeInstanceFactory.newNodeInstance(this, process.getTasks().get(0L));
        this.nodeContainer.addNodeInstance(startTask);
        this.status = STATUS.ACTIVE;
        startTask.trigger(null, null);

    }
    
    public void addService(String name, Service service) {
        this.services.put(name, service);
    }
    
    public Service getService(String name){
        return this.services.get(name);
    }

    @Override
    public void setProcessDefinition(ProcessDefinition process) {
        this.process = process;
    }

    @Override
    public ContextInstance getContextInstance() {
        return context;
    }


    @Override
    public void triggerCompleted() {
        this.status = STATUS.ENDED;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }
    public STATUS getStatus() {
        return status;
    }
    
    public void start(Map<String, Object> variables) {
        this.context.setVariables(variables);
        start();
    }

    
    //From NodeContainer
    
    public List<NodeInstance> getNodeInstances() {
        return this.nodeContainer.getNodeInstances();
    }

    public void setNodeInstances(List<NodeInstance> nodeInstances) {
        this.nodeContainer.setNodeInstances(nodeInstances);
    }

    public void addNodeInstance(NodeInstance nodeInstance) {
        this.nodeContainer.addNodeInstance(nodeInstance);
    }

    public void removeNodeInstance(NodeInstance nodeInstance) {
        this.nodeContainer.removeNodeInstance(nodeInstance);
    }

    public NodeInstance getNodeInstance(Task task) {
        return this.nodeContainer.getNodeInstance(task);
    }

    public void nodeInstanceCompleted(NodeInstance nodeInstance, String outType) {
        this.nodeContainer.nodeInstanceCompleted(nodeInstance, outType);
    }
    
}
