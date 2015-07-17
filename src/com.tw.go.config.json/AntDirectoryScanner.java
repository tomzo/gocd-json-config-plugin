package com.tw.go.config.json;

import org.apache.tools.ant.DirectoryScanner;

import java.io.File;

public class AntDirectoryScanner implements ConfigDirectoryScanner {

    public AntDirectoryScanner()
    {

    }

    @Override
    public String[] getFilesMatchingPattern(File directory, String pattern) {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(directory);
        scanner.setIncludes(new String[]{pattern});
        scanner.scan();
        return scanner.getIncludedFiles();
    }
}
