package com.salaboy.jbpm5.dev.guide.executor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CommandContext implements Serializable {

    private static final long serialVersionUID = -1440017934399413860L;
    private Map<String, Serializable> data = new HashMap<String, Serializable>();

    public CommandContext() {
    }

    public void setData(Map<String, Serializable> data) {
        this.data = data;
    }

    public Map<String, Serializable> getData() {
        return data;
    }

    public Serializable getData(String key) {
        return data.get(key);
    }

    public void setData(String key, Serializable value) {
        data.put(key, value);
    }

    public Set<String> keySet() {
        return data.keySet();
    }

    @Override
    public String toString() {
        return "CommandContext{" + "data=" + data + '}';
    }
}
