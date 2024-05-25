package com.tw.go.config.json;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

class JSONUtils {

    private static final Gson GSON = new GsonBuilder().create();

    static Map<String, String> fromJSON(String json) {
        return GSON.fromJson(json, new TypeToken<Map<String, String>>() {}.getType());
    }

    static String toJSON(Object object) {
        return GSON.toJson(object);
    }
}
