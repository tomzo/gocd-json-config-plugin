package com.tw.go.config.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

public class JsonConfigCollectionTest {

    private JsonConfigCollection jsonCollection;
    private JsonObject pipe1;
    private JsonObject pipe2;
    private JsonObject devEnv;
    private JsonObject pipeInGroup;

    @Before
    public void setUp() {
        jsonCollection = new JsonConfigCollection();

        pipe1 = new JsonObject();
        pipe1.addProperty("name", "pipe1");

        pipe2 = new JsonObject();
        pipe2.addProperty("name", "pipe2");

        pipeInGroup = new JsonObject();
        pipeInGroup.addProperty("name", "pipe3");
        pipeInGroup.addProperty("group", "mygroup");

        devEnv = new JsonObject();
        devEnv.addProperty("name", "dev");
    }

    @Test
    public void shouldReturnDefaultTargetVersionWhenThereAreNoPipelinesOrEnvironmentsDefined() {
        jsonCollection.updateVersionFromPipelinesAndEnvironments();
        JsonObject jsonObject = jsonCollection.getJsonObject();
        assertTargetVersion(jsonObject, 1);
    }

    @Test
    public void shouldReturnEnvironmentsArrayInJsonObjectWhenEmpty() {
        JsonObject jsonObject = jsonCollection.getJsonObject();
        assertThat(jsonObject.get("environments") instanceof JsonArray, is(true));
        assertThat(jsonObject.getAsJsonArray("environments"), is(new JsonArray()));
    }

    @Test
    public void shouldAppendPipelinesToPipelinesCollection() {
        jsonCollection.addPipeline(pipe1, "pipe1.json");
        jsonCollection.addPipeline(pipe2, "pipe2.json");
        JsonObject jsonObject = jsonCollection.getJsonObject();
        assertThat(jsonObject.getAsJsonArray("pipelines").size(), is(2));
    }


    @Test
    public void shouldReturnEnvironmentsInJsonObject() {
        jsonCollection.addEnvironment(devEnv, "dev.json");
        JsonObject jsonObject = jsonCollection.getJsonObject();
        assertThat(jsonObject.getAsJsonArray("environments").size(), is(1));
    }

    @Test
    public void shouldUpdateTargetVersionWhenItIsTheSameAcrossAllPipelinesAndEnvironments() {
        jsonCollection.addPipeline(pipelineWithVersion(2), "pipe1.json");
        jsonCollection.addPipeline(pipelineWithVersion(2), "pipe2.json");
        jsonCollection.addEnvironment(envWithVersion(2), "env1.json");
        jsonCollection.addEnvironment(envWithVersion(2), "env2.json");

        jsonCollection.updateVersionFromPipelinesAndEnvironments();
        JsonObject jsonObject = jsonCollection.getJsonObject();

        assertTargetVersion(jsonObject, 2);
    }

    @Test
    public void shouldUpdateTargetVersionWhenItIsTheDefaultOrMissingAcrossAllPipelinesAndEnvironments() {
        int defaultVersionExpected = 1;

        jsonCollection.addPipeline(new JsonObject(), "pipe1.json");
        jsonCollection.addPipeline(pipelineWithVersion(defaultVersionExpected), "pipe2.json");
        jsonCollection.addEnvironment(new JsonObject(), "env1.json");
        jsonCollection.addEnvironment(envWithVersion(defaultVersionExpected), "env2.json");

        jsonCollection.updateVersionFromPipelinesAndEnvironments();
        JsonObject jsonObject = jsonCollection.getJsonObject();

        assertTargetVersion(jsonObject, defaultVersionExpected);
    }

    @Test
    public void shouldFailToUpdateTargetVersionWhenItIs_NOT_TheSameAcrossAllPipelinesAndEnvironments() {
        jsonCollection.addPipeline(pipelineWithVersion(1), "pipe1.json");
        jsonCollection.addPipeline(pipelineWithVersion(2), "pipe2.json");
        jsonCollection.addEnvironment(envWithVersion(1), "env1.json");
        jsonCollection.addEnvironment(new JsonObject(), "env2.json");

        try {
            jsonCollection.updateVersionFromPipelinesAndEnvironments();
            fail("Should have failed to find a unique version");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), containsString("Versions across files are not unique"));
        }
    }

    private JsonElement envWithVersion(int version) {
        return pipelineWithVersion(version);
    }

    private JsonElement pipelineWithVersion(int version) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("format_version", version);
        return jsonObject;
    }

    private void assertTargetVersion(JsonObject jsonObject, int expectedVersion) {
        assertThat(jsonObject.get("target_version") instanceof JsonPrimitive, is(true));
        assertThat(jsonObject.getAsJsonPrimitive("target_version").getAsInt(), is(expectedVersion));
    }
}
