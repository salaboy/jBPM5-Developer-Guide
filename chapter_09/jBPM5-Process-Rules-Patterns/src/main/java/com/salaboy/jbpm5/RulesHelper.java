/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5;

import com.salaboy.model.MarketMetric;
import com.salaboy.model.Car;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.impl.ClassPathResource;
import org.drools.runtime.StatelessKnowledgeSession;

/**
 *
 * @author salaboy
 */
public class RulesHelper {
    public static Map<String, Boolean> cachedResults = new HashMap<String, Boolean>();
    public static boolean evaluate(Car car){
        
        if(cachedResults.containsKey(car.getName())){
            return cachedResults.get(car.getName());
        }
        
        StatelessKnowledgeSession session = createStatelessSession("good_old_patterns/car-sell-or-drop-complex-decision.drl");
        List list = new ArrayList();
        list.add(car);
        // Get external information that you don't want to keep in the process, for example:
        //   - Call an external service to get information about the stock market
        //   - Check the average price for this type of cars in ebay
        //   - Check for the competitors prices in the last two months
        // We can get a metric about the current status of the market
        // when the process is being executed and use that to influence our decision
        MarketMetric metric = new MarketMetric(0.8);
        list.add(metric);
        
        session.execute(list);
        cachedResults.put(car.getName(), metric.getResult());
        return metric.getResult();
        
    }
    
    private static StatelessKnowledgeSession createStatelessSession(String rules){
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(new ClassPathResource(rules), ResourceType.DRL);
        
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
