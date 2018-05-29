package com.symphony.hackathon.gs3.model.views;

import com.symphony.hackathon.gs3.model.Todo;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public class TodoListByRoomEntityWrapper {
    public static class Inner {
        public List<RoomList> rooms;

        public Inner() {
        }
    }

    public static class RoomList {
        public String roomName;
        public List<Todo> items;

        public RoomList() {
        }

        public RoomList(String roomName, List<Todo> items) {
            this.roomName = roomName;
            this.items = items;
        }
    }
    public Inner todos;
    public String action;

    public TodoListByRoomEntityWrapper() {
    }

    public TodoListByRoomEntityWrapper(List<Todo> todos) {
        this.todos = new Inner();
        this.todos.rooms = todos.stream().collect(groupingBy(t -> t.roomName)).entrySet().stream().map(e->new RoomList(e.getKey(), e.getValue())).collect(Collectors.toList());
    }
}
