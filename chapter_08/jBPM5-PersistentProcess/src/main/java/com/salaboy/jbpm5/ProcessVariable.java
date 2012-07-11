/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5;

/**
 *
 * @author salaboy
 */
//@PropertySpecific

public class ProcessVariable<T> {
    private long processInstanceId;
    private String name;
    private T value;

    public ProcessVariable(long processInstanceId, String name, T value) {
        this.processInstanceId = processInstanceId;
        this.name = name;
        this.value = value;
    }

    public long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public T getValue() {
        return value;
    }
    
    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ProcessVariable{" + "processInstanceId=" + processInstanceId + ", name=" + name + ", value=" + value + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProcessVariable other = (ProcessVariable) obj;
        if (this.processInstanceId != other.processInstanceId) {
            return false;
        }
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if (this.value != other.value && (this.value == null || !this.value.equals(other.value))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + (int) (this.processInstanceId ^ (this.processInstanceId >>> 32));
        hash = 47 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 47 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }

    

    
    
    
    
}
