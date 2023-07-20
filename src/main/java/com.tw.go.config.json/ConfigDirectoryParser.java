package com.tw.go.config.json;

import com.google.gson.JsonElement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

class ConfigDirectoryParser {
    private ConfigDirectoryScanner scanner;
    private JsonConfigParser parser;
    private String pipelinePattern;
    private String environmentPattern;

    ConfigDirectoryParser(ConfigDirectoryScanner scanner, JsonConfigParser parser, String pipelinePattern, String environmentPattern) {
        this.scanner = scanner;
        this.parser = parser;
        this.pipelinePattern = pipelinePattern;
        this.environmentPattern = environmentPattern;
    }

    JsonConfigCollection parseDirectory(File baseDir) {
        JsonConfigCollection config = new JsonConfigCollection();
        File currentFile;

        try {
            for (String environmentFile : scanner.getFilesMatchingPattern(baseDir, environmentPattern)) {
                currentFile = new File(baseDir, environmentFile);
                JsonElement environment = JsonConfigParser.parseStream(config, new FileInputStream(currentFile), currentFile.getPath());
                if (null != environment) {
                    config.addEnvironment(environment, environmentFile);
                }
            }

            for (String pipelineFile : scanner.getFilesMatchingPattern(baseDir, pipelinePattern)) {
                currentFile = new File(baseDir, pipelineFile);
                JsonElement pipeline = JsonConfigParser.parseStream(config, new FileInputStream(currentFile), currentFile.getPath());
                if (null != pipeline) {
                    config.addPipeline(pipeline, pipelineFile);
                }
            }
        } catch (FileNotFoundException e) {
            config.addError(new PluginError(e.getMessage()));
        }

        return config;
    }
}
