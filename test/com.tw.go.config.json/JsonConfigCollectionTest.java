package com.tw.go.config.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

public class JsonConfigCollectionTest {

    private JsonConfigCollection jsonCollection;
    private JsonObject pipe1;
    private JsonObject pipe2;
    private JsonObject devEnv;

    @Before
    public void SetUp()
    {
        jsonCollection = new JsonConfigCollection();

        pipe1 = new JsonObject();
        pipe1.addProperty("name","pipe1");

        pipe2 = new JsonObject();
        pipe2.addProperty("name","pipe2");

        devEnv = new JsonObject();
        devEnv.addProperty("name","dev");
    }

    @Test
    public void shouldReturnEnvironmentsArrayInJsonObjectWhenEmpty()
    {
        JsonObject jsonObject = jsonCollection.getJsonObject();
        assertThat(jsonObject.get("environments") instanceof JsonArray,is(true));
        assertThat(jsonObject.getAsJsonArray("environments"), is(new JsonArray()));
    }

    @Test
    public void shouldReturnGroupsArrayInJsonObjectWhenEmpty()
    {
        JsonObject jsonObject = jsonCollection.getJsonObject();
        assertThat(jsonObject.get("groups") instanceof JsonArray,is(true));
        assertThat(jsonObject.getAsJsonArray("groups"), is(new JsonArray()));
    }

    @Test
    public void shouldAddPipelinesToDefaultGroup()
    {
        jsonCollection.addPipeline(pipe1);
        assertThat(jsonCollection.getOrCreateDefaultGroupPipelines().size(), is(1));
    }
    @Test
    public void shouldAppendDefaultGroupToGroupsInJsonObject()
    {
        jsonCollection.addPipeline(pipe1);
        JsonObject jsonObject = jsonCollection.getJsonObject();
        assertThat(jsonObject.getAsJsonArray("groups").size(),is(1));
    }
    @Test
    public void shouldAppendOnlyOnceDefaultGroupToGroupsInJsonObjectWhen2PipelinesAdded()
    {
        jsonCollection.addPipeline(pipe1);
        jsonCollection.addPipeline(pipe2);
        JsonObject jsonObject = jsonCollection.getJsonObject();
        assertThat(jsonObject.getAsJsonArray("groups").size(),is(1));
    }
    
    @Test
    public void shouldReturnPipelinesInDefaultGroupInJsonObject()
    {
        jsonCollection.addPipeline(pipe1);
        jsonCollection.addPipeline(pipe2);
        JsonObject jsonObject = jsonCollection.getJsonObject();

        JsonArray pipelinesInDefault = jsonObject.getAsJsonArray("groups").get(0).getAsJsonObject().getAsJsonArray("pipelines");
        assertThat(pipelinesInDefault.size(),is(2));
        assertThat(pipelinesInDefault,hasItem(pipe1));
        assertThat(pipelinesInDefault,hasItem(pipe2));
    }

    @Test
    public void shouldReturnEnvironmentsInJsonObject()
    {
        jsonCollection.addEnvironment(devEnv);
        JsonObject jsonObject = jsonCollection.getJsonObject();
        assertThat(jsonObject.getAsJsonArray("environments").size(),is(1));
    }
}
