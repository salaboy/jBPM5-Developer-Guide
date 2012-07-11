/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.salaboy.jbpm5.dev.guide;

import com.salaboy.jbpm5.dev.guide.executor.CommandContext;
import com.salaboy.jbpm5.dev.guide.executor.Executor;
import com.salaboy.jbpm5.dev.guide.executor.commands.PrintOutCommand;
import com.salaboy.jbpm5.dev.guide.executor.entities.ErrorInfo;
import com.salaboy.jbpm5.dev.guide.executor.entities.RequestInfo;
import com.salaboy.jbpm5.dev.guide.executor.entities.STATUS;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Server;
import org.hibernate.exception.ExceptionUtils;
import org.junit.*;
import static org.junit.Assert.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author salaboy
 */
public class PersistenceTest {

    private static EntityManagerFactory emf;
    private EntityManager em;
    private Server server;
    private ApplicationContext ctx;

    public PersistenceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        DeleteDbFiles.execute("~", "mydb", false);

        try {

            server = Server.createTcpServer(new String[]{"-tcp", "-tcpAllowOthers", "-tcpDaemon", "-trace"}).start();
        } catch (SQLException ex) {
            System.out.println("ex: " + ex);
        }
        ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        emf = (EntityManagerFactory) ctx.getBean("entityManagerFactory");
       
    }

    @After
    public void tearDown() {
        emf.close();
        server.stop();
    }

    @Test
    public void persistenceSimple() {
         em = emf.createEntityManager();
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setKey("HI");
        requestInfo.setStatus(STATUS.QUEUED);
        requestInfo.setMessage("Ready to execute");
        em.getTransaction().begin();
        em.persist(requestInfo);
        em.getTransaction().commit();
        List<?> resultList = em.createQuery("Select r from RequestInfo as r").getResultList();
        assertEquals(1, resultList.size());
        em.close();
    }

    @Test
    public void persistenceWithExceptionSimple() {
        em = emf.createEntityManager();
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setKey("HI");
        requestInfo.setMessage("Ready to execute");
        
        Throwable exception = new RuntimeException("Text Expcetion!!");
        System.out.println(System.currentTimeMillis() + " >>> Before - Error Found!!!" + exception.getMessage());
        em.getTransaction().begin();
        ErrorInfo errorInfo = new ErrorInfo(exception.getMessage(), ExceptionUtils.getFullStackTrace(exception.fillInStackTrace()));
        errorInfo.setRequestInfo(requestInfo);
        requestInfo.setStatus(STATUS.ERROR);

        requestInfo.getErrorInfo().add(errorInfo);

        em.persist(requestInfo);
        
        em.getTransaction().commit();
        
        System.out.println(System.currentTimeMillis() + " >>> After - Error Found!!!" + exception.getMessage());
        em.close();
    }
    
    @Test
    public void persistenceWithExceptionMulti() {
        em = emf.createEntityManager();
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setKey("HI");
        requestInfo.setMessage("Ready to execute");
        
        Throwable exception = new RuntimeException("Text Expcetion!!");
        System.out.println(System.currentTimeMillis() + " >>> Before - Error Found!!!" + exception.getMessage());
        em.getTransaction().begin();
        ErrorInfo errorInfo = new ErrorInfo(exception.getMessage(), ExceptionUtils.getFullStackTrace(exception.fillInStackTrace()));
        errorInfo.setRequestInfo(requestInfo);
        requestInfo.setStatus(STATUS.ERROR);
        ErrorInfo errorInfo2 = new ErrorInfo(exception.getMessage(), ExceptionUtils.getFullStackTrace(exception.fillInStackTrace()));
        errorInfo2.setRequestInfo(requestInfo);
        requestInfo.setStatus(STATUS.ERROR);

        requestInfo.getErrorInfo().add(errorInfo);
        requestInfo.getErrorInfo().add(errorInfo2);
        em.persist(requestInfo);
        
        em.getTransaction().commit();
        
        
        List<?> resultList = em.createQuery("Select e from ErrorInfo as e").getResultList();
        assertEquals(2, resultList.size());
        
        System.out.println(System.currentTimeMillis() + " >>> After - Error Found!!!" + exception.getMessage());
        em.close();
    }
    
    
    
      @Test
    public void cancelRequestTest() throws InterruptedException { 

        ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        Executor executor = (Executor) ctx.getBean("executorService");
        //  The executor is on purpose not started to not fight again race condition 
        // with the request cancelations.
        CommandContext ctxCMD = new CommandContext();
        ctxCMD.setData("businessKey", UUID.randomUUID().toString());
        Long requestId = executor.scheduleRequest(PrintOutCommand.class.getCanonicalName(), ctxCMD);
        // cancel the task immediately
        executor.cancelRequest(requestId);
        
        em = emf.createEntityManager();
       
        List resultList = em.createNamedQuery("CancelledRequests").getResultList();

        assertEquals(1, resultList.size());
        em.close();

        executor.destroy();

    }
    
}
