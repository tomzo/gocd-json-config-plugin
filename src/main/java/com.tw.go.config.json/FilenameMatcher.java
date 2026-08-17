package com.tw.go.config.json;

import org.codehaus.plexus.util.SelectorUtils;

import java.io.File;

/**
 * Convenience class that matches the filename only (not full paths) against
 * pipeline and environment patterns.
 */
class FilenameMatcher {
    private final String pipelineFilePattern;
    private final String environmentFilePattern;

    FilenameMatcher(String pipelineFilePattern, String environmentFilePattern) {
        this.pipelineFilePattern = basename(pipelineFilePattern);
        this.environmentFilePattern = basename(environmentFilePattern);
    }

    /**
     * @param pathPattern the path-matching pattern
     * @return a pattern that only matches the filename (i.e., basename)
     */
    private static String basename(String pathPattern) {
        return new File(normalizePattern(pathPattern)).getName();
    }

    /**
     * Ripped from `org.codehaus.plexus.util.DirectoryScanner`
     */
    private static String normalizePattern(String p) {
        String pattern = p.replace('/', File.separatorChar)
                .replace('\\', File.separatorChar);
        if (pattern.endsWith(File.separator)) {
            pattern += "**";
        }
        return pattern;
    }

    boolean isPipelineFile(String filename) {
        return SelectorUtils.match(pipelineFilePattern, filename);
    }

    boolean isEnvironmentFile(String filename) {
        return SelectorUtils.match(environmentFilePattern, filename);
    }
}
