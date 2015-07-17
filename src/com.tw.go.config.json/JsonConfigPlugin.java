package com.tw.go.config.json;

import com.google.gson.*;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Extension
public class JsonConfigPlugin implements GoPlugin {

    private static final String DISPLAY_NAME_ENVIRONMENT_PATTERN = "Go environment files pattern";
    private static final String DISPLAY_NAME_PIPELINE_PATTERN = "Go pipeline files pattern";
    private static final String PLUGIN_SETTINGS_PIPELINE_PATTERN = "pipeline_pattern";
    private static final String MISSING_DIRECTORY_MESSAGE = "directory property is missing in parse-directory request";
    private static final String EMPTY_REQUEST_BODY_MESSAGE = "Request body cannot be null or empty";
    private static Logger LOGGER = Logger.getLoggerFor(JsonConfigPlugin.class);

    public static final String PLUGIN_SETTINGS_GET_CONFIGURATION = "go.plugin-settings.get-configuration";
    public static final String PLUGIN_SETTINGS_GET_VIEW = "go.plugin-settings.get-view";
    public static final String PLUGIN_SETTINGS_VALIDATE_CONFIGURATION = "go.plugin-settings.validate-configuration";
    public static final String PLUGIN_SETTINGS_ENVIRONMENT_PATTERN = "environment_pattern";
    private static final String DEFAULT_ENVIRONMENT_PATTERN = "**/*.goenvironment.json";
    private static final String DEFAULT_PIPELINE_PATTERN = "**/*.gopipeline.json";

    private final Gson gson = new Gson();

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {

    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) throws UnhandledRequestTypeException {
        String requestName = request.requestName();
        if (requestName.equals(PLUGIN_SETTINGS_GET_CONFIGURATION)) {
            return handleGetPluginSettingsConfiguration();
        } else if (requestName.equals(PLUGIN_SETTINGS_GET_VIEW)) {
            try {
                return handleGetPluginSettingsView();
            } catch (IOException e) {
                return renderJSON(500, String.format("Failed to find template: %s", e.getMessage()));
            }
        } else if (requestName.equals(PLUGIN_SETTINGS_VALIDATE_CONFIGURATION)) {
            return handleValidatePluginSettingsConfiguration(request);
        }
        if ("parse-directory".equals(request.requestName())) {
            return handleParseDirectoryRequest(request);
        }
        throw new UnhandledRequestTypeException(request.requestName());
    }
    private GoPluginApiResponse handleGetPluginSettingsView() throws IOException {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("template", IOUtils.toString(getClass().getResourceAsStream("/plugin-settings.template.html"), "UTF-8"));
        return renderJSON(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE, response);
    }
    private GoPluginApiResponse handleValidatePluginSettingsConfiguration(GoPluginApiRequest goPluginApiRequest) {
        List<Map<String, Object>> response = new ArrayList<Map<String, Object>>();
        return renderJSON(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE, response);
    }

    private GoPluginApiResponse handleGetPluginSettingsConfiguration() {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put(PLUGIN_SETTINGS_PIPELINE_PATTERN, createField(DISPLAY_NAME_PIPELINE_PATTERN, DEFAULT_PIPELINE_PATTERN, false, false, "0"));
        response.put(PLUGIN_SETTINGS_ENVIRONMENT_PATTERN, createField(DISPLAY_NAME_ENVIRONMENT_PATTERN, DEFAULT_ENVIRONMENT_PATTERN, false, false, "1"));
        return renderJSON(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE, response);
    }
    private Map<String, Object> createField(String displayName, String defaultValue, boolean isRequired, boolean isSecure, String displayOrder) {
        Map<String, Object> fieldProperties = new HashMap<String, Object>();
        fieldProperties.put("display-name", displayName);
        fieldProperties.put("default-value", defaultValue);
        fieldProperties.put("required", isRequired);
        fieldProperties.put("secure", isSecure);
        fieldProperties.put("display-order", displayOrder);
        return fieldProperties;
    }
    private GoPluginApiResponse renderJSON(final int responseCode, Object response) {
        final String json = response == null ? null : new GsonBuilder().create().toJson(response);
        return new GoPluginApiResponse() {
            @Override
            public int responseCode() {
                return responseCode;
            }

            @Override
            public Map<String, String> responseHeaders() {
                return null;
            }

            @Override
            public String responseBody() {
                return json;
            }
        };
    }

