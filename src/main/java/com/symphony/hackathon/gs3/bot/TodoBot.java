package com.symphony.hackathon.gs3.bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.symphony.hackathon.gs3.JsonObjectMapper;
import com.symphony.hackathon.gs3.TodoBotConfiguration;
import com.symphony.hackathon.gs3.model.Todo;
import com.symphony.hackathon.gs3.model.views.TodoEntityWrapper;
import com.symphony.hackathon.gs3.model.views.TodoListByRoomEntityWrapper;
import com.symphony.hackathon.gs3.model.views.TodoListEntityWrapper;
import com.symphony.hackathon.gs3.services.TodoService;
import com.symphony.hackathon.gs3.symphony.utils.MessageMLTemplateLoader;
import io.dropwizard.jackson.Jackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.events.*;
import org.symphonyoss.client.exceptions.MessagesException;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.model.Room;
import org.symphonyoss.client.services.*;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymStream;
import org.symphonyoss.symphony.clients.model.SymStreamTypes;

import java.util.List;

public class TodoBot implements ChatListener, ChatServiceListener, RoomServiceEventListener, RoomEventListener {

    private static TodoBot instance;
    private final Logger logger = LoggerFactory.getLogger(TodoBot.class);
    private SymphonyClient symClient;
    private RoomService roomService;
    private TodoService todoService;
    TodoBotConfiguration config;
    private ObjectMapper mapper = JsonObjectMapper.get();

    private static String TASK_TEMPLATE = MessageMLTemplateLoader.load("task");
    private static String TASK_LIST_TEMPLATE = MessageMLTemplateLoader.load("taskList");
    private static String TASK_LIST_BY_ROOM_TEMPLATE = MessageMLTemplateLoader.load("taskListByRoom");

    protected TodoBot(SymphonyClient symClient, TodoBotConfiguration config) {
        this.symClient = symClient;
        this.config = config;
        this.todoService = new TodoService(symClient);
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
    public void onChatMessage(SymMessage message) {
        try {
            if (message == null)
                return;
            logger.debug("TS: {}\nFrom ID: {}\nSymMessage: {}\nSymMessage Type: {}",
                    message.getTimestamp(),
                    message.getFromUserId(),
                    message.getMessage(),
                    message.getMessageType());

            if (message.getMessageText().toLowerCase().contains("hi todo bot")) {
                sendMessage(message.getStream(), "Hello");
            }
            if (message.getMessageText().toLowerCase().startsWith("/task ")) {
                Todo task = todoService.createTask(message);
                sendMessage(message.getStream(), mapper.writeValueAsString(new TodoEntityWrapper(task, "Created")), TASK_TEMPLATE);
            }
            if (message.getMessageText().toLowerCase().startsWith("/tasks")) {
                List<Todo> tasks;
                if(message.getStream().getStreamType() == SymStreamTypes.Type.ROOM){
                    tasks = todoService.getForRoom(message.getStreamId());
                    if(tasks.size() == 0){
                        sendMessage(message.getStream(), "There are no tasks");
                    } else {
                        sendMessage(message.getStream(), mapper.writeValueAsString(new TodoListEntityWrapper(tasks)), TASK_LIST_TEMPLATE);
                    }
                } else {
                    tasks = todoService.getForAssignee(message.getSymUser().getId());
                    if(tasks.size() == 0){
                        sendMessage(message.getStream(), "There are no tasks");
                    } else {
                        sendMessage(message.getStream(), mapper.writeValueAsString(new TodoListByRoomEntityWrapper(tasks)), TASK_LIST_BY_ROOM_TEMPLATE);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to send message", e);
        }
    }

    private void sendMessage(SymStream stream, String entity, String messageML) throws MessagesException {
        SymMessage message = new SymMessage();
        message.setMessage(messageML);
        message.setEntityData(entity);
        symClient.getMessagesClient().sendMessage(stream, message);
    }

    private void sendMessage(SymStream stream, String text) throws MessagesException {
        SymMessage message = new SymMessage();
        message.setMessage(String.format("<messageML>%s</messageML>", text));
        symClient.getMessagesClient().sendMessage(stream, message);
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
