package com.tw.go.config.json;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConfigDirectoryParserTest {
    private final File directory = new File("json-plugin-test-dir");
    private ConfigDirectoryParser parser;
    private String pipe1String;
    private String devenvString;

    @BeforeEach
    public void SetUp() throws Exception {
        FileUtils.deleteDirectory(directory);

        parser = new ConfigDirectoryParser(
                new AntDirectoryScanner(), new JsonConfigParser(),
                PluginSettings.DEFAULT_PIPELINE_PATTERN,
                PluginSettings.DEFAULT_ENVIRONMENT_PATTERN);

        FileUtils.forceMkdir(directory);

        Gson gson = new Gson();

        JsonObject pipe1 = new JsonObject();
        pipe1.addProperty("name", "pipe1");

        pipe1String = gson.toJson(pipe1);

        JsonObject devenv = new JsonObject();
        devenv.addProperty("name", "devend");
        devenvString = gson.toJson(devenv);
    }

    @Test
    public void shouldParseEmptyDirectory() throws Exception {
        parser.parseDirectory(directory);
    }

    @Test
    public void shouldThrowWhenDirectoryDoesNotExist() {
        assertThrows(RuntimeException.class, () -> parser.parseDirectory(new File(directory, ".doesNotExist")));
    }

    @Test
    public void shouldAppendPipelineFromDirectory() throws Exception {
        createFileWithContent("pipe1.gopipeline.json", this.pipe1String);
        JsonConfigCollection result = parser.parseDirectory(directory);
        assertThat(result.getPipelines().size(), is(1));
    }

    @Test
    public void shouldAppendErrorsWithLocationWhenInvalidContent() throws Exception {
        createFileWithContent("pipe1.gopipeline.json", this.pipe1String);
        createFileWithContent("pipeBad1.gopipeline.json", "bad pipeline");
        JsonConfigCollection result = parser.parseDirectory(directory);
        assertEquals(1, result.getErrors().size());
        JsonObject response = result.getErrors().get(0).getAsJsonObject();
        assertEquals(pathTo("pipeBad1.gopipeline.json"), response.getAsJsonPrimitive("location").getAsString());
        assertThat(response.getAsJsonPrimitive("message").getAsString(), startsWith("Failed to parse file as JSON: "));

    }

    @Test
    public void shouldAppendAllErrorsWhenManyFilesHaveInvalidContent() throws Exception {
        createFileWithContent("pipe1.gopipeline.json", this.pipe1String);
        createFileWithContent("pipeBad1.gopipeline.json", "bad pipeline");
        createFileWithContent("pipeBad2.gopipeline.json", "bad pipeline 2");

        JsonConfigCollection result = parser.parseDirectory(directory);
        assertThat(result.getErrors().size(), is(2));
    }

    @Test
    public void shouldAppendErrorWhenPipelineFileIsEmpty() throws Exception {
        createFileWithContent("pipe1.gopipeline.json", this.pipe1String);
        createFileWithContent("pipeBad1.gopipeline.json", "");

        JsonConfigCollection result = parser.parseDirectory(directory);
        assertThat(result.getErrors().size(), is(1));

        assertEquals(1, result.getErrors().size());

        JsonObject response = result.getErrors().get(0).getAsJsonObject();
        assertEquals(pathTo("pipeBad1.gopipeline.json"), response.getAsJsonPrimitive("location").getAsString());
        assertEquals("File is empty", response.getAsJsonPrimitive("message").getAsString());
    }

    @Test
    public void shouldAppendErrorWhenPipelineBlockIsEmpty() throws Exception {
        createFileWithContent("pipe1.gopipeline.json", this.pipe1String);
        createFileWithContent("pipeBad1.gopipeline.json", "{}");

        JsonConfigCollection result = parser.parseDirectory(directory);
        assertEquals(1, result.getErrors().size());

        JsonObject response = result.getErrors().get(0).getAsJsonObject();
        assertEquals(pathTo("pipeBad1.gopipeline.json"), response.getAsJsonPrimitive("location").getAsString());
        assertEquals("Definition is empty", response.getAsJsonPrimitive("message").getAsString());
    }

    @Test
    public void shouldAppendEnvironmentFromDirectory() throws Exception {
        createFileWithContent("devenv.goenvironment.json", this.devenvString);
        JsonConfigCollection result = parser.parseDirectory(directory);
        assertThat(result.getEnvironments().size(), is(1));
    }

    @Test
    public void shouldAppendErrorsWithLocationWhenInvalidContentInEnvironment() throws Exception {
        createFileWithContent("devenv.goenvironment.json", this.pipe1String);
        createFileWithContent("badEnv.goenvironment.json", "bad environment");
        JsonConfigCollection result = parser.parseDirectory(directory);
        assertEquals(1, result.getErrors().size());

        JsonObject response = result.getErrors().get(0).getAsJsonObject();
        assertEquals(pathTo("badEnv.goenvironment.json"), response.getAsJsonPrimitive("location").getAsString());
        assertThat(response.getAsJsonPrimitive("message").getAsString(), startsWith("Failed to parse file as JSON: "));
    }

    @Test
    public void shouldAppendAllErrorsWhenManyEnvironmentFilesHaveInvalidContent() throws Exception {
        createFileWithContent("pipe1.gopipeline.json", this.pipe1String);
        createFileWithContent("badEnv.goenvironment.json", "bad env");
        createFileWithContent("badEnv2.goenvironment.json", "bad env 2");
        JsonConfigCollection result = parser.parseDirectory(directory);

        assertEquals(2, result.getErrors().size());
    }

    @Test
    public void shouldAppendErrorWhenEnvironmentFileIsEmpty() throws Exception {
        createFileWithContent("devenv.goenvironment.json", this.devenvString);
        createFileWithContent("badEnv.goenvironment.json", "");

        JsonConfigCollection result = parser.parseDirectory(directory);
        assertEquals(1, result.getErrors().size());

        JsonObject response = result.getErrors().get(0).getAsJsonObject();
        assertEquals(pathTo("badEnv.goenvironment.json"), response.getAsJsonPrimitive("location").getAsString());
        assertEquals("File is empty", response.getAsJsonPrimitive("message").getAsString());
    }

    @Test
    public void shouldThrowErrorWhenEnvironmentBlockIsEmpty() throws Exception {
        createFileWithContent("devenv.goenvironment.json", this.devenvString);
        createFileWithContent("badEnv.goenvironment.json", "{}");

        JsonConfigCollection result = parser.parseDirectory(directory);
        assertEquals(1, result.getErrors().size());

        JsonObject response = result.getErrors().get(0).getAsJsonObject();
        assertEquals(pathTo("badEnv.goenvironment.json"), response.getAsJsonPrimitive("location").getAsString());
        assertEquals("Definition is empty", response.getAsJsonPrimitive("message").getAsString());
    }

    private void createFileWithContent(String filePath, String content) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(pathTo(filePath), "UTF-8");
        writer.println(content);
        writer.close();
    }

    private String pathTo(String child) {
        return new File(directory, child).getPath();
    }
}
