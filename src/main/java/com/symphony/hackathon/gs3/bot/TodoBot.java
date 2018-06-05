package com.symphony.hackathon.gs3.bot;

import com.symphony.hackathon.gs3.TodoBotConfiguration;
import com.symphony.hackathon.gs3.model.Todo;
import com.symphony.hackathon.gs3.model.views.TodoEntityWrapper;
import com.symphony.hackathon.gs3.model.views.TodoListByRoomEntityWrapper;
import com.symphony.hackathon.gs3.model.views.TodoListEntityWrapper;
import com.symphony.hackathon.gs3.services.SymphonyToDoMessengeSender;
import com.symphony.hackathon.gs3.services.TodoReminderService;
import com.symphony.hackathon.gs3.services.TodoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.events.*;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.model.Room;
import org.symphonyoss.client.services.*;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymStreamTypes;

import java.util.Date;
import java.util.List;

public class TodoBot implements ChatListener, ChatServiceListener, RoomServiceEventListener, RoomEventListener {

    private static TodoBot instance;
    private final Logger logger = LoggerFactory.getLogger(TodoBot.class);
    private final TodoReminderService reminderService;
    private SymphonyClient symClient;
    private RoomService roomService;
    private TodoService todoService;
    private SymphonyToDoMessengeSender messageSender;
    private TodoBotConfiguration config;

    protected TodoBot(SymphonyClient symClient, TodoBotConfiguration config) {
        this.symClient = symClient;
        this.config = config;
        this.todoService = new TodoService(symClient);
        this.messageSender = new SymphonyToDoMessengeSender(this.symClient);
        this.reminderService = new TodoReminderService(this.symClient, this.todoService, this.messageSender);
        this.reminderService.start();
        init();
    }

    public static TodoBot getInstance(SymphonyClient symClient, TodoBotConfiguration config) {
        if (instance == null) {
            instance = new TodoBot(symClient, config);
        }
        return instance;
    }

    private void init() {
        logger.info("Connections example starting...");
        symClient.getChatService().addListener(this);
        roomService = symClient.getRoomService();
        roomService.addRoomServiceEventListener(this);
        ConnectionsService connectionsService = new ConnectionsService(symClient);
        connectionsService.setAutoAccept(true);
    }


