package com.tw.go.config.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static java.lang.String.format;

public class JsonConfigParser {
    public static JsonElement parseStream(JsonConfigCollection result, InputStream input, String location) {
        try (InputStreamReader contentReader = new InputStreamReader(input)) {
            if (input.available() < 1) {
                result.addError(new PluginError("File is empty", location));
                return null;
            }

            JsonElement el = JsonParser.parseReader(contentReader);

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

}
