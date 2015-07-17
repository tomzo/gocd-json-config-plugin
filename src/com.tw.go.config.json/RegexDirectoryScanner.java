package com.tw.go.config.json;

import java.io.File;
import java.io.FilenameFilter;

public class RegexDirectoryScanner {
    private File directory;
    private String environmentPattern;
    private String pipelinePattern;

    public RegexDirectoryScanner(File directory, String environmentPattern, String pipelinePattern)
    {
        this.directory = directory;
        this.environmentPattern = environmentPattern;
        this.pipelinePattern = pipelinePattern;
    }

    public File[] getEnvironmentFiles() {
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(environmentPattern);
            }
        });
        return files;
    }
    public File[] getPipelineFiles() {
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(pipelinePattern);
            }
        });
        return files;
    }
}
