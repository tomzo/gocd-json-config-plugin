package com.tw.go.config.json;

import com.google.gson.*;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoApiRequest;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Extension
public class JsonConfigPlugin implements GoPlugin {

    public static final String GET_PLUGIN_SETTINGS = "go.processor.plugin-settings.get";
    public static final String PLUGIN_SETTINGS_GET_CONFIGURATION = "go.plugin-settings.get-configuration";
    public static final String PLUGIN_SETTINGS_GET_VIEW = "go.plugin-settings.get-view";
    public static final String PLUGIN_SETTINGS_VALIDATE_CONFIGURATION = "go.plugin-settings.validate-configuration";
    public static final String PLUGIN_SETTINGS_ENVIRONMENT_PATTERN = "environment_pattern";
    public static final String DEFAULT_ENVIRONMENT_PATTERN = "**/*.goenvironment.json";
    public static final String DEFAULT_PIPELINE_PATTERN = "**/*.gopipeline.json";
    private static final String DISPLAY_NAME_ENVIRONMENT_PATTERN = "Go environment files pattern";
    private static final String DISPLAY_NAME_PIPELINE_PATTERN = "Go pipeline files pattern";
    private static final String PLUGIN_SETTINGS_PIPELINE_PATTERN = "pipeline_pattern";
    private static final String MISSING_DIRECTORY_MESSAGE = "directory property is missing in parse-directory request";
    private static final String EMPTY_REQUEST_BODY_MESSAGE = "Request body cannot be null or empty";
    private static final String PLUGIN_ID = "json.config.plugin";
    private static Logger LOGGER = Logger.getLoggerFor(JsonConfigPlugin.class);
    private final Gson gson = new Gson();
    private final Gson prettyPrint = new GsonBuilder().setPrettyPrinting().create();
    private GoApplicationAccessor goApplicationAccessor;

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
        this.goApplicationAccessor = goApplicationAccessor;
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
        } else if ("pipeline-export".equals(request.requestName())) {
            return handlePipelineExportRequest(request);
        } else if ("get-capabilities".equals(request.requestName())) {
            return DefaultGoPluginApiResponse.success(gson.toJson(new Capabilities()));
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
        return JsonConfigHelper.response(responseCode, json);
    }

    private GoPluginApiResponse handlePipelineExportRequest(GoPluginApiRequest request) {
        JsonParser parser = new JsonParser();

        try {
            String requestBody = request.requestBody();

            if (requestBody == null) {
                return badRequest(EMPTY_REQUEST_BODY_MESSAGE);
            }

            JsonElement parsedRequest;
            try {
                parsedRequest = parser.parse(requestBody);
            } catch (JsonParseException parseException) {
                return badRequest("Request body must be valid JSON string");
            }

            if (parsedRequest.equals(new JsonObject())) {
                return badRequest(EMPTY_REQUEST_BODY_MESSAGE);
            }

            JsonObject requestObj = parsedRequest.getAsJsonObject();

            if (!requestObj.has("pipeline")) {
                return badRequest("`pipeline` is missing from request");
            }

            if (!requestObj.get("pipeline").isJsonObject()) {
                return badRequest("`pipeline` key in request is not an object");
            }

            String pipeline = prettyPrint.toJson(requestObj.get("pipeline"));
            return DefaultGoPluginApiResponse.success(gson.toJson(Collections.singletonMap("pipeline", pipeline)));
        } catch (Exception e) {
            LOGGER.error("Unexpected error occurred while exporting pipeline.", e);
            JsonConfigCollection config = new JsonConfigCollection();
            config.addError(new PluginError(e.toString(), "JSON config plugin"));
            return DefaultGoPluginApiResponse.error(gson.toJson(config.getJsonObject()));
        }
    }

    private GoPluginApiResponse handleParseDirectoryRequest(GoPluginApiRequest request) {
        JsonParser jsonParser = new JsonParser();
        try {
            String requestBody = request.requestBody();
            if (requestBody == null) {
                return badRequest(EMPTY_REQUEST_BODY_MESSAGE);
            }
            JsonElement parsed;
            try {
                parsed = jsonParser.parse(requestBody);
            } catch (JsonParseException parseException) {
                return badRequest("Request body must be valid JSON string");
            }
            if (parsed.equals(new JsonObject())) {
                return badRequest(EMPTY_REQUEST_BODY_MESSAGE);
            }
            JsonObject parsedRequest = parsed.getAsJsonObject();
            JsonPrimitive directoryJsonPrimitive = parsedRequest.getAsJsonPrimitive("directory");
            if (directoryJsonPrimitive == null) {
                return badRequest(MISSING_DIRECTORY_MESSAGE);
            }
            String directory = directoryJsonPrimitive.getAsString();
            File baseDir = new File(directory);

            JsonFileParser parser = new JsonFileParser();
            PluginSettings settings = getPluginSettings();
            ConfigDirectoryScanner scanner = new AntDirectoryScanner();

            String environmentPattern = isBlank(settings.getEnvironmentPattern()) ?
                    DEFAULT_ENVIRONMENT_PATTERN : settings.getEnvironmentPattern();

            String pipelinePattern = isBlank(settings.getPipelinePattern()) ?
                    DEFAULT_PIPELINE_PATTERN : settings.getPipelinePattern();

            ConfigDirectoryParser configDirectoryParser = new ConfigDirectoryParser(
                    scanner, parser, pipelinePattern, environmentPattern);
            JsonConfigCollection config = configDirectoryParser.parseDirectory(baseDir);

            config.updateVersionFromPipelinesAndEnvironments();
            JsonObject responseJsonObject = config.getJsonObject();

            return DefaultGoPluginApiResponse.success(gson.toJson(responseJsonObject));
        } catch (Exception e) {
            LOGGER.error("Unexpected error occurred while parsing configuration repository.", e);
            JsonConfigCollection config = new JsonConfigCollection();
            config.addError(new PluginError(e.toString(), "JSON config plugin"));
            return DefaultGoPluginApiResponse.error(gson.toJson(config.getJsonObject()));
        }
    }

    private boolean isBlank(String pattern) {
        return pattern == null || pattern.isEmpty();
    }

    public PluginSettings getPluginSettings() {
        Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("plugin-id", PLUGIN_ID);
        GoApiResponse response = goApplicationAccessor.submit(createGoApiRequest(GET_PLUGIN_SETTINGS, JSONUtils.toJSON(requestMap)));
        if (response.responseBody() == null || response.responseBody().trim().isEmpty()) {
            return new PluginSettings();
        }
        Map<String, String> responseBodyMap = (Map<String, String>) JSONUtils.fromJSON(response.responseBody());
        return new PluginSettings(
                responseBodyMap.get(PLUGIN_SETTINGS_PIPELINE_PATTERN),
                responseBodyMap.get(PLUGIN_SETTINGS_ENVIRONMENT_PATTERN));
    }


    private GoPluginApiResponse badRequest(String message) {
        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("message", message);
        return DefaultGoPluginApiResponse.badRequest(gson.toJson(responseJsonObject));
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
        return new GoPluginIdentifier("configrepo", Arrays.asList("1.0", "2.0"));
    }

    private GoApiRequest createGoApiRequest(final String api, final String responseBody) {
        return JsonConfigHelper.request(api, responseBody, pluginIdentifier());
    }
}
