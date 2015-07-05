package com.tw.go.config.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.plugin.configrepo.messages.ParseDirectoryMessage_1;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

@Extension
public class JsonConfigPlugin implements GoPlugin {
    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {

    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) throws UnhandledRequestTypeException {
        if ("parse-directory".equals(request.requestName())) {
            return handleParseDirectoryRequest(request);
        }
        throw new UnhandledRequestTypeException(request.requestName());
    }

    private GoPluginApiResponse handleParseDirectoryRequest(GoPluginApiRequest request) {
        //ParseDirectoryMessage_1 requestArguments;
        JsonConfigCollection config = new JsonConfigCollection();
        JsonFileParser parser = new JsonFileParser();
        DirectoryScanner scanner = new DirectoryScanner();
        for(File environmentFile : scanner.getEnvironmentFiles())
        {
            JsonElement env;
            try {
                env = parser.parseFile(environmentFile);
            }
            catch (Exception ex)
            {
                String errorBody = "{}";
                return DefaultGoPluginApiResponse.error(errorBody);
            }
            config.addEnvironment(env);
        }

        Gson gson = new Gson();

        return DefaultGoPluginApiResponse.success(gson.toJson(config.getJsonObject()));
    }

    private GoPluginApiResponse createResponse(int responseCode, String body) {
        final DefaultGoPluginApiResponse response = new DefaultGoPluginApiResponse(responseCode);
        response.setResponseBody(body);
        return response;
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier("configrepo", Arrays.asList("1.0"));
    }
}
