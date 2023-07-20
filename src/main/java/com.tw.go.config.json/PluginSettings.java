package com.tw.go.config.json;

import java.util.Map;

class PluginSettings {
    static final String DEFAULT_PIPELINE_PATTERN = "**/*.gopipeline.json";
    static final String DEFAULT_ENVIRONMENT_PATTERN = "**/*.goenvironment.json";
    static final String PLUGIN_SETTINGS_PIPELINE_PATTERN = "pipeline_pattern";
    static final String PLUGIN_SETTINGS_ENVIRONMENT_PATTERN = "environment_pattern";

    private String pipelinePattern;
    private String environmentPattern;

    PluginSettings() {
        this(DEFAULT_PIPELINE_PATTERN, DEFAULT_ENVIRONMENT_PATTERN);
    }

    PluginSettings(String pipelinePattern, String environmentPattern) {
        this.pipelinePattern = pipelinePattern;
        this.environmentPattern = environmentPattern;
    }

    static PluginSettings fromJson(String json) {
        Map<String, String> raw = JSONUtils.fromJSON(json);
        return new PluginSettings(
                raw.get(PLUGIN_SETTINGS_PIPELINE_PATTERN),
                raw.get(PLUGIN_SETTINGS_ENVIRONMENT_PATTERN));

    }

    String getPipelinePattern() {
        return pipelinePattern;
    }

    String getEnvironmentPattern() {
        return environmentPattern;
    }
}
