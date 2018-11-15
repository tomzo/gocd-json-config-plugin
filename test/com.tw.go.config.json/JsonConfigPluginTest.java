package com.tw.go.config.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.request.DefaultGoPluginApiRequest;
import com.thoughtworks.go.plugin.api.request.GoApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoApiResponse;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

import static java.lang.String.format;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNull;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class JsonConfigPluginTest {

    private JsonConfigPlugin plugin;
    private Gson gson;
    private JsonParser parser;
    private GoApplicationAccessor goAccessor;

    @Before
    public void SetUp() throws IOException {
        plugin = new JsonConfigPlugin();
        goAccessor = mock(GoApplicationAccessor.class);
        plugin.initializeGoApplicationAccessor(goAccessor);
        GoApiResponse settingsResponse = DefaultGoApiResponse.success("{}");
        when(goAccessor.submit(any(GoApiRequest.class))).thenReturn(settingsResponse);
        gson = new Gson();
        parser = new JsonParser();

        File emptyDir = new File("emptyDir");
        FileUtils.deleteDirectory(emptyDir);
        FileUtils.forceMkdir(emptyDir);
    }


    @Test
    public void shouldRespondSuccessToGetConfigurationRequest() throws UnhandledRequestTypeException {
        DefaultGoPluginApiRequest getConfigRequest = new DefaultGoPluginApiRequest("configrepo", "1.0", "go.plugin-settings.get-configuration");

        GoPluginApiResponse response = plugin.handle(getConfigRequest);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
    }

    @Test
    public void shouldContainEnvironmentPatternInResponseToGetConfigurationRequest() throws UnhandledRequestTypeException {
        DefaultGoPluginApiRequest getConfigRequest = new DefaultGoPluginApiRequest("configrepo", "1.0", "go.plugin-settings.get-configuration");

        GoPluginApiResponse response = plugin.handle(getConfigRequest);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
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
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
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

    private JsonObject getJsonObjectFromResponse(GoPluginApiResponse response) {
        String responseBody = response.responseBody();
        return parser.parse(responseBody).getAsJsonObject();
    }

    @Test
    public void shouldRespondSuccessToGetViewRequest() throws UnhandledRequestTypeException {
        DefaultGoPluginApiRequest getConfigRequest = new DefaultGoPluginApiRequest("configrepo", "1.0", "go.plugin-settings.get-view");

        GoPluginApiResponse response = plugin.handle(getConfigRequest);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
    }

    @Test
    public void shouldRespondSuccessToValidateConfigRequest() throws UnhandledRequestTypeException {
        DefaultGoPluginApiRequest validateRequest = new DefaultGoPluginApiRequest("configrepo", "1.0", "go.plugin-settings.validate-configuration");

        GoPluginApiResponse response = plugin.handle(validateRequest);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
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
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
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
        String requestBody = null;
        parseDirectoryRequest.setRequestBody(requestBody);

        GoPluginApiResponse response = plugin.handle(parseDirectoryRequest);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.BAD_REQUEST));
    }

    @Test
    public void shouldRespondBadRequestToParseDirectoryRequestWhenRequestBodyIsEmpty() throws UnhandledRequestTypeException {
        DefaultGoPluginApiRequest parseDirectoryRequest = new DefaultGoPluginApiRequest("configrepo", "1.0", "parse-directory");
        String requestBody = null;
        parseDirectoryRequest.setRequestBody("{}");

        GoPluginApiResponse response = plugin.handle(parseDirectoryRequest);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.BAD_REQUEST));
    }

    @Test
    public void shouldRespondBadRequestToParseDirectoryRequestWhenRequestBodyIsNotJson() throws UnhandledRequestTypeException {
        DefaultGoPluginApiRequest parseDirectoryRequest = new DefaultGoPluginApiRequest("configrepo", "1.0", "parse-directory");
        String requestBody = null;
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
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
    }

    @Test
    public void shouldGiveBackPipelineJSONForPipelineExport() throws UnhandledRequestTypeException {
        HashMap<String, Object> pipeline = new HashMap<String, Object>();
        pipeline.put("name", "pipeline");
        pipeline.put("group", "group");
        pipeline.put("stages", Collections.emptyList());

        Gson gson = new Gson();
        String pipelineJson = gson.toJson(pipeline);

        String requestJson = format("{\"pipeline\": %s}", pipelineJson);
        DefaultGoPluginApiRequest pipelineExportRequest = new DefaultGoPluginApiRequest("configrepo", "2.0", "pipeline-export");
        pipelineExportRequest.setRequestBody(requestJson);

        GoPluginApiResponse response = plugin.handle(pipelineExportRequest);

        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        assertThat(response.responseBody(), is(gson.toJson(Collections.singletonMap("pipeline", pipelineJson))));
    }

    @Test
    public void shouldRespondWithCapabilities() throws UnhandledRequestTypeException {
        String expected = gson.toJson(new Capabilities());
        DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest("configrepo", "2.0", "get-capabilities");

        GoPluginApiResponse response = plugin.handle(request);

        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
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
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
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

        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        final JsonParser parser = new JsonParser();
        JsonElement responseObj = parser.parse(response.responseBody());
        assertTrue(responseObj.isJsonObject());
        JsonObject obj = responseObj.getAsJsonObject();
        assertTrue(obj.has("errors"));
        assertTrue(obj.has("pipelines"));
        assertTrue(obj.has("environments"));
        assertTrue(obj.has("target_version"));
    }
}
