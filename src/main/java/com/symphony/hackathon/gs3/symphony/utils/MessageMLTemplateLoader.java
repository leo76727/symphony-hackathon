package com.symphony.hackathon.gs3.symphony.utils;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;

public class MessageMLTemplateLoader {
    public static String load(String id)  {
        try {
            return Resources.toString(Resources.getResource(String.format("render/%s-template.ml",id)), Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load template " + id);
        }
    }
}
