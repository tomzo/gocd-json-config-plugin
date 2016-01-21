package com.tw.go.config.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigDirectoryParser {
    private ConfigDirectoryScanner scanner;
    private JsonFileParser parser;
    private String pipelinePattern;
    private String environmentPattern;

    public ConfigDirectoryParser(ConfigDirectoryScanner scanner, JsonFileParser parser, String pipelinePattern, String environmentPattern) {

        this.scanner = scanner;
        this.parser = parser;
        this.pipelinePattern = pipelinePattern;
        this.environmentPattern = environmentPattern;
    }

    public JsonConfigCollection parseDirectory(File baseDir) throws Exception {
        List<PluginError> errors = new ArrayList<>();
        JsonConfigCollection config = new JsonConfigCollection();
        for (String environmentFile : scanner.getFilesMatchingPattern(baseDir, environmentPattern)) {
            try {
                JsonElement env = parser.parseFile(new File(baseDir, environmentFile));
                if(env == null || env.isJsonNull())
                {
                    PluginError error = new PluginError(
                            String.format("Environment file is empty"),
                            environmentFile);
                    errors.add(error);
                }
                else if(env.equals(new JsonObject()))
                {
                    PluginError error = new PluginError(
                            String.format("Environment definition is empty"),
                            environmentFile);
                    errors.add(error);
                }
                else
                    config.addEnvironment(env,environmentFile);
            }
            catch (JsonParseException parseException)
            {
                PluginError error = new PluginError(
                        String.format("Failed to parse environment file as JSON: %s",parseException.getMessage()),
                        environmentFile);
                errors.add(error);
            }
        }

        for (String pipelineFile : scanner.getFilesMatchingPattern(baseDir, pipelinePattern)) {
            try {
                JsonElement pipe = parser.parseFile(new File(baseDir, pipelineFile));
                if(pipe == null || pipe.isJsonNull())
                {
                    PluginError error = new PluginError(
                            String.format("Pipeline file is empty"),
                            pipelineFile);
                    errors.add(error);
                }
                else if(pipe.equals(new JsonObject()))
                {
                    PluginError error = new PluginError(
                            String.format("Pipeline definition is empty"),
                            pipelineFile);
                    errors.add(error);
                }
                else
                    config.addPipeline(pipe,pipelineFile);
            }
            catch (JsonParseException parseException)
            {
                PluginError error = new PluginError(
                        String.format("Failed to parse pipeline file as JSON: %s",parseException.getMessage()),
                        pipelineFile);
                errors.add(error);
            }
        }
        if(!errors.isEmpty())
            throw new ConfigDirectoryParseException(errors);

        return config;
    }
}
