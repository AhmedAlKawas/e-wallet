package com.example.pc.byebyecash.Model;

public class Request {

    private float credit;
    private String response;
    private String senderMobile;
    private String senderRole;
    private String status;

    public Request(float credit, String response, String senderMobile, String senderRole, String status) {
        this.credit = credit;
        this.response = response;
        this.senderMobile = senderMobile;
        this.senderRole = senderRole;
        this.status = status;
    }

    public Request(String response, String status) {
        this.response = response;
        this.status = status;
    }

    public Request() {
    }

    public String getSenderMobile() {
        return senderMobile;
    }

    public void setSenderMobile(String senderMobile) {
        this.senderMobile = senderMobile;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
