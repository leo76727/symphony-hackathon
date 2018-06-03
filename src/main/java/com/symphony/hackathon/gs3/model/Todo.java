package com.symphony.hackathon.gs3.model;

import java.time.LocalDateTime;
import java.util.List;

public class Todo {
    public long id;
    public String summary;
    public Status status;
    public String creatorName;
    public Long creatorId;
    public String assigneeName;
    public Long assigneeId;
    public LocalDateTime createdAt;
    public LocalDateTime due;
    public List<String> labels;
    public String roomId;
    public String roomName;
    public boolean reminded;

    public Todo() {
    }

    public Todo(long id, String summary, String creatorName, Long creatorId, String assigneeName, Long assigneeId, String roomId, String roomName, LocalDateTime due, List<String> labels) {
        this.id = id;
        this.summary = summary;
        this.creatorName = creatorName;
        this.creatorId = creatorId;
        this.assigneeName = assigneeName;
        this.assigneeId = assigneeId;
        this.roomId = roomId;
        this.roomName = roomName;
        this.due = due;
        this.labels = labels;
        this.status = Status.NEW;
        this.createdAt = LocalDateTime.now();
        this.reminded = false;
    }
}
