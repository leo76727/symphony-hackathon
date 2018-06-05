package com.symphony.hackathon.gs3.model;

public class Actor {
    public String actorName;
    public String url;

    public Actor() {
    }

    public Actor(String actorName, String url) {
        this.actorName = actorName;
        this.url = url;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
