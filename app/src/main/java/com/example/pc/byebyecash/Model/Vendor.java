package com.example.pc.byebyecash.Model;

public class Vendor {
    private double longitute;
    private double latitude;
    private String name;
    private int credit;
    private String password;

    public Vendor(double longitute, double latitude, String name, int credit, String password) {
        this.longitute = longitute;
        this.latitude = latitude;
        this.name = name;
        this.credit = credit;
        this.password = password;
    }

    public Vendor() {
    }

    public double getLongitute() {
        return longitute;
    }

    public void setLongitute(double longitute) {
        this.longitute = longitute;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
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
