/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.salaboy.jbpm5.dev.guide;

import com.salaboy.jbpm5.dev.guide.executor.CommandContext;
import com.salaboy.jbpm5.dev.guide.executor.Executor;
import com.salaboy.jbpm5.dev.guide.executor.commands.PrintOutCommand;
import java.sql.SQLException;
import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Server;

import org.junit.*;
import static org.junit.Assert.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author salaboy
 */
public class ExecutorSimpleTest {

    private Executor executor;
    private Server server;
    private ApplicationContext ctx;
    public static Map<String, Object> cachedEntities = new HashMap<String, Object>();
    public ExecutorSimpleTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        DeleteDbFiles.execute("~", "mydb", false);

        try {

            server = Server.createTcpServer(new String[]{"-tcp", "-tcpAllowOthers", "-tcpDaemon", "-trace"}).start();
        } catch (SQLException ex) {
            System.out.println("ex: " + ex);
        }

        ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        executor = (Executor) ctx.getBean("executorService");
        executor.init();
    }

    @After
    public void tearDown() {
        ((ClassPathXmlApplicationContext) ctx).close();
        server.stop();
    }

    @Test
    public void requestNoCallBackTest() throws InterruptedException {
        CommandContext ctxCMD = new CommandContext();
        ctxCMD.setData("businessKey", UUID.randomUUID().toString());
        executor.scheduleRequest(PrintOutCommand.class.getCanonicalName(), ctxCMD);

        Thread.sleep(10000);

        EntityManagerFactory emf = (EntityManagerFactory) ctx.getBean("entityManagerFactory");
        EntityManager em = emf.createEntityManager();
        List resultList = em.createNamedQuery("ExecutedRequests").getResultList();

        assertEquals(1, resultList.size());
        
        em.close();

    }

    @Test
    public void executorSetupSpringTest() {
        EntityManagerFactory emf = (EntityManagerFactory) ctx.getBean("entityManagerFactory");

        assertNotNull(emf);
        EntityManager em = emf.createEntityManager();
        List resultList = em.createNamedQuery("QueuedRequestInfos").getResultList();

        assertEquals(0, resultList.size());

        em.close();
    }

    @Test
    public void executorSimpleTestWithCallback() throws InterruptedException {
        
        CommandContext commandContext = new CommandContext();
        commandContext.setData("businessKey", UUID.randomUUID().toString());
        cachedEntities.put((String)commandContext.getData("businessKey"), new Long(1));
        String callbacks = SimpleIncrementCallback.class.getCanonicalName();
        commandContext.setData("callbacks", callbacks);
        executor.scheduleRequest(PrintOutCommand.class.getCanonicalName(), commandContext);

        Thread.sleep(10000);

        EntityManagerFactory emf = (EntityManagerFactory) ctx.getBean("entityManagerFactory");
        EntityManager em = emf.createEntityManager();
        List resultList = em.createNamedQuery("ExecutedRequests").getResultList();

        assertEquals(1, resultList.size());
        assertEquals(2, ((Long)cachedEntities.get((String)commandContext.getData("businessKey"))).intValue());
        em.close();
    }
    
    @Test
    public void executorExceptionTest() throws InterruptedException {
        
        CommandContext commandContext = new CommandContext();
        commandContext.setData("businessKey", UUID.randomUUID().toString());
        cachedEntities.put((String)commandContext.getData("businessKey"), new Long(1));
        String callbacks = SimpleIncrementCallback.class.getCanonicalName();
        commandContext.setData("callbacks", callbacks);
        commandContext.setData("retries", 0);
        executor.scheduleRequest(ThrowExceptionCommand.class.getCanonicalName(), commandContext);
        System.out.println(System.currentTimeMillis()+"  >>> Sleeping for 10 secs");
        Thread.sleep(10000);
        System.out.println(System.currentTimeMillis()+" >>> Waking up from Sleeping for 10 secs");
        EntityManagerFactory emf = (EntityManagerFactory) ctx.getBean("entityManagerFactory");
        EntityManager em = emf.createEntityManager();
        List resultList = em.createNamedQuery("InErrorRequests").getResultList();

        assertEquals(1, resultList.size());
        System.out.println("Error: "+resultList.get(0));
        
        resultList = em.createNamedQuery("allErrors").getResultList();
        System.out.println(" >>> Errors: "+resultList);
        assertEquals(1, resultList.size());
        
        em.close();
    }
    
     @Test
    public void defaultRequestRetryTest() throws InterruptedException {
        CommandContext ctxCMD = new CommandContext();
        ctxCMD.setData("businessKey", UUID.randomUUID().toString());
        
        executor.scheduleRequest(ThrowExceptionCommand.class.getCanonicalName(), ctxCMD);
        
        Thread.sleep(25000);
        
        EntityManagerFactory emf = (EntityManagerFactory) ctx.getBean("entityManagerFactory");
        EntityManager em = emf.createEntityManager();
       
        List resultList = em.createNamedQuery("InErrorRequests").getResultList();

        assertEquals(1, resultList.size());
        
        resultList = em.createNamedQuery("allErrors").getResultList();
        // Three retries means 4 executions in total 1(regular) + 3(retries)
        assertEquals(4, resultList.size());
        em.close();
        
    }
}
