package com.symphony.hackathon.gs3.services;

import com.google.common.collect.Lists;
import com.symphony.hackathon.gs3.model.Todo;
import com.symphony.hackathon.gs3.symphony.utils.EntityDataHelper;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.exceptions.StreamsException;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymStreamTypes;
import org.symphonyoss.symphony.clients.model.SymUser;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TodoService {

    AtomicLong idGenerator = new AtomicLong(1);

    private SymphonyClient symClient;
    private Map<Long, Todo> todos = new HashMap<>();
    private TodoRepo repo = new TodoRepo();

    public TodoService(SymphonyClient symClient){
        this.symClient = symClient;
        todos = repo.loadAll();
        idGenerator.set(todos.keySet().stream().mapToLong(l->l).max().orElse(0) + 1 );
    }

    public Todo createTask(SymMessage message) throws StreamsException {

        Optional<SymUser> assignee = getAssignee(message);
        String assigneeName = assignee.map(SymUser::getDisplayName).orElse("Unassigned");
        Long assigneeId = assignee.map(SymUser::getId).orElse(null);
        List<String> labels = Lists.newArrayList();
        LocalDateTime due = null;
        String summary = message.getMessageText()
                .replace("/task ", "")
                .replace("@" + assigneeName, "");

        Todo todo = new Todo(
                idGenerator.getAndIncrement(),
                summary,
                message.getSymUser().getDisplayName(),
                message.getSymUser().getId(),
                assigneeName,
                assigneeId,
                message.getStreamId(),
                message.getStream().getStreamType() == SymStreamTypes.Type.ROOM ? symClient.getStreamsClient().getRoomDetail(message.getStreamId()).getRoomAttributes().getName() : "Personal",
                due,
                labels
        );
        save(todo);
        return todo;
    }

    public void save(Todo todo){
        todos.put(todo.id,todo);
        repo.save(todo);
    }

    private Optional<SymUser> getAssignee(SymMessage message) {
        if(message.getStream().getStreamType() != SymStreamTypes.Type.ROOM){
            return Optional.of(message.getSymUser());
        }
        List<SymUser> mentions = EntityDataHelper.getUserMentions(message.getEntityData(), symClient);
        if(mentions.size() >=1){
            return Optional.of(mentions.get(0));
        }
        return Optional.empty();
    }

    public List<Todo> getForRoom(String roomId) {
        return filtered(t->t.roomId.equals(roomId));
    }

    public List<Todo> getForAssignee(Long id) {
        return filtered(t->id.equals(t.assigneeId));
    }

    public List<Todo> filtered(Predicate<Todo> predicate){
        return todos.values().stream().filter(predicate).collect(Collectors.toList());
    }
}
