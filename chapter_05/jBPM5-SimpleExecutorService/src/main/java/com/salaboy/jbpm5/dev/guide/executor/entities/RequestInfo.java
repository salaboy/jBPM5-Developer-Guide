/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.salaboy.jbpm5.dev.guide.executor.entities;

import java.util.Date;
import javax.persistence.*;

/**
 *
 * @author salaboy
 */
@Entity(name="RequestInfo")
public class RequestInfo {
    
    @Id @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    @Temporal(TemporalType.TIMESTAMP)
    private Date time;
    @Enumerated(EnumType.STRING)
    private STATUS status;
    private String commandName;
    private String message;
    //Business Key for callback
    private String key;
    @Lob
    private byte[] requestData;

    public RequestInfo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }
    
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

	public byte[] getRequestData() {
		return requestData;
	}

	public void setRequestData(byte[] requestData) {
		this.requestData = requestData;
	}
}
