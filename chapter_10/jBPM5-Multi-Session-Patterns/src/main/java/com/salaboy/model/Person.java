package com.salaboy.model;

import java.io.Serializable;
import java.util.UUID;

public class Person implements Serializable{
    private String id;
    private String name;
    private int age;
    
    public Person(String name, int age) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Person{" + "id=" + id + ", name=" + name + ", age=" + age + '}';
    }

   
    
    
}
