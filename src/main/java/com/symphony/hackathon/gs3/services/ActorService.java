package com.symphony.hackathon.gs3.services;

import com.symphony.hackathon.gs3.model.Actor;
import com.symphony.hackathon.gs3.model.Status;
import com.symphony.hackathon.gs3.model.Todo;
import com.symphony.hackathon.gs3.model.views.TodoEntityWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.symphony.hackathon.gs3.services.SymphonyToDoMessengeSender.TASK_TEMPLATE;

public class ActorService {
    private final Logger logger = LoggerFactory.getLogger(ActorService.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    TodoService todoService;
    SymphonyToDoMessengeSender messageSender;
    Map<String, Actor> actors = new HashMap<>();
    private SymphonyClient symphonyClient;

    public ActorService() {
        actors.put("rfq-pricer", new Actor("rfq-pricer", "https://rfq.com/price?q="));
        actors.put("rfq-trader", new Actor("rfq-trader", "https://rfq.com/trade?q="));
    }

    public Actor addActor(String text) {
        String[] split = text.split("\\s+");
        Actor actor = new Actor(split[0], split[1]);
        actors.put(split[0], actor);

        return actor;
    }

    public Collection<Actor> getActors() {
        return actors.values();
    }

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            try{

                List<Todo> todos = todoService.filtered(t -> actors.keySet().contains(t.assigneeName) && t.status != Status.DONE);
                for(Todo todo : todos){
                    try{
                        switch(todo.assigneeName){
                            case "rfq-pricer":
                                price(todo);
                                break;
                            case "rfq-trader":
                                trade(todo);
                            default:
                                logger.error("Invalid actor");
                        }
                    } catch (Exception e){
                            logger.error("Failed to auto act for {}, will retry", todo, e);
                        }
                    }
                    logger.info("Auto acted {} todos", todos.size());
                }catch(Exception e) {
                logger.error("Failed to auto act on batch of reminders, will retry", e);
            }

        }, 0, 10, TimeUnit.SECONDS);
    }

    private void trade(Todo todo) throws Exception {
        todo.assigneeId = todo.creatorId;
        todo.assigneeName = todo.creatorName;
        todo.status = Status.WIP;
        todo.summary = todo.summary + " TradeId: Trade 123456";
        this.todoService.save(todo);
        messageSender.sendEntityMessage(todo.roomId, new TodoEntityWrapper(todo, "Traded!!"), TASK_TEMPLATE);
        if(todo.roomName != "Personal" && todo.assigneeId != null) {
            String imStreamId = this.symphonyClient.getStreamsClient().getStream(this.symphonyClient.getUsersClient().getUserFromId(todo.creatorId)).getStreamId();
            messageSender.sendEntityMessage(imStreamId, new TodoEntityWrapper(todo, "Traded!"), TASK_TEMPLATE);
        }
        System.out.println("Traded: " + todo.summary);
    }

    private void price(Todo todo) throws Exception {
        todo.assigneeId = todo.creatorId;
        todo.assigneeName = todo.creatorName;
        todo.status = Status.WIP;
        todo.summary = todo.summary + "Price: $514.3";
        this.todoService.save(todo);
        messageSender.sendEntityMessage(todo.roomId, new TodoEntityWrapper(todo, "Priced!"), TASK_TEMPLATE);
        if(todo.roomName != "Personal" && todo.assigneeId != null) {
            String imStreamId = this.symphonyClient.getStreamsClient().getStream(this.symphonyClient.getUsersClient().getUserFromId(todo.creatorId)).getStreamId();
            messageSender.sendEntityMessage(imStreamId, new TodoEntityWrapper(todo, "Priced!"), TASK_TEMPLATE);
        }
        System.out.println("Pricing: " + todo.summary);
    }

    public TodoService getTodoService() {
        return todoService;
    }

    public void setTodoService(TodoService todoService) {
        this.todoService = todoService;
    }

    public void setActors(Map<String, Actor> actors) {
        this.actors = actors;
    }

    public SymphonyToDoMessengeSender getMessageSender() {
        return messageSender;
    }

    public void setMessageSender(SymphonyToDoMessengeSender messageSender) {
        this.messageSender = messageSender;
    }

    public SymphonyClient getSymphonyClient() {
        return symphonyClient;
    }

    public void setSymphonyClient(SymphonyClient symphonyClient) {
        this.symphonyClient = symphonyClient;
    }
}
