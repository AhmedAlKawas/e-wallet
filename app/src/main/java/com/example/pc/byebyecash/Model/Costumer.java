package com.example.pc.byebyecash.Model;

public class Costumer {

    private String name;
    private int credit;
    private String password;

    public Costumer() {
    }

    public Costumer(String name, int credit, String password) {
        this.name = name;
        this.credit = credit;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
