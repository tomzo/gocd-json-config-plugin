package com.tw.go.config.json;

import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.request.GoApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.Map;

public class JsonConfigHelper {
    private JsonConfigHelper() {
    }

    public static GoApiRequest request(final String api, final String responseBody, GoPluginIdentifier identifier) {
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

    static GoPluginApiResponse response(final int responseCode, final String json) {
        return new GoPluginApiResponse() {
            @Override
            public int responseCode() {
                return responseCode;
            }

            @Override
            public Map<String, String> responseHeaders() {
                return null;
            }

            @Override
            public String responseBody() {
                return json;
            }
        };

    }
}
