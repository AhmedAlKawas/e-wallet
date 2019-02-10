package com.example.pc.byebyecash.Model;

public class Request {

    private float credit;
    private String response;

    public Request(float credit, String response) {
        this.credit = credit;
        this.response = response;
    }

    public Request() {
    }

    public float getCredit() {
        return credit;
    }

    public void setCredit(float credit) {
        this.credit = credit;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
