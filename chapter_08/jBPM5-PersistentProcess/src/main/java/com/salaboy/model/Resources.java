/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.model;

/**
 *
 * @author salaboy
 */
public class Resources {
    private int available;

    public Resources(int available) {
        this.available = available;
    }

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "Resources{" + "available=" + available + '}';
    }
    
}