    //Chat sessions callback method.
    @Override
    public void onChatMessage(SymMessage message){
        try {
            if (message == null)
                return;
            logger.debug("TS: {}From ID: {} SymMessage: {} SymMessage Type: {}",
                    message.getTimestamp(),
                    message.getFromUserId(),
                    message.getMessage(),
                    message.getMessageType());

            String messageText = message.getMessageText().trim().toLowerCase();
            if (messageText.contains("hi todo bot")) {
                this.messageSender.sendMessage(message.getStreamId(), "Hello");
            }
            if (messageText.startsWith("/help")) {
                if(message.getStream().getStreamType() == SymStreamTypes.Type.ROOM){
                    this.messageSender.sendMessage(message.getStreamId(),"I've responded to you in private");
                    this.messageSender.sendMessage(this.symClient.getStreamsClient().getStream(message.getSymUser()).getStreamId(), usage());
                } else {
                    this.messageSender.sendMessage(message.getStreamId(), usage());
                }

            }
            if (messageText.startsWith("#task ")) {
                Todo task = todoService.createTask(message);

                this.messageSender.sendEntityMessage(task.roomId, new TodoEntityWrapper(task, "Created"), SymphonyToDoMessengeSender.TASK_TEMPLATE);
            }
            if (messageText.startsWith("#taskr ")) {
                List<Todo> tasks = todoService.createTasks(message);
                for(Todo task : tasks){
                    this.messageSender.sendEntityMessage(task.roomId, new TodoEntityWrapper(task, "Created!"), SymphonyToDoMessengeSender.TASK_TEMPLATE);
                }
            }
            if (messageText.startsWith("#task-assign ")) {
                Todo task = todoService.assignTask(message);
                if(task != null){
                    this.messageSender.sendEntityMessage(message.getStreamId(), new TodoEntityWrapper(task, "Assigned!"), SymphonyToDoMessengeSender.TASK_TEMPLATE);
                }
            }
            if (messageText.startsWith("#task-start ")) {
                Todo task = todoService.startTask(message);
                if(task != null){
                    this.messageSender.sendEntityMessage(message.getStreamId(), new TodoEntityWrapper(task, "Started!"), SymphonyToDoMessengeSender.TASK_TEMPLATE);
                }
            }

            if (messageText.startsWith("#task-done ")) {
                Todo task = todoService.completeTask(message);
                if(task != null){
                    this.messageSender.sendEntityMessage(message.getStreamId(), new TodoEntityWrapper(task, "Done!"), SymphonyToDoMessengeSender.TASK_TEMPLATE);
                }
            }
            if (messageText.startsWith("#task-edit ")) {
                Todo task = todoService.editTask(message);
                if(task != null){
                    this.messageSender.sendEntityMessage(message.getStreamId(), new TodoEntityWrapper(task, "Edited!"), SymphonyToDoMessengeSender.TASK_TEMPLATE);
                }
            }
            if (messageText.startsWith("#test ")) {
                this.messageSender.sendEntityMessage(message.getStreamId(), new ExampleTimerEntity(), "<messageML><div class=\"entity\" data-entity-id=\"timer\"><b><i>Please install the Hello World application to render this entity.</i></b></div></messageML>");
            }
            if (messageText.startsWith("#tasks")) {
                List<Todo> tasks;
                if(message.getStream().getStreamType() == SymStreamTypes.Type.ROOM){
                    tasks = todoService.getForRoom(message.getStreamId());
                    if(tasks.size() == 0){
                        this.messageSender.sendMessage(message.getStreamId(), "There are no tasks");
                    } else {
                        this.messageSender.sendEntityMessage(message.getStreamId(), new TodoListEntityWrapper(tasks), SymphonyToDoMessengeSender.TASK_LIST_TEMPLATE);
                    }
                } else {
                    tasks = todoService.getForAssignee(message.getSymUser().getId());
                    if(tasks.size() == 0){
                        this.messageSender.sendMessage(message.getStreamId(), "There are no tasks");
                    } else {
                        this.messageSender.sendEntityMessage(message.getStreamId(), new TodoListByRoomEntityWrapper(tasks), SymphonyToDoMessengeSender.TASK_LIST_BY_ROOM_TEMPLATE);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to send message", e);
        }
    }

    public static class ExampleTimerEntity{
        InnerTimer timer;
        public static class InnerTimer {
            public String version = "1.0";
            public String type = "com.symphony.timer";

            public String getVersion() {
                return version;
            }

            public void setVersion(String version) {
                this.version = version;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

        }
        public ExampleTimerEntity(){
            this.timer = new InnerTimer();
        }

        public InnerTimer getTimer() {
            return timer;
        }

        public void setTimer(InnerTimer timer) {
            this.timer = timer;
        }
    }

    private String usage() {
        return "<br />" +
                "I'm a simple bot that can help you manage your tasks and i can remind you when they're due.<br />" +
                "Tasks are scoped to each room, you can create a private task list by messaging me directly.<br />" +
                "Add labels using #hashtag, set priority with p:1 (1, 2 or 3), assign it with @user mention<br />" +
                "<br />" +
                "Create a task:<br />" +
                "#task summary <br />" +
                "#task summary #label1 #label2 @Assignee <br />" +
                "#task summary at Thursday #label1 #label2 @Assignee <br />" +
                "#task summary at June 14th #label1 #label2 @Assignee <br />" +
                "<br />" +
                "Create a recurring task:<br />" +
                "#taskr summary every date expression until limit<br />" +
                "#taskr submit timesheet every friday at 4PM until 1 month<br />" +
                "<br />" +
                "List tasks (all tasks for the current room or all tasks assigned to you via a direct message):<br />" +
                "#tasks<br />" +
                "<br />" +
                "Start a task, moving the state to WIP and assigning to you if it's currently unassigned:<br />" +
                "#task-start TODO-1<br />" +
                "<br />" +
                "Complete a task, moving it to the done state.<br />" +
                "#task-complete TODO-1<br />" +
                "<br />" +
                "Assign a task:<br />" +
                "#task-assign TODO-1 @John Doe<br />" +
                "<br />" +
                "Edit a task:<br />" +
                "#task-edit Works exactly like create task, but replaces it.<br />" +
                "<br />" +
                "Display this message:<br />" +
                "#help<br />";
    }

    @Override
    public void onNewChat(Chat chat) {
        chat.addListener(this);
        logger.debug("New chat session detected on stream {} with {}", chat.getStream().getStreamId(), chat.getRemoteUsers());
    }

    @Override
    public void onRemovedChat(Chat chat) {
    }

    @Override
    public void onMessage(SymMessage symMessage) {
        logger.info("Message detected from stream: {} from: {} message: {}",
                symMessage.getStreamId(),
                symMessage.getFromUserId(),
                symMessage.getMessage());
    }

    @Override
    public void onNewRoom(Room room) {
        room.addEventListener(this);
    }

    @Override
    public void onRoomMessage(SymMessage symMessage) {
        onChatMessage(symMessage);
    }

    @Override
    public void onSymRoomDeactivated(SymRoomDeactivated symRoomDeactivated) {

    }

    @Override
    public void onSymRoomMemberDemotedFromOwner(SymRoomMemberDemotedFromOwner symRoomMemberDemotedFromOwner) {

    }

    @Override
    public void onSymRoomMemberPromotedToOwner(SymRoomMemberPromotedToOwner symRoomMemberPromotedToOwner) {

    }

    @Override
    public void onSymRoomReactivated(SymRoomReactivated symRoomReactivated) {

    }

    @Override
    public void onSymRoomUpdated(SymRoomUpdated symRoomUpdated) {

    }

    @Override
    public void onSymUserJoinedRoom(SymUserJoinedRoom symUserJoinedRoom) {
    }

    @Override
    public void onSymUserLeftRoom(SymUserLeftRoom symUserLeftRoom) {

    }

    @Override
    public void onSymRoomCreated(SymRoomCreated symRoomCreated) {

    }
}
