package com.tw.go.config.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

import static com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse.*;
import static com.tw.go.config.json.PluginSettings.*;
import static java.lang.String.format;

@Extension
public class JsonConfigPlugin implements GoPlugin, ConfigRepoMessages {
    private static final String DISPLAY_NAME_ENVIRONMENT_PATTERN = "Go environment files pattern";
    private static final String DISPLAY_NAME_PIPELINE_PATTERN = "Go pipeline files pattern";
    private static final String PLUGIN_ID = "json.config.plugin";
    private static Logger LOGGER = Logger.getLoggerFor(JsonConfigPlugin.class);

    private final Gson gson = new Gson();
    private final Gson prettyPrint = new GsonBuilder().setPrettyPrinting().create();
    private GoApplicationAccessor goApplicationAccessor;
    private PluginSettings settings;

    private static boolean isBlank(String pattern) {
        return pattern == null || pattern.trim().isEmpty();
    }

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
        this.goApplicationAccessor = goApplicationAccessor;
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) throws UnhandledRequestTypeException {
        String requestName = request.requestName();
        switch (requestName) {
            case REQ_PLUGIN_SETTINGS_GET_CONFIGURATION:
                return handleGetPluginSettingsConfiguration();
            case REQ_PLUGIN_SETTINGS_GET_VIEW:
                try {
                    return handleGetPluginSettingsView();
                } catch (IOException e) {
                    return error(gson.toJson(format("Failed to find template: %s", e.getMessage())));
                }
            case REQ_PLUGIN_SETTINGS_VALIDATE_CONFIGURATION:
                return handleValidatePluginSettingsConfiguration();
            case REQ_PARSE_CONTENT:
                ensureConfigured();
                return handleParseContentRequest(request);
            case REQ_PARSE_DIRECTORY:
                ensureConfigured();
                return handleParseDirectoryRequest(request);
            case REQ_PIPELINE_EXPORT:
                return handlePipelineExportRequest(request);
            case REQ_PLUGIN_SETTINGS_CHANGED:
                configurePlugin(PluginSettings.fromJson(request.requestBody()));
                return new DefaultGoPluginApiResponse(SUCCESS_RESPONSE_CODE, "");
            case REQ_GET_CAPABILITIES:
                return success(gson.toJson(new Capabilities()));
        }
        throw new UnhandledRequestTypeException(request.requestName());
    }

    String getPipelinePattern() {
        if (null != settings && !isBlank(settings.getPipelinePattern())) {
            return settings.getPipelinePattern();
        }
        return DEFAULT_PIPELINE_PATTERN;
    }

    String getEnvironmentPattern() {
        if (null != settings && !isBlank(settings.getEnvironmentPattern())) {
            return settings.getEnvironmentPattern();
        }
        return DEFAULT_ENVIRONMENT_PATTERN;
    }

    /**
     * fetches plugin settings if we haven't yet
     */
    private void ensureConfigured() {
        if (null == settings) {
            settings = fetchPluginSettings();
        }
    }

    private GoPluginApiResponse handleGetPluginSettingsView() throws IOException {
        Map<String, Object> response = new HashMap<>();
        response.put("template", IOUtils.toString(getClass().getResourceAsStream("/plugin-settings.template.html"), "UTF-8"));
        return success(gson.toJson(response));
    }

    private GoPluginApiResponse handleValidatePluginSettingsConfiguration() {
        List<Map<String, Object>> response = new ArrayList<>();
        return success(gson.toJson(response));
    }

    private GoPluginApiResponse handleGetPluginSettingsConfiguration() {
        Map<String, Object> response = new HashMap<>();
        response.put(PLUGIN_SETTINGS_PIPELINE_PATTERN, createField(DISPLAY_NAME_PIPELINE_PATTERN, DEFAULT_PIPELINE_PATTERN, false, false, "0"));
        response.put(PLUGIN_SETTINGS_ENVIRONMENT_PATTERN, createField(DISPLAY_NAME_ENVIRONMENT_PATTERN, DEFAULT_ENVIRONMENT_PATTERN, false, false, "1"));
        return success(gson.toJson(response));
    }

    private Map<String, Object> createField(String displayName, String defaultValue, boolean isRequired, boolean isSecure, String displayOrder) {
        Map<String, Object> fieldProperties = new HashMap<>();
        fieldProperties.put("display-name", displayName);
        fieldProperties.put("default-value", defaultValue);
        fieldProperties.put("required", isRequired);
        fieldProperties.put("secure", isSecure);
        fieldProperties.put("display-order", displayOrder);
        return fieldProperties;
    }

    private GoPluginApiResponse handleParseContentRequest(GoPluginApiRequest request) {
        return handlingErrors(() -> {
            FilenameMatcher matcher = new FilenameMatcher(getPipelinePattern(), getEnvironmentPattern());
            ParsedRequest parsed = ParsedRequest.parse(request);
            List<Map<String, String>> contents = parsed.getParam("contents");

            JsonConfigCollection result = new JsonConfigCollection();
            contents.forEach(file -> {
                String filename = file.keySet().iterator().next();
                String content = file.get(filename);

                ByteArrayInputStream contentStream = new ByteArrayInputStream(content.getBytes());

                if (matcher.isEnvironmentFile(filename)) {
                    JsonElement env = JsonConfigParser.parseStream(result, contentStream, filename);
                    if (null != env) {
                        result.addEnvironment(env, filename);
                    }
                } else if (matcher.isPipelineFile(filename)) {
                    JsonElement pipe = JsonConfigParser.parseStream(result, contentStream, filename);
                    if (null != pipe) {
                        result.addPipeline(pipe, filename);
                    }
                } else {
                    result.addError(new PluginError("File does not match environment or pipeline pattern", filename));
                }
            });

            result.updateVersionFromPipelinesAndEnvironments();

            return success(gson.toJson(result.getJsonObject()));
        });
    }

    private GoPluginApiResponse handlePipelineExportRequest(GoPluginApiRequest request) {
        return handlingErrors(() -> {
            ParsedRequest parsed = ParsedRequest.parse(request);

            Map<String, Object> pipeline = parsed.getParam("pipeline");

            return success(gson.toJson(Collections.singletonMap("pipeline", prettyPrint.toJson(pipeline))));

        });
    }

    private GoPluginApiResponse handleParseDirectoryRequest(GoPluginApiRequest request) {
        return handlingErrors(() -> {
            ParsedRequest parsed = ParsedRequest.parse(request);
            File baseDir = new File(parsed.getStringParam("directory"));

            JsonConfigParser parser = new JsonConfigParser();
            ConfigDirectoryScanner scanner = new AntDirectoryScanner();

            ConfigDirectoryParser configDirectoryParser = new ConfigDirectoryParser(
                    scanner, parser, getPipelinePattern(), getEnvironmentPattern()
            );

            JsonConfigCollection config = configDirectoryParser.parseDirectory(baseDir);

            config.updateVersionFromPipelinesAndEnvironments();
            JsonObject responseJsonObject = config.getJsonObject();

            return success(gson.toJson(responseJsonObject));
        });
    }

    private PluginSettings fetchPluginSettings() {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("plugin-id", PLUGIN_ID);
        GoApiResponse response = goApplicationAccessor.submit(createGoApiRequest(REQ_GET_PLUGIN_SETTINGS, JSONUtils.toJSON(requestMap)));
        if (response.responseBody() == null || response.responseBody().trim().isEmpty()) {
            return new PluginSettings();
        }
        return PluginSettings.fromJson(response.responseBody());
    }


    private GoPluginApiResponse badRequest(String message) {
        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("message", message);
        return DefaultGoPluginApiResponse.badRequest(gson.toJson(responseJsonObject));
    }

    private GoPluginApiResponse handlingErrors(Supplier<GoPluginApiResponse> exec) {
        try {
            return exec.get();
        } catch (ParsedRequest.RequestParseException e) {
            return badRequest(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error occurred in JSON configuration plugin.", e);
            JsonConfigCollection config = new JsonConfigCollection();
            config.addError(new PluginError(e.toString(), "JSON config plugin"));
            return error(gson.toJson(config.getJsonObject()));
        }
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier("configrepo", Arrays.asList("1.0", "2.0"));
    }

    private void configurePlugin(PluginSettings settings) {
        this.settings = settings;
    }

    private GoApiRequest createGoApiRequest(final String api, final String responseBody) {
        return JsonConfigHelper.request(api, responseBody, pluginIdentifier());
    }
}
