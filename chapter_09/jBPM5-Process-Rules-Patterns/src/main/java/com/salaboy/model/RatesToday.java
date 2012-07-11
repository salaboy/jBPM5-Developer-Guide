/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.model;

/**
 *
 * @author salaboy
 */
public class RatesToday {
    private int rateA;
    private int rateB;

    public RatesToday(int rateA, int rateB) {
        this.rateA = rateA;
        this.rateB = rateB;
    }

    public int getRateA() {
        return rateA;
    }

    public void setRateA(int rateA) {
        this.rateA = rateA;
    }

    public int getRateB() {
        return rateB;
    }

    public void setRateB(int rateB) {
        this.rateB = rateB;
    }

    @Override
    public String toString() {
        return "RatesToday{" + "rateA=" + rateA + ", rateB=" + rateB + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RatesToday other = (RatesToday) obj;
        if (this.rateA != other.rateA) {
            return false;
        }
        if (this.rateB != other.rateB) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + this.rateA;
        hash = 23 * hash + this.rateB;
        return hash;
    }
    
    
}
