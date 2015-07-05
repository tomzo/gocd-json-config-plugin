package com.tw.go.config.json;

import java.io.File;
import java.io.FilenameFilter;

public class DirectoryScanner {
    private File directory;
    private String environmentPattern;

    public DirectoryScanner(File directory,String environmentPattern)
    {
        this.directory = directory;
        this.environmentPattern = environmentPattern;
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
}
