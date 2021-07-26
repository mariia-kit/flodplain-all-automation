package com.here.platform.proxy.helper;

import static io.qameta.allure.Allure.step;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.here.platform.ns.dto.SentryError;
import com.here.platform.proxy.dto.AwsS3Provider;
import com.here.platform.proxy.dto.ProxyError;
import com.here.platform.proxy.dto.ProxyProvider;
import com.here.platform.proxy.dto.ProxyProviderResource;
import com.here.platform.proxy.dto.ProxyTunnelError;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import java.util.Arrays;
import lombok.Getter;
import org.assertj.core.api.Assertions;


public class ProxyProviderAssertion {

    @Getter
    private final Response response;

    public ProxyProviderAssertion(Response response) {
        this.response = response;
    }

    @Step("Expected response code equals to '{responseCode}'")
    public ProxyProviderAssertion expectedCode(int responseCode) {
        Assertions.assertThat(response.getStatusCode())
                .withFailMessage("Response code not as expected! Expected "+ responseCode + " but found " + response.getStatusCode())
                .isEqualTo(responseCode);
        return this;
    }

    @Step("Expected response value equals to ProxyProvider: '{expected.serviceName}'")
    public ProxyProviderAssertion expectedEqualsProvider(ProxyProvider expected) {
        var actual = response.getBody().as(ProxyProvider.class);
        Assertions.assertThat(actual).isEqualToIgnoringGivenFields(expected,
                "id", "scbeId", "resources", "authMethod", "apiKey", "apiKeyQueryParamName", "authUsername", "authPassword");
        return this;
    }

    @Step("Expected response value equals to AWSProvider: '{expected.serviceName}'")
    public ProxyProviderAssertion expectedEqualsAwsS3Provider(AwsS3Provider expected) {
        var actual = response.getBody().as(AwsS3Provider.class);
        Assertions.assertThat(actual).isEqualToIgnoringGivenFields(expected,
                "id", "resources", "serviceName", "providerRealm", "identifier", "providerType");
        return this;
    }

    @Step("Expected response value equals to ProxyProvider: '{expected.serviceName}'")
    public ProxyProviderAssertion expectedProviderInList(ProxyProvider expected) {
        ProxyProvider[] actual = response.getBody().as(ProxyProvider[].class);
        Arrays.stream(actual).filter(prov -> prov.getId().equals(expected.getId()))
                .findAny().ifPresentOrElse(prov ->
        Assertions.assertThat(prov).isEqualToIgnoringGivenFields(expected,
                "id", "scbeId", "resources", "authMethod", "apiKey", "apiKeyQueryParamName", "authUsername", "authPassword"),
                () -> Assertions.fail("No provider with id " + expected.getId() + " found in response!"));
        return this;
    }

    @Step("Expected response value equals to ProxyProvider: '{proxyProvider.serviceName}' with resource {resource.title}")
    public ProxyProviderAssertion expectedProviderInList(ProxyProvider proxyProvider, ProxyProviderResource resource) {
        ProxyProvider[] actual = response.getBody().as(ProxyProvider[].class);
        Arrays.stream(actual).filter(prov -> prov.getId().equals(proxyProvider.getId()))
                .findAny().ifPresentOrElse(prov -> {
                    Assertions.assertThat(prov).isEqualToIgnoringGivenFields(proxyProvider,
                            "id", "scbeId", "resources", "authMethod", "apiKey",
                            "apiKeyQueryParamName", "authUsername", "authPassword");
                    prov.getResources().stream()
                            .filter(res -> res.getTitle().equals(resource.getTitle()))
                            .findAny()
                            .ifPresentOrElse(res ->
                                    Assertions.assertThat(res).isEqualToIgnoringGivenFields(resource,"id"),
                                    () -> Assertions.fail("No resource with title " + resource.getTitle() + " found in response!"));
                },
                () -> Assertions.fail("No provider with id " + proxyProvider.getId() + " found in response!"));
        return this;
    }

    @Step("Expected response value equals to ProxyProvider resource: '{expected.title}'")
    public ProxyProviderAssertion expectedResourceInProvider(ProxyProviderResource expected) {
        var provider = response.getBody().as(ProxyProvider.class);
        ProxyProviderResource actual = provider.getResources().stream().filter(res -> expected.getTitle().equals(res.getTitle()))
                .findAny().orElseThrow(() -> new RuntimeException("No resource with title "
                + expected.getTitle() + " found in current proxy provider!"));
        Assertions.assertThat(actual).isEqualToIgnoringGivenFields(expected, "id");
        return this;
    }

