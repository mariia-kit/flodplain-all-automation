package com.here.platform.common;

import io.restassured.response.Response;
import lombok.AllArgsConstructor;


public class ResponseExpectMessages {

    private final Response targetResponse;

    public ResponseExpectMessages(Response targetResponse) {
        this.targetResponse = targetResponse;
    }

    public String expectedStatuesCode(StatusCode expectedStatusCode) {
        return new StringBuilder().append("\n")
                .append("Expected status code: ").append(expectedStatusCode.code).append("\n")
                .append("Actual status code: ").append(targetResponse.statusCode()).append("\n")
                .append("Actual response body:").append("\n")
                .append(targetResponse.body().asString()).append("\n")
                .toString();
    }

    public String expectedResponseBodyClass(Class expectedClass) {
        return new StringBuilder().append("\n")
                .append("Unexpected response body:").append("\n")
                .append(targetResponse.asString()).append("\n")
                .append("Expected body type: ").append(expectedClass.getSimpleName())
                .toString();
    }

    @AllArgsConstructor
    public enum StatusCode {

        CREATED(201), OK(200), ACCEPTED(202), NO_CONTENT(204),
        REDIRECT(302),
        BAD_REQUEST(400), UNAUTHORIZED(401), NOT_FOUND(404), FORBIDDEN(409), TOO_LARGE(413),
        SERVER_ERROR(500), SERVICE_UNAVAILABLE(503), GATEWAY_TIMEOUT(504);

        public int code;

    }

}
