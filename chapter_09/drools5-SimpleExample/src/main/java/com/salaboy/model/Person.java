/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.model;

/**
 *
 * @author salaboy
 */
public class Person {
    private String name;
    private int age;
    private boolean enabledToVote = false;
    private boolean enabledToDrive = false;
    private boolean happy = false;

    public Person(int age) {
        this.age = age;
    }

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isEnabledToDrive() {
        return enabledToDrive;
    }

    public void setEnabledToDrive(boolean enabledToDrive) {
        this.enabledToDrive = enabledToDrive;
    }

    public boolean isEnabledToVote() {
        return enabledToVote;
    }

    public void setEnabledToVote(boolean enabledToVote) {
        this.enabledToVote = enabledToVote;
    }

    public boolean isHappy() {
        return happy;
    }

    public void setHappy(boolean happy) {
        this.happy = happy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Person other = (Person) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if (this.age != other.age) {
            return false;
        }
        if (this.enabledToVote != other.enabledToVote) {
            return false;
        }
        if (this.enabledToDrive != other.enabledToDrive) {
            return false;
        }
        if (this.happy != other.happy) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 59 * hash + this.age;
        hash = 59 * hash + (this.enabledToVote ? 1 : 0);
        hash = 59 * hash + (this.enabledToDrive ? 1 : 0);
        hash = 59 * hash + (this.happy ? 1 : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "Person{" + "name=" + name + ", age=" + age + ", enabledToVote=" + enabledToVote + ", enabledToDrive=" + enabledToDrive + ", happy=" + happy + '}';
    }
    
    
}
