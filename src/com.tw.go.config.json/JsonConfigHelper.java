package com.tw.go.config.json;

import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.request.GoApiRequest;

import java.util.Map;

class JsonConfigHelper {
    private JsonConfigHelper() {
    }

    static GoApiRequest request(final String api, final String responseBody, GoPluginIdentifier identifier) {
        return new GoApiRequest() {
            @Override
            public String api() {
                return api;
            }

            @Override
            public String apiVersion() {
                return "2.0";
            }

            @Override
            public GoPluginIdentifier pluginIdentifier() {
                return identifier;
            }

            @Override
            public Map<String, String> requestParameters() {
                return null;
            }

            @Override
            public Map<String, String> requestHeaders() {
                return null;
            }

            @Override
            public String requestBody() {
                return responseBody;
            }
        };
    }
}
