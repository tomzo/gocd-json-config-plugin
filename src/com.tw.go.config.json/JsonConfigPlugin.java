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
import com.thoughtworks.go.plugin.configrepo.messages.ParseDirectoryMessage_1;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Extension
public class JsonConfigPlugin implements GoPlugin {
    private static Logger LOGGER = Logger.getLoggerFor(JsonConfigPlugin.class);

    public static final String PLUGIN_SETTINGS_GET_CONFIGURATION = "go.plugin-settings.get-configuration";
    public static final String PLUGIN_SETTINGS_GET_VIEW = "go.plugin-settings.get-view";
    public static final String PLUGIN_SETTINGS_VALIDATE_CONFIGURATION = "go.plugin-settings.validate-configuration";
    public static final String PLUGIN_SETTINGS_ENVIRONMENT_PATTERN = "environment_pattern";
    private static final String DEFAULT_ENVIRONMENT_PATTERN = ".*goenvironment.json";

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
        Map<String, Object> responseMap = (Map<String, Object>) JSONUtils.fromJSON(goPluginApiRequest.requestBody());
        final Map<String, String> configuration = keyValuePairs(responseMap, "plugin-settings");
        List<Map<String, Object>> response = new ArrayList<Map<String, Object>>();

        //TODO actual validation
        validate(response);

        return renderJSON(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE, response);
    }
    private void validate(List<Map<String, Object>> response) {
        Map<String, Object> fieldValidation = new HashMap<String, Object>();
        if (!fieldValidation.isEmpty()) {
            response.add(fieldValidation);
        }
    }
    private Map<String, String> keyValuePairs(Map<String, Object> map, String mainKey) {
        Map<String, String> keyValuePairs = new HashMap<String, String>();
        Map<String, Object> fieldsMap = (Map<String, Object>) map.get(mainKey);
        for (String field : fieldsMap.keySet()) {
            Map<String, Object> fieldProperties = (Map<String, Object>) fieldsMap.get(field);
            String value = (String) fieldProperties.get("value");
            keyValuePairs.put(field, value);
        }
        return keyValuePairs;
    }

    private GoPluginApiResponse handleGetPluginSettingsConfiguration() {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put(PLUGIN_SETTINGS_ENVIRONMENT_PATTERN, createField("Environment pattern", DEFAULT_ENVIRONMENT_PATTERN, false, false, "0"));
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
        Gson gson = new Gson();
        try {
            Map<String, Object> dataMap = (Map<String, Object>) JSONUtils.fromJSON(request.requestBody());
            String directory = (String) dataMap.get("directory");

            JsonConfigCollection config = new JsonConfigCollection();
            JsonFileParser parser = new JsonFileParser();
            //TODO use environment pattern from settings
            DirectoryScanner scanner = new DirectoryScanner(new File(directory), DEFAULT_ENVIRONMENT_PATTERN);
            for (File environmentFile : scanner.getEnvironmentFiles()) {
                JsonElement env;
                try {
                    env = parser.parseFile(environmentFile);
                } catch (Exception ex) {
                    String errorBody = "{}";
                    return DefaultGoPluginApiResponse.error(errorBody);
                }
                config.addEnvironment(env);
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
