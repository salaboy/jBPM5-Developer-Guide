/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.model;

/**
 *
 * @author salaboy
 */
public class Car {
    private Person person;

    public Car(Person person) {
        this.person = person;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    @Override
    public String toString() {
        return "Car{" + "person=" + person + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Car other = (Car) obj;
        if (this.person != other.person && (this.person == null || !this.person.equals(other.person))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + (this.person != null ? this.person.hashCode() : 0);
        return hash;
    }
    
    
}
