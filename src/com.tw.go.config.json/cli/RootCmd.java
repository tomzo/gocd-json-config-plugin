package com.tw.go.config.json.cli;

import com.beust.jcommander.Parameter;

class RootCmd {
    @Parameter(names = {"--help", "-h"}, help = true, description = "Print this help message")
    boolean help;
}