    private GoPluginApiResponse handleParseDirectoryRequest(GoPluginApiRequest request) {
        //ParseDirectoryMessage_1 requestArguments;

        JsonParser jsonParser = new JsonParser();
        try {
            String requestBody = request.requestBody();
            if(requestBody == null) {
                return badRequest(EMPTY_REQUEST_BODY_MESSAGE);
            }
            JsonElement parsedResponse;
            try {
                parsedResponse = jsonParser.parse(requestBody);
            }
            catch (JsonParseException parseException)
            {
                return badRequest("Request body must be valid JSON string");
            }
            if(parsedResponse.equals(new JsonObject())) {
                return badRequest(EMPTY_REQUEST_BODY_MESSAGE);
            }
            JsonObject parsedResponseObject = parsedResponse.getAsJsonObject();
            JsonPrimitive directoryJsonPrimitive = parsedResponseObject.getAsJsonPrimitive("directory");
            if(directoryJsonPrimitive == null) {
                return badRequest(MISSING_DIRECTORY_MESSAGE);
            }
            String directory = directoryJsonPrimitive.getAsString();
            File baseDir = new File(directory);

            JsonConfigCollection config = new JsonConfigCollection();
            JsonFileParser parser = new JsonFileParser();
            //TODO use pipeline pattern from settings
            //TODO use environment pattern from settings
            ConfigDirectoryScanner scanner = new AntDirectoryScanner();

            String environmentPattern = DEFAULT_ENVIRONMENT_PATTERN;
            String pipelinePattern = DEFAULT_PIPELINE_PATTERN;

            for (String environmentFile : scanner.getFilesMatchingPattern(baseDir, environmentPattern)) {
                JsonElement env = parser.parseFile(new File(baseDir,environmentFile));
                config.addEnvironment(env);
            }

            for (String pipelineFile : scanner.getFilesMatchingPattern(baseDir, pipelinePattern)) {
                JsonElement pipe = parser.parseFile(new File(baseDir,pipelineFile));
                config.addPipeline(pipe);
            }

            JsonObject configJsonObject = config.getJsonObject();
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.add("partialConfig",configJsonObject);

            return DefaultGoPluginApiResponse.success(gson.toJson(responseJsonObject));
        }
        catch (Exception e) {
            LOGGER.warn("Error occurred while parsing configuration repository.", e);
            JsonObject responseJsonObject = new JsonObject();
            JsonArray errors = new JsonArray();
            JsonObject exceptionAsJson = new JsonObject();
            exceptionAsJson.addProperty("message",e.getMessage());
            errors.add(exceptionAsJson);
            responseJsonObject.add("pluginErrors", errors);
            return DefaultGoPluginApiResponse.success(gson.toJson(responseJsonObject));
        }
    }

    private GoPluginApiResponse badRequest(String message) {
        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("message", message);
        return DefaultGoPluginApiResponse.badRequest(gson.toJson(responseJsonObject));
    }

    private void parsePipelineFiles( RegexDirectoryScanner scanner, JsonConfigCollection config, JsonFileParser parser) throws Exception {

        for (File pipelineFile : scanner.getPipelineFiles()) {
            JsonElement env = parser.parseFile(pipelineFile);
            config.addPipeline(env);
        }
    }


    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private GoPluginApiResponse createResponse(int responseCode, String body) {
        final DefaultGoPluginApiResponse response = new DefaultGoPluginApiResponse(responseCode);
        response.setResponseBody(body);
        return response;
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier("configrepo", Arrays.asList("1.0"));
    }
}
