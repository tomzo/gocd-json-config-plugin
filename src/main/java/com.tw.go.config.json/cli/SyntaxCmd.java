package com.tw.go.config.json.cli;

import com.beust.jcommander.Parameter;

class SyntaxCmd {
    @Parameter(names = {"--help", "-h"}, help = true, description = "Print this help message")
    boolean help;

    @Parameter(description = "file", required = true)
    String file;
}
