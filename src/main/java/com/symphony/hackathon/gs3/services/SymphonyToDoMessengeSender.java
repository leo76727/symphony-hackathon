package com.symphony.hackathon.gs3.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.symphony.hackathon.gs3.JsonObjectMapper;
import com.symphony.hackathon.gs3.model.Todo;
import com.symphony.hackathon.gs3.model.views.TodoEntityWrapper;
import com.symphony.hackathon.gs3.symphony.utils.MessageMLTemplateLoader;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.exceptions.MessagesException;
import org.symphonyoss.client.exceptions.UsersClientException;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymStream;
import org.symphonyoss.symphony.pod.model.Stream;

import java.net.URLEncoder;
import java.nio.charset.Charset;

public class SymphonyToDoMessengeSender {

    public static String TASK_TEMPLATE = MessageMLTemplateLoader.load("task");
    public static String TASK_LIST_TEMPLATE = MessageMLTemplateLoader.load("taskList");
    public static String TASK_LIST_BY_ROOM_TEMPLATE = MessageMLTemplateLoader.load("taskListByRoom");

    private SymphonyClient symClient;
    private ObjectMapper mapper = JsonObjectMapper.get();

    public SymphonyToDoMessengeSender(SymphonyClient symClient) {
        this.symClient = symClient;
    }

    public void sendEntityMessage(String streamId, Object entity, String template) throws Exception {
        String json = mapper.writeValueAsString(entity);
        SymMessage message = new SymMessage();
        message.setMessage(template);
        message.setEntityData(json);
        symClient.getMessagesClient().sendMessage(new Stream().id(streamId), message);
    }

    public void sendMessage(String streamId, String text) throws Exception {
        SymMessage message = new SymMessage();

        message.setMessage(String.format("<messageML>%s</messageML>", text));
        symClient.getMessagesClient().sendMessage(new Stream().id(streamId), message);
    }
}
