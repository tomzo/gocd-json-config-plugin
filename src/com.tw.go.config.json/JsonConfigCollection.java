package com.tw.go.config.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonConfigCollection {

    private JsonObject mainObject = new JsonObject();
    private JsonArray environments = new JsonArray();
    private JsonArray groups = new JsonArray();

    public JsonConfigCollection()
    {
        mainObject.add("environments",environments);
        mainObject.add("groups",groups);
    }

    public void addEnvironment(JsonElement environment) {
        environments.add(environment);
    }
    public void addGroup(JsonElement group) {
        groups.add(group);
    }

    public JsonObject getJsonObject()
    {
        return mainObject;
    }
}
