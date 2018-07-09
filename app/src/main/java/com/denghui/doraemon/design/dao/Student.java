package com.denghui.doraemon.design.dao;

public class Student {
    private String mName;
    private int mAge;

    public Student(String name, int age) {
        mName = name;
        mAge = age;
    }

    public int getAge() {
        return mAge;
    }

    public String getName() {
        return mName;
    }

    public void setAge(int mAge) {
        this.mAge = mAge;
    }

    public void setName(String mName) {
        this.mName = mName;
    }
}
