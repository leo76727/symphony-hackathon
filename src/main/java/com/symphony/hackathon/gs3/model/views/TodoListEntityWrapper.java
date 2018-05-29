package com.symphony.hackathon.gs3.model.views;

import com.symphony.hackathon.gs3.model.Todo;

import java.util.List;

public class TodoListEntityWrapper {
    public static class Inner {
        public List<Todo> items;

        public Inner() {
        }
    }
    public Inner todos;
    public String action;

    public TodoListEntityWrapper() {
    }

    public TodoListEntityWrapper(List<Todo> todos) {
        this.todos = new Inner();
        this.todos.items = todos;
    }
}
