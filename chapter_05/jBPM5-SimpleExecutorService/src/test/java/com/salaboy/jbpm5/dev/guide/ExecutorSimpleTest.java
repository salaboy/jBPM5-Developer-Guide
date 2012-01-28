/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.salaboy.jbpm5.dev.guide;

import com.salaboy.jbpm5.dev.guide.executor.Executor;
import com.salaboy.jbpm5.dev.guide.executor.ExecutorFactoryBean;

import org.junit.*;

/**
 *
 * @author salaboy
 */
public class ExecutorSimpleTest {
    private Executor executor;
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
    	ExecutorFactoryBean factoryBean = new ExecutorFactoryBean();
    	executor = factoryBean.getObject();
    }

    @After
    public void tearDown() {
        executor.destroy();
        executor = null;
    }

    @Test
    public void executorSimpleTest() throws InterruptedException {
        
        executor.schedule("com.salaboy.jbpm5.dev.guide.executor.commands.PrintOutCommand", "myKey", null);
        
        Thread.sleep(15000);
    }
}
