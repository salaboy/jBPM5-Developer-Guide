package com.salaboy.patterns;

import com.salaboy.sessions.patterns.BusinessEntity;
import java.util.Map;
import javax.persistence.EntityManager;
import org.drools.runtime.StatefulKnowledgeSession;

/**
 * Interface that provides knowledge session's related functionality required
 * by some of the rules used in this project.
 * @author esteban
 */
public interface KnowledgeSessionSupport {

    /**
     * Completes the work item handler associated with a {@link BusinessEntity}.
     * This method is typically invoked from within the rules of this test.
     * @param ksession the session to be used
     * @param em the Entity Manager to be used
     * @param entity the BuisnessEntity
     * @param results the results to use in the completion
     * @throws Exception
     */
    void completeInteraction(StatefulKnowledgeSession ksession, EntityManager em, BusinessEntity entity, Map<String, Object> results) throws Exception;

    /**
     * Same as {@link #loadKnowldgeSession(int, java.lang.String, java.lang.String, javax.persistence.EntityManager)
     * loadKnowldgeSession(id, sessionName, sessionName, em)}
     */
    StatefulKnowledgeSession loadKnowldgeSession(int id, String sessionName, EntityManager em);

    /**
     * Loads a knowledge session from the database and registers its work item
     * handlers.
     * @param id The session id.
     * @param sessionName The sessionName used to retrieve the correct kbase
     * for the session.
     * @param businessKey the business key used to register the work item
     * handlers.
     * @param em The entity manager that will be used by the registered work
     * item handlers.
     * @return
     */
    StatefulKnowledgeSession loadKnowldgeSession(int id, String sessionName, String businessKey, EntityManager em);
    
}
