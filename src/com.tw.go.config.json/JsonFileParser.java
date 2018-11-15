package com.tw.go.config.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;

import static java.lang.String.format;

public class JsonFileParser {

    private final JsonParser parser;

    public JsonFileParser() {
        parser = new JsonParser();
    }

    public static JsonElement processFile(JsonConfigCollection result, JsonFileParser parser, File file) throws Exception {
        try {
            JsonElement el = parser.parseFile(file);

            if (el == null || el.isJsonNull()) {
                PluginError error = new PluginError("File is empty", file.getPath());
                result.addError(error);
            } else if (el.equals(new JsonObject())) {
                PluginError error = new PluginError("Definition is empty", file.getPath());
                result.addError(error);
            } else {
                return el;
            }
        } catch (JsonParseException e) {
            PluginError error = new PluginError(format("Failed to parse file as JSON: %s", e.getMessage()), file.getPath());
            result.addError(error);
        }

        return null;
    }

    public JsonElement parseFile(File path) throws Exception {
        final FileReader reader = new FileReader(path);
        return parser.parse(reader);
    }
}
