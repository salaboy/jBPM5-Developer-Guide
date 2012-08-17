/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.process.engine.taskinstances.impl;

import com.salaboy.process.engine.structures.ProcessInstance;
import com.salaboy.process.engine.structures.Task;
import com.salaboy.process.engine.structures.NodeInstance;
import com.salaboy.process.engine.structures.impl.ProcessInstanceImpl;
import com.salaboy.process.engine.services.ProcessEventSupportService;

/**
 *
 * @author salaboy
 */
public class EndTaskNodeInstance extends AbstractNodeInstance {

    public EndTaskNodeInstance(ProcessInstance pI, Task task) {
        super(pI, task);
    }


    @Override
    public void internalTrigger(NodeInstance from, String type) {
        this.processInstance.setStatus(ProcessInstanceImpl.STATUS.ENDED);
        System.out.println("YOU REACH THE END OF THE PROCESS");
    }

   
    
}
