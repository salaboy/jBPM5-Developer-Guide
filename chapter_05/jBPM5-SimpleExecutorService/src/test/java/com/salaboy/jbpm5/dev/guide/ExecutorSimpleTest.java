/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.salaboy.jbpm5.dev.guide;

import com.salaboy.jbpm5.dev.guide.executor.Executor;
import com.salaboy.jbpm5.dev.guide.executor.ExecutorImpl;
import org.junit.*;

/**
 *
 * @author salaboy
 */
public class ExecutorSimpleTest {
    private Executor executor = new ExecutorImpl();
    public ExecutorSimpleTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        executor.init();
    }

    @After
    public void tearDown() {
        executor.destroy();
    }

    @Test
    public void executorSimpleTest() throws InterruptedException {
        
        
        executor.schedule("com.salaboy.jbpm5.dev.guide.executor.commands.PrintOutCommand");
        
        Thread.sleep(15000);
    }
}
