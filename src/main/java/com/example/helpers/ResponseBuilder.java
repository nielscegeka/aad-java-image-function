package com.example.helpers;

import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;

import java.util.Map;

public class ResponseBuilder {
    public HttpResponseMessage createResponse(HttpRequestMessage<Map<String,Object>> request,
                                              HttpStatus status, String body, boolean isJson) {
        if (isJson) {
            return request.createResponseBuilder(status).header("Content-Type", "application/json")
                    .body("{\"image\": \"" + body + "\"}").build();
        }

        return request.createResponseBuilder(status).body(body).build();
    }
}
