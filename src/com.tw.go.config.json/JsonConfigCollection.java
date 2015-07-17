package com.tw.go.config.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JsonConfigCollection {
    private JsonObject mainObject = new JsonObject();
    private JsonArray environments = new JsonArray();
    private JsonArray groups = new JsonArray();

    public JsonConfigCollection()
    {
        mainObject.add("environments",environments);
        mainObject.add("groups",groups);
    }

    protected JsonArray getEnvironments()
    {
        return environments;
    }

    public void addEnvironment(JsonElement environment) {
        environments.add(environment);
    }
    public void addGroup(JsonElement group) {
        groups.add(group);
    }

    protected JsonArray getOrCreateDefaultGroupPipelines()
    {
        return getOrCreateGroupPipelines("");
    }

    protected JsonArray getOrCreateGroupPipelines(String groupName) {
        return getOrCreateGroup(groupName).getAsJsonArray("pipelines");
    }

    public JsonObject getJsonObject()
    {
        return mainObject;
    }

    public void addPipeline(JsonElement pipeline) {
        JsonObject pipelineObject = pipeline.getAsJsonObject();
        JsonPrimitive groupMember = pipelineObject.getAsJsonPrimitive("group");
        if(groupMember == null)
            getOrCreateDefaultGroupPipelines().add(pipeline);
        else
        {
            String groupName = groupMember.getAsString();
            getOrCreateGroupPipelines(groupName).add(pipeline);
        }
    }

    protected JsonObject getOrCreateGroup(String group) {

        JsonObject groupObject = tryFindGroup(group);
        if(groupObject == null)
        {
            groupObject = createGroup(group);
        }

        return groupObject;
    }

    private JsonObject createGroup(String group) {
        JsonObject groupObject;
        groupObject = new JsonObject();
        groupObject.addProperty("name",group);
        JsonArray pipelines = new JsonArray();
        groupObject.add("pipelines",pipelines);
        groups.add(groupObject);
        return groupObject;
    }

    private JsonObject tryFindGroup(String group) {
        JsonObject groupObject = null;
        for(JsonElement e : groups)
        {
            JsonObject asJsonObject = e.getAsJsonObject();
            if(asJsonObject.getAsJsonPrimitive("name").getAsString().equals(group))
            {
                groupObject = asJsonObject;
            }
        }
        return groupObject;
    }
}
