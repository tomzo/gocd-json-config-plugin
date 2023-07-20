package com.tw.go.config.json;

import com.google.gson.*;

import java.util.HashSet;
import java.util.Set;

public class JsonConfigCollection {
    private static final int DEFAULT_VERSION = 1;
    private final Gson gson;

    private JsonObject mainObject = new JsonObject();
    private JsonArray environments = new JsonArray();
    private JsonArray pipelines = new JsonArray();
    private JsonArray errors = new JsonArray();

    public JsonConfigCollection()
    {
        gson = new Gson();

        updateVersionTo(DEFAULT_VERSION);
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

    public JsonArray getErrors() {
        return errors;
    }

    public void addError(PluginError error) {
        errors.add(gson.toJsonTree(error));
    }

    public void updateVersionFromPipelinesAndEnvironments() {
        Set<Integer> uniqueVersions = new HashSet<>();

        for (JsonElement pipeline : pipelines) {
            JsonElement versionElement = pipeline.getAsJsonObject().get("format_version");
            uniqueVersions.add(versionElement == null ? DEFAULT_VERSION : versionElement.getAsInt());
        }

        for (JsonElement environment : environments) {
            JsonElement versionElement = environment.getAsJsonObject().get("format_version");
            uniqueVersions.add(versionElement == null ? DEFAULT_VERSION : versionElement.getAsInt());
        }

        if (uniqueVersions.size() > 1) {
            throw new RuntimeException("Versions across files are not unique. Found versions: " + uniqueVersions + ". There can only be one version across the whole repository.");
        }
        updateVersionTo(uniqueVersions.iterator().hasNext() ? uniqueVersions.iterator().next() : DEFAULT_VERSION);
    }

    private void updateVersionTo(int version) {
        mainObject.remove("target_version");
        mainObject.add("target_version", new JsonPrimitive(version));
    }
}
