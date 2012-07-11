package com.salaboy.model;

public class Person {

    private String name;
    private int age;
    private String plan;
    private int score;
    public Person(String name, int age) {
        super();
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

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
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
        if ((this.plan == null) ? (other.plan != null) : !this.plan.equals(other.plan)) {
            return false;
        }
        if (this.score != other.score) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 89 * hash + this.age;
        hash = 89 * hash + (this.plan != null ? this.plan.hashCode() : 0);
        hash = 89 * hash + this.score;
        return hash;
    }

    @Override
    public String toString() {
        return "Person{" + "name=" + name + ", age=" + age + ", plan=" + plan + ", score=" + score + '}';
    }

   

   
    
    
}