    @Step("Expected response contains Proxy error {error.status} {error.title}")
    public ProxyProviderAssertion expectedError(ProxyError error) {
        if (response.getStatusCode() == error.getStatus()) {
            try {
                ProxyError actual = response.getBody().as(ProxyError.class);
                Assertions.assertThat(actual).isEqualTo(error);
            } catch (ClassCastException e) {
                Assertions.fail("No sign of error " + error.getStatus() + " detected!");
            }
        } else {
            Assertions.fail("Expected error code " + error.getStatus() +
                    " not detected, " + response.getStatusCode() + " found!");
        }
        return this;
    }

    @Step("Expected response contains Proxy Tunnel error {error.status} {error.error}")
    public ProxyProviderAssertion expectedTunnelError(ProxyTunnelError error) {
        if (response.getStatusCode() == error.getStatus()) {
            try {
                ProxyTunnelError actual = response.getBody().as(ProxyTunnelError.class);
                Assertions.assertThat(actual).isEqualToIgnoringGivenFields(error, "timestamp");
            } catch (ClassCastException e) {
                Assertions.fail("No sign of error " + error.getStatus() + " detected!");
            }
        } else {
            Assertions.fail("Expected error code " + error.getStatus() +
                    " not detected, " + response.getStatusCode() + " found!");
        }
        return this;
    }

    @Step("Expected response contains Sentry error {error.status} {error.error}")
    public ProxyProviderAssertion expectedSentryError(SentryError error) {
        if (response.getStatusCode() == error.getStatus()) {
            try {
                int code = response.getStatusCode();
                String errorName = response.jsonPath().getString("error");
                String errorDescr = response.jsonPath().getString("error_description");
                SentryError actual = new SentryError(code, errorName, errorDescr);
                assertEquals(error, actual, "Sentry Error not as expected!");
            } catch (ClassCastException e) {
                org.junit.jupiter.api.Assertions.fail("No sign of error " + error.getStatus() + " detected!");
            }
        } else {
            org.junit.jupiter.api.Assertions.fail("Expected error code " + error.getStatus() +
                    " not detected, " + response.getStatusCode() + " found!");
        }
        return this;
    }

    @Step("Expected response time is less then '{maxThreshold}'")
    public ProxyProviderAssertion verifyResponseTime(long maxThreshold) {
        step("Actual response time:" + response.getTime());
        Assertions.assertThat(response.getTime())
                .isLessThanOrEqualTo(maxThreshold)
                .withFailMessage("Response time " + response.getTime() +
                        " is more than expected threshold " + maxThreshold + " ms!");
        return this;
    }

    @Step("Expected response value equals to ProxyProvider: '{expected.serviceName}'")
    public ProxyProviderAssertion expectedEqualsProviderNotIgnoringFields(String resourcePath) {

        //TODO: refactor this
        assertNotNull(response.jsonPath().getString("resId"));
        assertNotNull(response.jsonPath().getString("x-forwarded-for"));
        assertNotNull(response.jsonPath().getString("x-forwarded-proto"));
        assertNotNull(response.jsonPath().getString("x-forwarded-port"));
        assertNotNull(response.jsonPath().getString("host"));
        assertNotNull(response.jsonPath().getString("x-amzn-trace-id"));

        assertNotNull(response.jsonPath().getString("authorization-claims"));
        assertNotNull(response.jsonPath().getString("rlm"));
        assertNotNull(response.jsonPath().getString("x-gdpr-subp"));
        assertNotNull(response.jsonPath().getString("x-request-id"));
        assertNotNull(response.jsonPath().getString("x-correlation-id"));
        assertNotNull(response.jsonPath().getString("accept"));

        assertNotNull(response.jsonPath().getString("accept-encoding"));
        assertNotNull(response.jsonPath().getString("user-agent"));
        assertNotNull(response.jsonPath().getString("x-auth-source"));
        assertNotNull(response.jsonPath().getString("x-auth-time"));
        assertNotNull(response.jsonPath().getString("if-modified-since"));
        assertNotNull(response.jsonPath().getString("Authorization"));

        assertTrue(response.jsonPath().getString("host").contains("reference-data-provider.ost.solo-experiments.com"));

        //TODO: refactor this
        assertEquals("[" + resourcePath.substring(11) + ", null, null]", response.jsonPath().getString("resId"));

        return this;
    }
}
