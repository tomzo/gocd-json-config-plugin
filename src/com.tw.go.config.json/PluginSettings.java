package com.tw.go.config.json;

class PluginSettings {
    static final String DEFAULT_PIPELINE_PATTERN = "**/*.gopipeline.json";
    static final String DEFAULT_ENVIRONMENT_PATTERN = "**/*.goenvironment.json";

    private String pipelinePattern;
    private String environmentPattern;

    PluginSettings() {
        this(DEFAULT_PIPELINE_PATTERN, DEFAULT_ENVIRONMENT_PATTERN);
    }

    PluginSettings(String pipelinePattern, String environmentPattern) {
        this.pipelinePattern = pipelinePattern;
        this.environmentPattern = environmentPattern;
    }

    private static boolean isBlank(String pattern) {
        return pattern == null || pattern.trim().isEmpty();
    }

    String getPipelinePattern() {
        return isBlank(pipelinePattern) ? DEFAULT_PIPELINE_PATTERN : pipelinePattern;
    }

    String getEnvironmentPattern() {
        return isBlank(environmentPattern) ? DEFAULT_ENVIRONMENT_PATTERN : environmentPattern;
    }
}
