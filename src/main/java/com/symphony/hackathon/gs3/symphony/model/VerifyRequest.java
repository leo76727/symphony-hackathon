package com.symphony.hackathon.gs3.symphony.model;

public class VerifyRequest {

    private String jwt;
    private String podId;

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public String getPodId() {
        return podId;
    }

    public void setPodId(String podId) {
        this.podId = podId;
    }
}
