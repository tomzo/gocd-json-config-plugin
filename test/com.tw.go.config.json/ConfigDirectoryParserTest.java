package com.tw.go.config.json;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ConfigDirectoryParserTest {

    private File directory = new File("json-plugin-test-dir");
    private ConfigDirectoryParser parser;
    private Gson gson;
    private String pipe1String;
    private String devenvString;

    @Before
    public void SetUp() throws Exception
    {
        FileUtils.deleteDirectory(directory);

        parser = new ConfigDirectoryParser(
                new AntDirectoryScanner(), new JsonFileParser(),
                JsonConfigPlugin.DEFAULT_PIPELINE_PATTERN,
                JsonConfigPlugin.DEFAULT_ENVIRONMENT_PATTERN);

        FileUtils.forceMkdir(directory);

        gson = new Gson();

        JsonObject pipe1 = new JsonObject();
        pipe1.addProperty("name","pipe1");

        pipe1String = gson.toJson(pipe1);

        JsonObject devenv = new JsonObject();
        devenv.addProperty("name","devend");
        devenvString = gson.toJson(devenv);
    }

    @Test
    public void shouldParseEmptyDirectory() throws Exception {
        JsonConfigCollection result = parser.parseDirectory(directory);
    }

    @Test
    public void shouldThrowWhenDirectoryDoesNotExist() throws Exception {
        try {
            JsonConfigCollection result = parser.parseDirectory(new File(directory, ".doesNotExist"));
            fail("should have thrown");
        }
        catch (Exception ex)
        {
         //good
        }
    }

    @Test
    public void shouldAppendPipelineFromDirectory() throws Exception
    {
        createFileWithContent("pipe1.gopipeline.json", this.pipe1String);
        JsonConfigCollection result = parser.parseDirectory(directory);
        assertThat(result.getPipelines().size(), is(1));
    }
    @Test
    public void shouldThrowErrorsWithLocationWhenInvalidContent() throws Exception
    {
        createFileWithContent("pipe1.gopipeline.json", this.pipe1String);
        createFileWithContent("pipeBad1.gopipeline.json", "bad pipeline");
        try {
            parser.parseDirectory(directory);
            fail("should have thrown");
        }
        catch (ConfigDirectoryParseException parseException)
        {
            assertThat(parseException.getErrors().size(),is(1));
            assertThat(parseException.getErrors().get(0).getLocation(), is("pipeBad1.gopipeline.json"));
            assertThat(parseException.getErrors().get(0).getMessage(), startsWith("Failed to parse pipeline file as JSON: "));
        }
    }
    @Test
    public void shouldThrowAllErrorsWhenManyFilesHaveInvalidContent() throws Exception
    {
        createFileWithContent("pipe1.gopipeline.json", this.pipe1String);
        createFileWithContent("pipeBad1.gopipeline.json", "bad pipeline");
        createFileWithContent("pipeBad2.gopipeline.json", "bad pipeline 2");
        try {
            parser.parseDirectory(directory);
            fail("should have thrown");
        }
        catch (ConfigDirectoryParseException parseException)
        {
            assertThat(parseException.getErrors().size(),is(2));
        }
    }
    @Test
    public void shouldThrowErrorWhenPipelineFileIsEmpty() throws Exception
    {
        createFileWithContent("pipe1.gopipeline.json", this.pipe1String);
        createFileWithContent("pipeBad1.gopipeline.json", "");
        try {
            parser.parseDirectory(directory);
            fail("should have thrown");
        }
        catch (ConfigDirectoryParseException parseException)
        {
            assertThat(parseException.getErrors().size(),is(1));
            assertThat(parseException.getErrors().get(0).getLocation(), is("pipeBad1.gopipeline.json"));
            assertThat(parseException.getErrors().get(0).getMessage(), is("Pipeline file is empty"));
        }
    }
    @Test
    public void shouldThrowErrorWhenPipelineBlockIsEmpty() throws Exception
    {
        createFileWithContent("pipe1.gopipeline.json", this.pipe1String);
        createFileWithContent("pipeBad1.gopipeline.json", "{}");
        try {
            parser.parseDirectory(directory);
            fail("should have thrown");
        }
        catch (ConfigDirectoryParseException parseException)
        {
            assertThat(parseException.getErrors().size(),is(1));
            assertThat(parseException.getErrors().get(0).getLocation(), is("pipeBad1.gopipeline.json"));
            assertThat(parseException.getErrors().get(0).getMessage(), is("Pipeline definition is empty"));
        }
    }


    @Test
    public void shouldAppendEnvironmentFromDirectory() throws Exception
    {
        createFileWithContent("devenv.goenvironment.json", this.devenvString);
        JsonConfigCollection result = parser.parseDirectory(directory);
        assertThat(result.getEnvironments().size(), is(1));
    }
    @Test
    public void shouldThrowErrorsWithLocationWhenInvalidContentInEnvironment() throws Exception
    {
        createFileWithContent("devenv.goenvironment.json", this.pipe1String);
        createFileWithContent("badEnv.goenvironment.json", "bad environment");
        try {
            parser.parseDirectory(directory);
            fail("should have thrown");
        }
        catch (ConfigDirectoryParseException parseException)
        {
            assertThat(parseException.getErrors().size(),is(1));
            assertThat(parseException.getErrors().get(0).getLocation(), is("badEnv.goenvironment.json"));
            assertThat(parseException.getErrors().get(0).getMessage(), startsWith("Failed to parse environment file as JSON: "));
        }
    }
    @Test
    public void shouldThrowAllErrorsWhenManyEnvironmentFilesHaveInvalidContent() throws Exception
    {
        createFileWithContent("pipe1.gopipeline.json", this.pipe1String);
        createFileWithContent("badEnv.goenvironment.json", "bad env");
        createFileWithContent("badEnv2.goenvironment.json", "bad env 2");
        try {
            parser.parseDirectory(directory);
            fail("should have thrown");
        }
        catch (ConfigDirectoryParseException parseException)
        {
            assertThat(parseException.getErrors().size(),is(2));
        }
    }
    @Test
    public void shouldThrowErrorWhenEnvironmentFileIsEmpty() throws Exception
    {
        createFileWithContent("devenv.goenvironment.json", this.devenvString);
        createFileWithContent("badEnv.goenvironment.json", "");
        try {
            parser.parseDirectory(directory);
            fail("should have thrown");
        }
        catch (ConfigDirectoryParseException parseException)
        {
            assertThat(parseException.getErrors().size(),is(1));
            assertThat(parseException.getErrors().get(0).getLocation(), is("badEnv.goenvironment.json"));
            assertThat(parseException.getErrors().get(0).getMessage(), is("Environment file is empty"));
        }
    }
    @Test
    public void shouldThrowErrorWhenEnvironmentBlockIsEmpty() throws Exception
    {
        createFileWithContent("devenv.goenvironment.json", this.devenvString);
        createFileWithContent("badEnv.goenvironment.json", "{}");
        try {
            parser.parseDirectory(directory);
            fail("should have thrown");
        }
        catch (ConfigDirectoryParseException parseException)
        {
            assertThat(parseException.getErrors().size(),is(1));
            assertThat(parseException.getErrors().get(0).getLocation(), is("badEnv.goenvironment.json"));
            assertThat(parseException.getErrors().get(0).getMessage(), is("Environment definition is empty"));
        }
    }

    private void createFileWithContent(String filePath, String content) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(new File(directory, filePath), "UTF-8");
        writer.println(content);
        writer.close();
    }
}
