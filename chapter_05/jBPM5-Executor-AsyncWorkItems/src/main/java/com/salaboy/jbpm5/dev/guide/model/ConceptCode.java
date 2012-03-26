/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.dev.guide.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author salaboy
 */
public class ConceptCode implements Serializable{
    private String code;
    private BigDecimal rate;
    private String desc;
    private int units;

    public ConceptCode() {
    }

    
    
    public ConceptCode(String code, BigDecimal rate, String desc, int units) {
        this.code = code;
        this.rate = rate;
        this.desc = desc;
        this.units = units;
    }
    
    
    

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public int getUnits() {
        return units;
    }

    public void setUnits(int units) {
        this.units = units;
    }

    @Override
    public String toString() {
        return "ConceptCode{" + "code=" + code + ", rate=" + rate + ", desc=" + desc + ", units=" + units + '}';
    }
    
    
    
}
