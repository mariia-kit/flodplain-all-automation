package com.here.platform.common;

import static io.qameta.allure.Allure.addAttachment;
import static io.qameta.allure.Allure.step;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import lombok.experimental.UtilityClass;


@UtilityClass
class AllureExtension {

    void logHttpRequest(HttpRequest targetRequest) {
        step((requestStep) -> {
            requestStep.name(String.format("Send %s request", targetRequest.method()));
            requestStep.parameter("To", targetRequest.uri());
            requestStep.parameter("With headers", targetRequest.headers().toString());
        });
    }

    void logHttpResponse(HttpResponse<String> targetResponse) {
        step((responseStep) -> {
            responseStep.name("Response");
            responseStep.parameter("Status code", targetResponse.statusCode());
            responseStep.parameter("Headers", targetResponse.headers());
            addAttachment("Body", "application/json", String.valueOf(targetResponse.body()));
            var previousResponse = targetResponse.previousResponse();
            logRedirect(previousResponse);
        });
    }

    private void logRedirect(Optional<HttpResponse<String>> previousResponse) {
        previousResponse.ifPresent(redirectResponse -> step((inner) -> {
            inner.name("Redirect");
            inner.parameter("from", redirectResponse.uri());
            inner.parameter("headers", redirectResponse.headers());
            inner.parameter("status code", redirectResponse.statusCode());
            if (redirectResponse.previousResponse().isPresent()) {
                logRedirect(Optional.of(redirectResponse.previousResponse().get()));
            }
        }));
    }

}
