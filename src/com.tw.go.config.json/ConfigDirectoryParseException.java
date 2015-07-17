package com.tw.go.config.json;

import java.util.List;

public class ConfigDirectoryParseException extends RuntimeException {
    private List<PluginError> errors;

    public ConfigDirectoryParseException(List<PluginError> errors) {
        this.errors = errors;
    }

    public List<PluginError> getErrors() {
        return errors;
    }
}
