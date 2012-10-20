/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5;

import com.salaboy.model.Car;
import java.util.HashMap;
import java.util.Map;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.impl.ClassPathResource;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;

/**
 *
 * @author salaboy
 */
public class RankCarWorkItemHandler implements WorkItemHandler {

    public void executeWorkItem(WorkItem wi, WorkItemManager wim) {
        
        Car car = (Car)wi.getParameter("carInput");
        System.out.println("Ranking Car"+car);
        StatelessKnowledgeSession session = createStatelessSession();
        session.execute(car);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("carOutput", car);
        wim.completeWorkItem(wi.getId(), params);
    }

    public void abortWorkItem(WorkItem wi, WorkItemManager wim) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
    private StatelessKnowledgeSession createStatelessSession(){
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(new ClassPathResource("good_old_patterns/car-ranking-rules.drl"), ResourceType.DRL);
        
        if (kbuilder.hasErrors()) {
            for (KnowledgeBuilderError error : kbuilder.getErrors()) {
                System.out.println(">>> Error:" + error.getMessage());

            }
            throw new IllegalStateException(">>> Knowledge couldn't be parsed! ");
        }
        
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        
        return kbase.newStatelessKnowledgeSession();
    }
    
}
