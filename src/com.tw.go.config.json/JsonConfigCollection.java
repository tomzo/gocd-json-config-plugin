package com.tw.go.config.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JsonConfigCollection {
    private static final int TARGET_VERSION = 1;

    private JsonObject mainObject = new JsonObject();
    private JsonArray environments = new JsonArray();
    private JsonArray pipelines = new JsonArray();
    private JsonArray errors = new JsonArray();

    public JsonConfigCollection()
    {
        mainObject.add("target_version",new JsonPrimitive(TARGET_VERSION));
        mainObject.add("environments",environments);
        mainObject.add("pipelines",pipelines);
        mainObject.add("errors",errors);
    }

    protected JsonArray getEnvironments()
    {
        return environments;
    }

    public void addEnvironment(JsonElement environment,String location) {
        environments.add(environment);
        environment.getAsJsonObject().add("location",new JsonPrimitive(location));
    }

    public JsonObject getJsonObject()
    {
        return mainObject;
    }

    public void addPipeline(JsonElement pipeline,String location) {
        pipelines.add(pipeline);
        pipeline.getAsJsonObject().add("location",new JsonPrimitive(location));
    }

    public JsonArray getPipelines() {
        return pipelines;
    }
}
