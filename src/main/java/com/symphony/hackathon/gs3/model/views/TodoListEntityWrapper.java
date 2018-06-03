package com.symphony.hackathon.gs3.model.views;

import com.symphony.hackathon.gs3.model.Todo;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
        this.todos.items = todos.stream().sorted(Comparator.comparing(t -> t.due == null ? LocalDateTime.MAX :t.due)).collect(Collectors.toList());
    }
}
