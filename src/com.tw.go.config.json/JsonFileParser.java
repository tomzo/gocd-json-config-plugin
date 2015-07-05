package com.tw.go.config.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

public class JsonFileParser {

    private final JsonParser parser;

    public JsonFileParser()
    {
        parser = new JsonParser();
    }

    public JsonElement parseFile(File path) throws Exception
    {
        final FileReader reader = new FileReader(path);
        return parser.parse(reader);
    }
}
