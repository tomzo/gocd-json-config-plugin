package com.tw.go.config.json;

import java.io.File;

public interface ConfigDirectoryScanner {
    String[] getFilesMatchingPattern(File directory,String pattern);
}
