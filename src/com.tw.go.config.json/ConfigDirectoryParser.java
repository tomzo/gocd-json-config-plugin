package com.tw.go.config.json;

import com.google.gson.JsonElement;

import java.io.File;

class ConfigDirectoryParser {
    private ConfigDirectoryScanner scanner;
    private JsonFileParser parser;
    private String pipelinePattern;
    private String environmentPattern;

    ConfigDirectoryParser(ConfigDirectoryScanner scanner, JsonFileParser parser, String pipelinePattern, String environmentPattern) {
        this.scanner = scanner;
        this.parser = parser;
        this.pipelinePattern = pipelinePattern;
        this.environmentPattern = environmentPattern;
    }

    JsonConfigCollection parseDirectory(File baseDir) {
        JsonConfigCollection config = new JsonConfigCollection();
        for (String environmentFile : scanner.getFilesMatchingPattern(baseDir, environmentPattern)) {
            JsonElement environment = JsonFileParser.processFile(config, parser, new File(baseDir, environmentFile));
            if (null != environment) {
                config.addEnvironment(environment, environmentFile);
            }
        }

        for (String pipelineFile : scanner.getFilesMatchingPattern(baseDir, pipelinePattern)) {
            JsonElement pipeline = JsonFileParser.processFile(config, parser, new File(baseDir, pipelineFile));
            if (null != pipeline) {
                config.addPipeline(pipeline, pipelineFile);
            }
        }

        return config;
    }
}
