package com.symphony.hackathon.gs3.model.views;

import com.symphony.hackathon.gs3.model.Todo;

public class TodoEntityWrapper {
    public Todo todo;
    public String action;

    public TodoEntityWrapper(Todo todo, String action) {
        this.todo = todo;
        this.action = action;
    }

    public TodoEntityWrapper() {
    }
}
