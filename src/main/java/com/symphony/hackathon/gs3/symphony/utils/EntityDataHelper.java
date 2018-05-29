package com.symphony.hackathon.gs3.symphony.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.symphony.clients.model.SymUser;

import java.util.ArrayList;
import java.util.List;

public class EntityDataHelper {
    private static ObjectMapper objectMapper = Jackson.newObjectMapper();
    public static List<SymUser> getUserMentions(String entityData, SymphonyClient client){
        try {
            List<SymUser> mentions = new ArrayList<>();
            JsonNode jsonNode = objectMapper.readValue(entityData, JsonNode.class);
            for(JsonNode item : jsonNode){
                if("com.symphony.user.mention".equals(item.get("type").asText())){
                    for(JsonNode mention : item.get("id")){
                        if("com.symphony.user.userId".equals(mention.get("type").asText())){
                            Long userId = mention.get("value").asLong();
                            mentions.add(client.getUsersClient().getUserFromId(userId));
                        }

                    }
                }
            }
            return mentions;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse mentions",e);
        }
    }

    public static void main(String[] args) {
        getUserMentions("{\"0\":{\"id\":[{\"type\":\"com.symphony.user.userId\",\"value\":349026222342589}],\"type\":\"com.symphony.user.mention\"}}", null);
    }
}
