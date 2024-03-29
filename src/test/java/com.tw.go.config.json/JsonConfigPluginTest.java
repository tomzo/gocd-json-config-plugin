package com.tw.go.config.json;

import com.google.gson.*;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.request.DefaultGoPluginApiRequest;
import com.thoughtworks.go.plugin.api.request.GoApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoApiResponse;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

import static com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE;
import static com.tw.go.config.json.ConfigRepoMessages.REQ_PARSE_CONTENT;
import static com.tw.go.config.json.ConfigRepoMessages.REQ_PLUGIN_SETTINGS_CHANGED;
import static com.tw.go.config.json.PluginSettings.*;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class JsonConfigPluginTest {
    private final Path emptyDir = Path.of("emptyDir");
    @TempDir
    public Path tempDir;

    private JsonConfigPlugin plugin;
    private Gson gson;

    private GoApplicationAccessor goAccessor;

    @BeforeEach
    public void setUp() throws IOException {
        plugin = new JsonConfigPlugin();
        goAccessor = mock(GoApplicationAccessor.class);
        plugin.initializeGoApplicationAccessor(goAccessor);
        GoApiResponse settingsResponse = DefaultGoApiResponse.success("{}");
        when(goAccessor.submit(any())).thenReturn(settingsResponse);
        gson = new Gson();
        Files.createDirectory(emptyDir);
    }

    @AfterEach
    public void tearDown() throws IOException {
        if (emptyDir.toFile().exists()) {
            Files.walk(emptyDir).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Test
    public void respondsToParseContentRequest() throws Exception {
        final Gson gson = new Gson();
        DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest("configrepo", "2.0", REQ_PARSE_CONTENT);

        HashMap<String, String> contents = new HashMap<>();
        contents.put("foo.gopipeline.json", "{\"name\": \"a\", \"stages\":[]}");
        contents.put("foo.goenvironment.json", "{\"name\": \"b\"}");
        request.setRequestBody(gson.toJson(Collections.singletonMap("contents", contents)));

        GoPluginApiResponse response = plugin.handle(request);
        JsonObject jsonObjectFromResponse = getJsonObjectFromResponse(response);
        assertEquals(SUCCESS_RESPONSE_CODE, response.responseCode());
        assertEquals(new JsonArray(), jsonObjectFromResponse.get("errors"));

        JsonObject expected = new JsonObject();

        JsonArray pipelines = new JsonArray();
        JsonArray envs = new JsonArray();

        expected.add("errors", new JsonArray());
        expected.add("environments", envs);
        expected.add("pipelines", pipelines);
        expected.addProperty("target_version", 1);

        JsonObject p1 = new JsonObject();
        p1.addProperty("name", "a");
        p1.add("stages", new JsonArray());
        p1.addProperty("location", "foo.gopipeline.json");
        pipelines.add(p1);

        JsonObject e1 = new JsonObject();
        e1.addProperty("name", "b");
        e1.addProperty("location", "foo.goenvironment.json");
        envs.add(e1);

        assertEquals(expected, jsonObjectFromResponse);
    }

    @Test
    public void shouldRespondSuccessToGetConfigurationRequest() throws UnhandledRequestTypeException {
        DefaultGoPluginApiRequest getConfigRequest = new DefaultGoPluginApiRequest("configrepo", "1.0", "go.plugin-settings.get-configuration");

        GoPluginApiResponse response = plugin.handle(getConfigRequest);
        assertThat(response.responseCode(), is(SUCCESS_RESPONSE_CODE));
    }

    @Test
    public void shouldConsumePluginSettingsOnConfigChangeRequest() throws UnhandledRequestTypeException {
        DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest("configrepo", "2.0", REQ_PLUGIN_SETTINGS_CHANGED);
        HashMap<String, String> body = new HashMap<>();
        body.put(PLUGIN_SETTINGS_PIPELINE_PATTERN, "*.foo.pipes.json");
        body.put(PLUGIN_SETTINGS_ENVIRONMENT_PATTERN, "*.foo.envs.json");
        request.setRequestBody(JSONUtils.toJSON(body));

        DefaultGoPluginApiRequest parseDirectoryRequest = new DefaultGoPluginApiRequest("configrepo", "1.0", "parse-directory");
        String requestBody = "{\n" +
                "    \"directory\":\"emptyDir\",\n" +
                "    \"configurations\":[]\n" +
                "}";
        parseDirectoryRequest.setRequestBody(requestBody);
        ParsedRequest parsed = ParsedRequest.parse(parseDirectoryRequest);

        assertEquals(DEFAULT_PIPELINE_PATTERN, plugin.getPipelinePattern(parsed));
        assertEquals(DEFAULT_ENVIRONMENT_PATTERN, plugin.getEnvironmentPattern(parsed));

        GoPluginApiResponse response = plugin.handle(request);

        assertThat(response.responseCode(), is(SUCCESS_RESPONSE_CODE));
        assertEquals("*.foo.pipes.json", plugin.getPipelinePattern(parsed));
        assertEquals("*.foo.envs.json", plugin.getEnvironmentPattern(parsed));
    }


    @Test
    public void shouldContainEnvironmentPatternInResponseToGetConfigurationRequest() throws UnhandledRequestTypeException {
        DefaultGoPluginApiRequest getConfigRequest = new DefaultGoPluginApiRequest("configrepo", "1.0", "go.plugin-settings.get-configuration");

        GoPluginApiResponse response = plugin.handle(getConfigRequest);
        assertThat(response.responseCode(), is(SUCCESS_RESPONSE_CODE));
        JsonObject responseJsonObject = getJsonObjectFromResponse(response);
        JsonElement environmentPatternConfig = responseJsonObject.get("environment_pattern");
        assertNotNull(environmentPatternConfig);
        JsonObject environmentPatternConfigAsJsonObject = environmentPatternConfig.getAsJsonObject();
        assertThat(environmentPatternConfigAsJsonObject.get("display-name").getAsString(), is("Go environment files pattern"));
        assertThat(environmentPatternConfigAsJsonObject.get("default-value").getAsString(), is("**/*.goenvironment.json"));
        assertThat(environmentPatternConfigAsJsonObject.get("required").getAsBoolean(), is(false));
        assertThat(environmentPatternConfigAsJsonObject.get("secure").getAsBoolean(), is(false));
        assertThat(environmentPatternConfigAsJsonObject.get("display-order").getAsInt(), is(1));
    }

    @Test
    public void shouldContainPipelinePatternInResponseToGetConfigurationRequest() throws UnhandledRequestTypeException {
        DefaultGoPluginApiRequest getConfigRequest = new DefaultGoPluginApiRequest("configrepo", "1.0", "go.plugin-settings.get-configuration");

        GoPluginApiResponse response = plugin.handle(getConfigRequest);
        assertThat(response.responseCode(), is(SUCCESS_RESPONSE_CODE));
        JsonObject responseJsonObject = getJsonObjectFromResponse(response);
        JsonElement pipelinePatternConfig = responseJsonObject.get("pipeline_pattern");
        assertNotNull(pipelinePatternConfig);
        JsonObject pipelinePatternConfigAsJsonObject = pipelinePatternConfig.getAsJsonObject();
        assertThat(pipelinePatternConfigAsJsonObject.get("display-name").getAsString(), is("Go pipeline files pattern"));
        assertThat(pipelinePatternConfigAsJsonObject.get("default-value").getAsString(), is("**/*.gopipeline.json"));
        assertThat(pipelinePatternConfigAsJsonObject.get("required").getAsBoolean(), is(false));
        assertThat(pipelinePatternConfigAsJsonObject.get("secure").getAsBoolean(), is(false));
        assertThat(pipelinePatternConfigAsJsonObject.get("display-order").getAsInt(), is(0));
    }

    @Test
    public void getPipelinePatternShouldReturnValueAtConfigRepoLevelIfDefined() {
        DefaultGoPluginApiRequest parseDirectoryRequest = new DefaultGoPluginApiRequest("configrepo", "1.0", "parse-directory");
        String requestBody = "{\n" +
                "    \"directory\":\"emptyDir\",\n" +
                "    \"configurations\":[" +
                        "{" +
                            "\"key\" : \"pipeline_pattern\"," +
                            "\"value\" : \"**/*.goprodpipeline.json\" " +
                        "}" +
                    "]\n" +
                "}";
        parseDirectoryRequest.setRequestBody(requestBody);
        ParsedRequest parsed = ParsedRequest.parse(parseDirectoryRequest);
        String pattern = plugin.getPipelinePattern(parsed);
        assertThat(pattern, is("**/*.goprodpipeline.json"));
    }

    @Test
    public void getEnvironmentPatternShouldReturnValueAtConfigRepoLevelIfDefined() {
        DefaultGoPluginApiRequest parseDirectoryRequest = new DefaultGoPluginApiRequest("configrepo", "1.0", "parse-directory");
        String requestBody = "{\n" +
                "    \"directory\":\"emptyDir\",\n" +
                "    \"configurations\":[" +
                        "{" +
                            "\"key\" : \"environment_pattern\"," +
                            "\"value\" : \"**/*.goprodenvironment.json\" " +
                        "}" +
                    "]\n" +
                "}";
        parseDirectoryRequest.setRequestBody(requestBody);
        ParsedRequest parsed = ParsedRequest.parse(parseDirectoryRequest);
        String pattern = plugin.getEnvironmentPattern(parsed);
        assertThat(pattern, is("**/*.goprodenvironment.json"));
    }

    @Test
    public void getEnvironmentPatternShouldReturnValueAtConfigRepoLevelIfBothPatternsDefined() {
        DefaultGoPluginApiRequest parseDirectoryRequest = new DefaultGoPluginApiRequest("configrepo", "1.0", "parse-directory");
        String requestBody = "{\n" +
                "    \"directory\":\"emptyDir\",\n" +
                "    \"configurations\":[" +
                    "{" +
                        "\"key\" : \"environment_pattern\"," +
                        "\"value\" : \"**/*.goprodenvironment.json\" " +
                    "}," +
                    "{" +
                        "\"key\" : \"pipeline_pattern\"," +
                        "\"value\" : \"**/*.goprodpipeline.json\" " +
                    "}" +
                "]\n" +
                "}";
        parseDirectoryRequest.setRequestBody(requestBody);
        ParsedRequest parsed = ParsedRequest.parse(parseDirectoryRequest);
        String pattern = plugin.getEnvironmentPattern(parsed);
        assertThat(pattern, is("**/*.goprodenvironment.json"));
    }

    private JsonObject getJsonObjectFromResponse(GoPluginApiResponse response) {
        String responseBody = response.responseBody();
        return JsonParser.parseString(responseBody).getAsJsonObject();
    }

    @Test
    public void shouldRespondSuccessToGetViewRequest() throws UnhandledRequestTypeException {
        DefaultGoPluginApiRequest getConfigRequest = new DefaultGoPluginApiRequest("configrepo", "1.0", "go.plugin-settings.get-view");

        GoPluginApiResponse response = plugin.handle(getConfigRequest);
        assertThat(response.responseCode(), is(SUCCESS_RESPONSE_CODE));
    }

    @Test
    public void shouldRespondSuccessToValidateConfigRequest() throws UnhandledRequestTypeException {
        DefaultGoPluginApiRequest validateRequest = new DefaultGoPluginApiRequest("configrepo", "1.0", "go.plugin-settings.validate-configuration");

        GoPluginApiResponse response = plugin.handle(validateRequest);
        assertThat(response.responseCode(), is(SUCCESS_RESPONSE_CODE));
    }

    @Test
    public void shouldRespondWithGetIcon() throws UnhandledRequestTypeException, IOException {
        DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest("configrepo", "2.0", "get-icon");

        GoPluginApiResponse response = plugin.handle(request);
        assertThat(response.responseCode(), is(SUCCESS_RESPONSE_CODE));
        JsonObject jsonObject = getJsonObjectFromResponse(response);
        assertEquals(jsonObject.entrySet().size(), 2);
        assertEquals(jsonObject.get("content_type").getAsString(), "image/svg+xml");
        byte[] actualData = Base64.getDecoder().decode(jsonObject.get("data").getAsString());
        try (InputStream inputStream = Objects.requireNonNull(getClass().getResourceAsStream("/json.svg"))) {
            assertArrayEquals(inputStream.readAllBytes(), actualData);
        }
    }

    @Test
    public void shouldRespondSuccessToParseDirectoryRequestWhenEmpty() throws UnhandledRequestTypeException {
        DefaultGoPluginApiRequest parseDirectoryRequest = new DefaultGoPluginApiRequest("configrepo", "1.0", "parse-directory");
        String requestBody = "{\n" +
                "    \"directory\":\"emptyDir\",\n" +
                "    \"configurations\":[]\n" +
                "}";
        parseDirectoryRequest.setRequestBody(requestBody);

        GoPluginApiResponse response = plugin.handle(parseDirectoryRequest);
        assertThat(response.responseCode(), is(SUCCESS_RESPONSE_CODE));
        JsonObject responseJsonObject = getJsonObjectFromResponse(response);
        assertNull(responseJsonObject.get("pluginErrors"));
    }

    @Test
    public void shouldRespondBadRequestToParseDirectoryRequestWhenDirectoryIsNotSpecified() throws UnhandledRequestTypeException {
        DefaultGoPluginApiRequest parseDirectoryRequest = new DefaultGoPluginApiRequest("configrepo", "1.0", "parse-directory");
        String requestBody = "{\n" +
                "    \"configurations\":[]\n" +
                "}";
        parseDirectoryRequest.setRequestBody(requestBody);

        GoPluginApiResponse response = plugin.handle(parseDirectoryRequest);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.BAD_REQUEST));
    }

    @Test
    public void shouldRespondBadRequestToParseDirectoryRequestWhenRequestBodyIsNull() throws UnhandledRequestTypeException {
        DefaultGoPluginApiRequest parseDirectoryRequest = new DefaultGoPluginApiRequest("configrepo", "1.0", "parse-directory");
        parseDirectoryRequest.setRequestBody(null);

        GoPluginApiResponse response = plugin.handle(parseDirectoryRequest);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.BAD_REQUEST));
    }

    @Test
    public void shouldRespondBadRequestToParseDirectoryRequestWhenRequestBodyIsEmpty() throws UnhandledRequestTypeException {
        DefaultGoPluginApiRequest parseDirectoryRequest = new DefaultGoPluginApiRequest("configrepo", "1.0", "parse-directory");
        parseDirectoryRequest.setRequestBody("{}");

        GoPluginApiResponse response = plugin.handle(parseDirectoryRequest);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.BAD_REQUEST));
    }

    @Test
    public void shouldRespondBadRequestToParseDirectoryRequestWhenRequestBodyIsNotJson() throws UnhandledRequestTypeException {
        DefaultGoPluginApiRequest parseDirectoryRequest = new DefaultGoPluginApiRequest("configrepo", "1.0", "parse-directory");
        parseDirectoryRequest.setRequestBody("{bla");

        GoPluginApiResponse response = plugin.handle(parseDirectoryRequest);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.BAD_REQUEST));
    }

    @Test
    public void shouldTalkToGoApplicationAccessorToGetPluginSettings() throws UnhandledRequestTypeException {
        DefaultGoPluginApiRequest parseDirectoryRequest = new DefaultGoPluginApiRequest("configrepo", "1.0", "parse-directory");
        String requestBody = "{\n" +
                "    \"directory\":\"emptyDir\",\n" +
                "    \"configurations\":[]\n" +
                "}";
        parseDirectoryRequest.setRequestBody(requestBody);

        GoPluginApiResponse response = plugin.handle(parseDirectoryRequest);

        verify(goAccessor, times(1)).submit(any(GoApiRequest.class));
        assertThat(response.responseCode(), is(SUCCESS_RESPONSE_CODE));
    }

    @Test
    public void shouldReturnListOfConfigFiles() throws UnhandledRequestTypeException, IOException {
        DefaultGoPluginApiRequest listConfigFilesRequest = new DefaultGoPluginApiRequest("configrepo", "1.0", "config-files");
        Files.createFile(tempDir.resolve("test.gopipeline.json"));
        Files.createFile(tempDir.resolve("test.goenvironment.json"));
        String requestBody = "{\n" +
                "    \"directory\":\"" + tempDir.toFile().getAbsolutePath() + "\"\n" +
                "}";
        listConfigFilesRequest.setRequestBody(requestBody);

        GoPluginApiResponse response = plugin.handle(listConfigFilesRequest);

        verify(goAccessor, times(1)).submit(any(GoApiRequest.class));
        assertThat(response.responseCode(), is(SUCCESS_RESPONSE_CODE));
        assertThat(response.responseBody(), is("{\"files\":[\"test.gopipeline.json\",\"test.goenvironment.json\"]}"));
    }

    @Test
    public void shouldReturnEmptyListWhenNoConfigFiles() throws UnhandledRequestTypeException {
        DefaultGoPluginApiRequest listConfigFilesRequest = new DefaultGoPluginApiRequest("configrepo", "1.0", "config-files");
        String requestBody = "{\n" +
                "    \"directory\":\"" + tempDir.toFile().getAbsolutePath() + "\"\n" +
                "}";
        listConfigFilesRequest.setRequestBody(requestBody);

        GoPluginApiResponse response = plugin.handle(listConfigFilesRequest);

        verify(goAccessor, times(1)).submit(any(GoApiRequest.class));
        assertThat(response.responseCode(), is(SUCCESS_RESPONSE_CODE));
        assertThat(response.responseBody(), is("{\"files\":[]}"));
    }

    @Test
    public void shouldGiveBackPipelineJSONForPipelineExport() throws UnhandledRequestTypeException {
        HashMap<String, Object> pipeline = new HashMap<>();
        pipeline.put("name", "pipeline");
        pipeline.put("group", "group");
        pipeline.put("stages", Collections.emptyList());

        Gson gson = new Gson();
        String pipelineJson = gson.toJson(pipeline);

        String requestJson = format("{\"pipeline\": %s}", pipelineJson);
        DefaultGoPluginApiRequest pipelineExportRequest = new DefaultGoPluginApiRequest("configrepo", "2.0", "pipeline-export");
        pipelineExportRequest.setRequestBody(requestJson);

        GoPluginApiResponse response = plugin.handle(pipelineExportRequest);

        assertThat(response.responseCode(), is(SUCCESS_RESPONSE_CODE));
        Gson pretty = new GsonBuilder().setPrettyPrinting().create();
        String prettyPrinted = pretty.toJson(pipeline);
        assertThat(response.responseBody(), is(gson.toJson(Collections.singletonMap("pipeline", prettyPrinted))));
    }

    @Test
    public void shouldRespondWithCapabilities() throws UnhandledRequestTypeException {
        String expected = gson.toJson(new Capabilities());
        DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest("configrepo", "2.0", "get-capabilities");

        GoPluginApiResponse response = plugin.handle(request);

        assertThat(response.responseCode(), is(SUCCESS_RESPONSE_CODE));
        assertThat(response.responseBody(), is(expected));
    }

    @Test
    public void shouldRespondSuccessToParseDirectoryRequestWhenPluginHasConfiguration() throws UnhandledRequestTypeException {
        GoApiResponse settingsResponse = DefaultGoApiResponse.success("{}");
        when(goAccessor.submit(any(GoApiRequest.class))).thenReturn(settingsResponse);

        DefaultGoPluginApiRequest parseDirectoryRequest = new DefaultGoPluginApiRequest("configrepo", "1.0", "parse-directory");
        String requestBody = "{\n" +
                "    \"directory\":\"emptyDir\",\n" +
                "    \"configurations\":[]\n" +
                "}";
        parseDirectoryRequest.setRequestBody(requestBody);

        GoPluginApiResponse response = plugin.handle(parseDirectoryRequest);

        verify(goAccessor, times(1)).submit(any(GoApiRequest.class));
        assertThat(response.responseCode(), is(SUCCESS_RESPONSE_CODE));
    }

    @Test
    public void shouldContainValidFieldsInResponseMessage() throws UnhandledRequestTypeException {
        GoApiResponse settingsResponse = DefaultGoApiResponse.success("{}");
        when(goAccessor.submit(any(GoApiRequest.class))).thenReturn(settingsResponse);

        DefaultGoPluginApiRequest parseDirectoryRequest = new DefaultGoPluginApiRequest("configrepo", "1.0", "parse-directory");
        String requestBody = "{\n" +
                "    \"directory\":\"emptyDir\",\n" +
                "    \"configurations\":[]\n" +
                "}";
        parseDirectoryRequest.setRequestBody(requestBody);

        GoPluginApiResponse response = plugin.handle(parseDirectoryRequest);

        assertThat(response.responseCode(), is(SUCCESS_RESPONSE_CODE));
        JsonElement responseObj = JsonParser.parseString(response.responseBody());
        assertTrue(responseObj.isJsonObject());
        JsonObject obj = responseObj.getAsJsonObject();
        assertTrue(obj.has("errors"));
        assertTrue(obj.has("pipelines"));
        assertTrue(obj.has("environments"));
        assertTrue(obj.has("target_version"));
    }
}
