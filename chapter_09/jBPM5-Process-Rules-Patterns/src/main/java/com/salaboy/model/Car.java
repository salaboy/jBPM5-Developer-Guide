/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.model;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author salaboy
 */
public class Car implements Serializable{
    private String name;
    private Date year;
    private int nroOfDoors;
    private String transmition;
    private String fuelType;
    private int maximunSpeed;
    private double originalPrice;
    private double currentPrice;
    private int ranking;
    
    public Car() {
    }

    public Car(String name, Date year, int nroOfDoors, String transmition, String fuelType, int maximunSpeed, double originalPrice) {
        this.name = name;
        this.year = year;
        this.nroOfDoors = nroOfDoors;
        this.transmition = transmition;
        this.fuelType = fuelType;
        this.maximunSpeed = maximunSpeed;
        this.originalPrice = originalPrice;
    }

    
    
    public double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getYear() {
        return year;
    }

    public void setYear(Date year) {
        this.year = year;
    }

    public int getNroOfDoors() {
        return nroOfDoors;
    }

    public void setNroOfDoors(int nroOfDoors) {
        this.nroOfDoors = nroOfDoors;
    }

    public String getTransmition() {
        return transmition;
    }

    public void setTransmition(String transmition) {
        this.transmition = transmition;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public int getMaximunSpeed() {
        return maximunSpeed;
    }

    public void setMaximunSpeed(int maximunSpeed) {
        this.maximunSpeed = maximunSpeed;
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    @Override
    public String toString() {
        return "Car{" + "name=" + name + ", year=" + year + ", nroOfDoors=" + nroOfDoors + ", transmition=" + transmition + ", fuelType=" + fuelType + ", maximunSpeed=" + maximunSpeed + ", originalPrice=" + originalPrice + ", currentPrice=" + currentPrice + ", ranking=" + ranking + '}';
    }
    
    

    

    
    
    
    
}
