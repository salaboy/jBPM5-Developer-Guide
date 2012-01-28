package com.salaboy.jbpm5.dev.guide.executor;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.springframework.beans.factory.FactoryBean;

public class ExecutorFactoryBean implements FactoryBean<Executor> {

	private int waitTime = 5000;

	private final ExecutorImpl executor = new ExecutorImpl();

	private String username;
	private String driverClass;
	private String dialectClass;
	private String password;
	private String url;
	
    private EntityManager em = null;

    public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDriverClass() {
		return driverClass;
	}

	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	public String getDialectClass() {
		return dialectClass;
	}

	public void setDialectClass(String dialectClass) {
		this.dialectClass = dialectClass;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public EntityManager createEntityManager() {
		if (em == null) {
			Map<String, String> connectionProperties = new HashMap<String, String>();
			if (driverClass != null) {
				connectionProperties.put("hibernate.connection.driver_class", driverClass);
			}
			if (dialectClass != null) {
				connectionProperties.put("hibernate.dialect", dialectClass);
			} 
			if (username != null) {
				connectionProperties.put("hibernate.connection.username", username);
			}
			if (password != null) {
				connectionProperties.put("hibernate.connection.password", password);
			}
			if (url != null) {
				connectionProperties.put("hibernate.connection.url", url);
			}
	        EntityManagerFactory emf = Persistence.createEntityManagerFactory(
	        		"org.jbpm.executor", connectionProperties);
	        em = emf.createEntityManager();
		}
        return em;
	}
	
	public ExecutorFactoryBean() {
	}
	
	public int getWaitTime() {
		return waitTime;
	}

	public void setWaitTime(int waitTime) {
		this.waitTime = waitTime;
	}

	public Executor getObject() throws Exception {
		executor.setWaitTime(waitTime);
		executor.setEntityManager(createEntityManager());
		executor.init();
		return executor;
	}

	public Class<?> getObjectType() {
		return Executor.class;
	}

	public boolean isSingleton() {
		return true;
	}
}
