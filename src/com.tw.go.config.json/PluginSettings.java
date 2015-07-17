package com.tw.go.config.json;

public class PluginSettings {
    private String pipelinePattern;
    private String environmentPattern;

    public PluginSettings()
    {
    }
    public PluginSettings(String pipelinePattern,String environmentPattern)
    {
        this.pipelinePattern = pipelinePattern;
        this.environmentPattern = environmentPattern;
    }

    public String getPipelinePattern() {
        return pipelinePattern;
    }

    public void setPipelinePattern(String pipelinePattern) {
        this.pipelinePattern = pipelinePattern;
    }

    public String getEnvironmentPattern() {
        return environmentPattern;
    }

    public void setEnvironmentPattern(String environmentPattern) {
        this.environmentPattern = environmentPattern;
    }
}
