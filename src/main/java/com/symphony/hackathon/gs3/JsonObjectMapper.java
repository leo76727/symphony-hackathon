package com.symphony.hackathon.gs3;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.dropwizard.jackson.Jackson;

public class JsonObjectMapper {
    private static ObjectMapper mapper = null;
    public static ObjectMapper get(){
        if(mapper == null){
            mapper = Jackson.newObjectMapper();
            mapper = mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        }
        return mapper;
    }
}
