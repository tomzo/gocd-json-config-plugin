package com.tw.go.config.json.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.gson.JsonObject;
import com.tw.go.config.json.JsonConfigCollection;
import com.tw.go.config.json.JsonFileParser;
import com.tw.go.config.json.PluginError;

import java.io.File;

import static java.lang.String.format;

public class JsonPluginCli {
    public static void main(String[] args) {
        RootCmd root = new RootCmd();
        SyntaxCmd syntax = new SyntaxCmd();

        JCommander cmd = JCommander.newBuilder().
                programName("yaml-cli").
                addObject(root).
                addCommand("syntax", syntax).
                build();

        try {
            cmd.parse(args);

            if (root.help) {
                printUsageAndExit(0, cmd, cmd.getParsedCommand());
            }

            if (syntax.help) {
                printUsageAndExit(0, cmd, cmd.getParsedCommand());
            }

            if (null == syntax.file) {
                printUsageAndExit(1, cmd, cmd.getParsedCommand());
            }
        } catch (ParameterException e) {
            error(e.getMessage());
            printUsageAndExit(1, cmd, cmd.getParsedCommand());
        }

        JsonConfigCollection collection = new JsonConfigCollection();
        JsonObject result = collection.getJsonObject();

        File file = new File(syntax.file);

        try {
            JsonFileParser.processFile(collection, new JsonFileParser(), file);
        } catch (Exception e) {
            collection.addError(new PluginError(e.getMessage(), file.getAbsolutePath()));
        }

        result.remove("environments");
        result.remove("pipelines");

        if (collection.getErrors().size() > 0) {
            die(syntax.quiet, 1, result.toString());
        } else {
            die(syntax.quiet, 0, "OK");
        }
    }

    private static void echo(String message, Object... items) {
        System.out.println(format(message, items));
    }

    private static void error(String message, Object... items) {
        System.err.println(format(message, items));
    }

    private static void die(int exitCode, String message, Object... items) {
        if (exitCode != 0) {
            error(message, items);
        } else {
            echo(message, items);
        }
        System.exit(exitCode);
    }

    private static void die(boolean quietly, int exitCode, String message, Object... items) {
        if (quietly) {
            System.exit(exitCode);
        }
        die(exitCode, message, items);
    }

    private static void printUsageAndExit(int exitCode, JCommander cmd, String command) {
        StringBuilder out = new StringBuilder();
        if (null == command) {
            cmd.usage(out);
        } else {
            cmd.usage(command, out);
        }
        die(exitCode, out.toString());
    }
}
