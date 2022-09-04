package com.apetta.detext_app.login;

public class User {

    private String email;

    public User() { }

    public User(String email){
        setEmail(email);
    }

    public void setEmail(String email) {this.email = email;}
    public String getEmail() {return this.email;}
}
