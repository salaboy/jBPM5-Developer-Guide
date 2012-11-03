/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author salaboy
 */
public class Data implements Serializable{
    private Map<String, Object> dataMap = new HashMap<String, Object>();

    public Data() {
    }

    public Data(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
    }
    
    public Map<String, Object> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
    }

    @Override
    public String toString() {
        return "Data{" + "dataMap=" + dataMap + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.dataMap != null ? this.dataMap.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Data other = (Data) obj;
        if (this.dataMap != other.dataMap && (this.dataMap == null || !this.dataMap.equals(other.dataMap))) {
            return false;
        }
        return true;
    }
    
    
    
    
}
