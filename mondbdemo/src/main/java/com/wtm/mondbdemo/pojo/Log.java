package com.wtm.mondbdemo.pojo;


import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "users")
public class Log {
    private String username;
    private String age;

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
