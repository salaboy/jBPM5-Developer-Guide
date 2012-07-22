/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.model;

/**
 *
 * @author salaboy
 */
public class MarketMetric {
    private double metric;
    private boolean result;

    public MarketMetric(double metric) {
        this.metric = metric;
    }
    
    
    public MarketMetric(double metric, boolean result) {
        this.metric = metric;
        this.result = result;
    }

    public double getMetric() {
        return metric;
    }

    public void setMetric(double metric) {
        this.metric = metric;
    }

    public boolean getResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "MarketMetric{" + "metric=" + metric + ", result=" + result + '}';
    }
    
    
}
