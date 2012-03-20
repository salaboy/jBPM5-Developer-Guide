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
        executor.destroy();
        executor = null;
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


    }

    @Test
    public void executorSetupSpringTest() {
        EntityManagerFactory emf = (EntityManagerFactory) ctx.getBean("entityManagerFactory");

        assertNotNull(emf);
        EntityManager em = emf.createEntityManager();
        List resultList = em.createNamedQuery("QueuedRequestInfos").getResultList();

        assertEquals(0, resultList.size());


    }

    @Test
    public void executorSimpleTestWithCallback() throws InterruptedException {
        
        CommandContext commandContext = new CommandContext();
        commandContext.setData("businessKey", UUID.randomUUID().toString());
        cachedEntities.put((String)commandContext.getData("businessKey"), new Long(1));
        List<String> callbacks = new ArrayList<String>();
        callbacks.add(SimpleIncrementCommandDoneHandler.class.getCanonicalName());
        commandContext.setData("callbacks", callbacks);
        executor.scheduleRequest(PrintOutCommand.class.getCanonicalName(), commandContext);

        Thread.sleep(10000);

        EntityManagerFactory emf = (EntityManagerFactory) ctx.getBean("entityManagerFactory");
        EntityManager em = emf.createEntityManager();
        List resultList = em.createNamedQuery("ExecutedRequests").getResultList();

        assertEquals(1, resultList.size());
        assertEquals(2, ((Long)cachedEntities.get((String)commandContext.getData("businessKey"))).intValue());
        
    }
    
    @Test
    public void cancelRequestTest() throws InterruptedException {
        CommandContext ctxCMD = new CommandContext();
        ctxCMD.setData("businessKey", UUID.randomUUID().toString());
        Long requestId = executor.scheduleRequest(PrintOutCommand.class.getCanonicalName(), ctxCMD);
        // cancel the task immediately
        executor.cancelRequest(requestId);
        
        Thread.sleep(9000);

        EntityManagerFactory emf = (EntityManagerFactory) ctx.getBean("entityManagerFactory");
        EntityManager em = emf.createEntityManager();
        List resultList = em.createNamedQuery("ExecutedRequests").getResultList();

        assertEquals(0, resultList.size());


    }
}
