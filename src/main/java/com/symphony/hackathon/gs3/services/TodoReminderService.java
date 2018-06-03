package com.symphony.hackathon.gs3.services;

import com.symphony.hackathon.gs3.model.Todo;
import com.symphony.hackathon.gs3.model.views.TodoEntityWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.symphony.hackathon.gs3.services.SymphonyToDoMessengeSender.TASK_TEMPLATE;

public class TodoReminderService {

    private final Logger logger = LoggerFactory.getLogger(TodoReminderService.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private SymphonyClient symphonyClient;
    private final TodoService todoService;
    private SymphonyToDoMessengeSender messageSender;

    public TodoReminderService(SymphonyClient symphonyClient, TodoService todoService, SymphonyToDoMessengeSender messageSender) {
        this.symphonyClient = symphonyClient;
        this.todoService = todoService;
        this.messageSender = messageSender;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            try{
                List<Todo> todos = todoService.filtered(t -> !t.reminded && t.due != null && t.due.isBefore(LocalDateTime.now()));
                for(Todo todo : todos){
                    try{
                        sendReminder(todo);
                        todo.reminded = true;
                        todoService.save(todo);
                        logger.info("Sent reminder for {}", todo);
                    } catch (Exception e){
                        logger.error("Failed to send reminder for {}, will retry", todo, e);
                    }
                }
                logger.info("Sent {} reminders", todos.size());
            }catch(Exception e){
                logger.error("Failed to send batch of reminders, will retry", e);
            }

        }, 0, 1, TimeUnit.MINUTES);
    }

    private void sendReminder(Todo todo) throws Exception {
        this.messageSender.sendEntityMessage(todo.roomId, new TodoEntityWrapper(todo, "Reminder!"), TASK_TEMPLATE);
        if(todo.roomName != "Personal" && todo.assigneeId != null){
            String imStreamId = this.symphonyClient.getStreamsClient().getStream(this.symphonyClient.getUsersClient().getUserFromId(todo.assigneeId)).getStreamId();
            this.messageSender.sendEntityMessage(imStreamId, new TodoEntityWrapper(todo, "Reminder!"), TASK_TEMPLATE);
        }
    }
}
