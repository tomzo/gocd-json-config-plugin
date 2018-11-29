package com.tw.go.config.json;


import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

class JSONUtils {
    static <T> T fromJSON(String json) {
        return new GsonBuilder().create().fromJson(json, new TypeToken<T>() {}.getType());
    }

    static String toJSON(Object object) {
        return new GsonBuilder().create().toJson(object);
    }
}
