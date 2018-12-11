package com.tw.go.config.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import static java.lang.String.format;

public class JsonConfigParser {

    private final JsonParser parser;

    public JsonConfigParser() {
        parser = new JsonParser();
    }

    public static JsonElement parseStream(JsonConfigCollection result, InputStream input, String location) {
        return parseStream(result, new JsonConfigParser(), input, location);
    }

    public static JsonElement parseStream(JsonConfigCollection result, JsonConfigParser parser, InputStream input, String location) {
        try (InputStreamReader contentReader = new InputStreamReader(input)) {
            JsonElement el = parser.parse(contentReader);

            if (el == null || el.isJsonNull()) {
                PluginError error = new PluginError("File is empty", location);
                result.addError(error);
            } else if (el.equals(new JsonObject())) {
                PluginError error = new PluginError("Definition is empty", location);
                result.addError(error);
            } else {
                return el;
            }
        } catch (IOException | JsonParseException e) {
            PluginError error = new PluginError(format("Failed to parse file as JSON: %s", e.getMessage()), location);
            result.addError(error);
        }

        return null;
    }

    private JsonElement parse(Reader reader) {
        return parser.parse(reader);
    }
}
