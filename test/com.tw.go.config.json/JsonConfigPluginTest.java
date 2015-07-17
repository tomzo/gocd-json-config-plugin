package com.tw.go.config.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.request.DefaultGoPluginApiRequest;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class JsonConfigPluginTest {

    private JsonConfigPlugin plugin;
    private Gson gson;
    private JsonParser parser;

    @Before
    public void SetUp()
    {
        plugin = new JsonConfigPlugin();
        gson = new Gson();
        parser = new JsonParser();
    }


    @Test
    public void shouldRespondSuccessToGetConfigurationRequest()  throws UnhandledRequestTypeException
    {
        DefaultGoPluginApiRequest getConfigRequest = new DefaultGoPluginApiRequest("configrepo","1.0","go.plugin-settings.get-configuration");

        GoPluginApiResponse response = plugin.handle(getConfigRequest);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
    }

    @Test
    public void shouldContainEnvironmentPatternInResponseToGetConfigurationRequest()  throws UnhandledRequestTypeException
    {
        DefaultGoPluginApiRequest getConfigRequest = new DefaultGoPluginApiRequest("configrepo","1.0","go.plugin-settings.get-configuration");

        GoPluginApiResponse response = plugin.handle(getConfigRequest);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        JsonObject responseJsonObject = getJsonObjectFromResponse(response);
        JsonElement environmentPatternConfig = responseJsonObject.get("environment_pattern");
        assertNotNull(environmentPatternConfig);
        JsonObject environmentPatternConfigAsJsonObject = environmentPatternConfig.getAsJsonObject();
        assertThat(environmentPatternConfigAsJsonObject.get("display-name").getAsString(), is("Go environment files pattern"));
        assertThat(environmentPatternConfigAsJsonObject.get("default-value").getAsString(),is("**/*.goenvironment.json"));
        assertThat(environmentPatternConfigAsJsonObject.get("required").getAsBoolean(),is(false));
        assertThat(environmentPatternConfigAsJsonObject.get("secure").getAsBoolean(),is(false));
        assertThat(environmentPatternConfigAsJsonObject.get("display-order").getAsInt(),is(1));
    }
    @Test
    public void shouldContainPipelinePatternInResponseToGetConfigurationRequest()  throws UnhandledRequestTypeException
    {
        DefaultGoPluginApiRequest getConfigRequest = new DefaultGoPluginApiRequest("configrepo","1.0","go.plugin-settings.get-configuration");

        GoPluginApiResponse response = plugin.handle(getConfigRequest);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        JsonObject responseJsonObject = getJsonObjectFromResponse(response);
        JsonElement pipelinePatternConfig = responseJsonObject.get("pipeline_pattern");
        assertNotNull(pipelinePatternConfig);
        JsonObject pipelinePatternConfigAsJsonObject = pipelinePatternConfig.getAsJsonObject();
        assertThat(pipelinePatternConfigAsJsonObject.get("display-name").getAsString(), is("Go pipeline files pattern"));
        assertThat(pipelinePatternConfigAsJsonObject.get("default-value").getAsString(),is("**/*.gopipeline.json"));
        assertThat(pipelinePatternConfigAsJsonObject.get("required").getAsBoolean(),is(false));
        assertThat(pipelinePatternConfigAsJsonObject.get("secure").getAsBoolean(),is(false));
        assertThat(pipelinePatternConfigAsJsonObject.get("display-order").getAsInt(),is(0));
    }

    private JsonObject getJsonObjectFromResponse(GoPluginApiResponse response) {
        String responseBody = response.responseBody();
        return parser.parse(responseBody).getAsJsonObject();
    }

    @Test
    public void shouldRespondSuccessToGetViewRequest() throws UnhandledRequestTypeException
    {
        DefaultGoPluginApiRequest getConfigRequest = new DefaultGoPluginApiRequest("configrepo","1.0","go.plugin-settings.get-view");

        GoPluginApiResponse response = plugin.handle(getConfigRequest);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
    }

    @Test
    public void shouldRespondSuccessToValidateConfigRequest() throws UnhandledRequestTypeException
    {
        DefaultGoPluginApiRequest validateRequest = new DefaultGoPluginApiRequest("configrepo","1.0","go.plugin-settings.validate-configuration");

        GoPluginApiResponse response = plugin.handle(validateRequest);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
    }

    @Test
    public void shouldRespondSuccessToParseDirectoryRequestWhenEmpty() throws UnhandledRequestTypeException
    {
        DefaultGoPluginApiRequest parseDirectoryRequest = new DefaultGoPluginApiRequest("configrepo","1.0","parse-directory");
        String requestBody = "{\n" +
                "    \"directory\":\"pipelines/flyweight/e4ee3e68-8a3e-4435-85ab-47e94-efbf5a\",\n" +
                "    \"configurations\":[]\n" +
                "}";
        parseDirectoryRequest.setRequestBody(requestBody);

        GoPluginApiResponse response = plugin.handle(parseDirectoryRequest);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
    }

    @Test
    public void shouldRespondBadRequestToParseDirectoryRequestWhenDirectoryIsNotSpecified() throws UnhandledRequestTypeException
    {
        DefaultGoPluginApiRequest parseDirectoryRequest = new DefaultGoPluginApiRequest("configrepo","1.0","parse-directory");
        String requestBody = "{\n" +
                "    \"configurations\":[]\n" +
                "}";
        parseDirectoryRequest.setRequestBody(requestBody);

        GoPluginApiResponse response = plugin.handle(parseDirectoryRequest);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.BAD_REQUEST));
    }
    @Test
    public void shouldRespondBadRequestToParseDirectoryRequestWhenRequestBodyIsNull() throws UnhandledRequestTypeException
    {
        DefaultGoPluginApiRequest parseDirectoryRequest = new DefaultGoPluginApiRequest("configrepo","1.0","parse-directory");
        String requestBody = null;
        parseDirectoryRequest.setRequestBody(requestBody);

        GoPluginApiResponse response = plugin.handle(parseDirectoryRequest);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.BAD_REQUEST));
    }
    @Test
    public void shouldRespondBadRequestToParseDirectoryRequestWhenRequestBodyIsEmpty() throws UnhandledRequestTypeException
    {
        DefaultGoPluginApiRequest parseDirectoryRequest = new DefaultGoPluginApiRequest("configrepo","1.0","parse-directory");
        String requestBody = null;
        parseDirectoryRequest.setRequestBody("{}");

        GoPluginApiResponse response = plugin.handle(parseDirectoryRequest);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.BAD_REQUEST));
    }
    @Test
    public void shouldRespondBadRequestToParseDirectoryRequestWhenRequestBodyIsNotJson() throws UnhandledRequestTypeException
    {
        DefaultGoPluginApiRequest parseDirectoryRequest = new DefaultGoPluginApiRequest("configrepo","1.0","parse-directory");
        String requestBody = null;
        parseDirectoryRequest.setRequestBody("{bla");

        GoPluginApiResponse response = plugin.handle(parseDirectoryRequest);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.BAD_REQUEST));
    }
}
