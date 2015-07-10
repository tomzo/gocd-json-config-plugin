package com.tw.go.config.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonConfigCollection {

    private final JsonObject defaultGroup;
    private final JsonArray defaultGroupPipelines;
    private JsonObject mainObject = new JsonObject();
    private JsonArray environments = new JsonArray();
    private JsonArray groups = new JsonArray();

    public JsonConfigCollection()
    {
        mainObject.add("environments",environments);
        mainObject.add("groups",groups);
        defaultGroup = new JsonObject();
        groups.add(defaultGroup);
        defaultGroupPipelines = new JsonArray();
        defaultGroup.add("pipelines",defaultGroupPipelines);
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

    public void addPipeline(JsonElement pipeline) {
        defaultGroupPipelines.add(pipeline);
    }
}
