package com.here.platform.proxy.helper;

import com.here.platform.proxy.dto.ProxyError;
import com.here.platform.proxy.dto.ProxyProvider;
import com.here.platform.proxy.dto.ProxyProviderResource;
import io.qameta.allure.Step;
import io.restassured.response.Response;
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
        Assertions.assertThat(actual).isEqualToIgnoringGivenFields(expected, "id", "scbeId", "resources", "authMethod");
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
                Assertions.assertThat(actual)
                        .withFailMessage("Proxy Error not as expected!")
                        .isEqualTo(error);
            } catch (ClassCastException e) {
                Assertions.fail("No sign of error " + error.getStatus() + " detected!");
            }
        } else {
            Assertions.fail("Expected error code " + error.getStatus() +
                    " not detected, " + response.getStatusCode() + " found!");
        }
        return this;
    }
}
