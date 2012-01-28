/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.salaboy.jbpm5.dev.guide;

import com.salaboy.jbpm5.dev.guide.executor.entities.RequestInfo;
import com.salaboy.jbpm5.dev.guide.executor.entities.STATUS;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author salaboy
 */
public class PersistenceTest {

    private static EntityManagerFactory emf;
    private EntityManager em;
    public PersistenceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
         emf = Persistence.createEntityManagerFactory("org.jbpm.executor");
         
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        emf.close();
    }

    @Before
    public void setUp() {
        em = emf.createEntityManager();
    }

    @After
    public void tearDown() {
        em.close();
    }

    @Test
    public void persistenceSimple() {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setKey("HI");
        requestInfo.setStatus(STATUS.QUEUED);
        requestInfo.setMessage("Ready to execute");
        em.getTransaction().begin();
        em.persist(requestInfo);
        em.getTransaction().commit();
        List<?> resultList = em.createQuery("Select r from RequestInfo as r").getResultList();
        assertEquals(1, resultList.size());
    }
    
    
}
