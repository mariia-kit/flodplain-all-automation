package com.here.platform.ns.restEndPoints;

import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.NSError;
import com.here.platform.ns.dto.SentryError;
import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.ns.helpers.DefaultResponses;
import io.qameta.allure.Step;
import io.restassured.http.Header;
import io.restassured.response.Response;
import java.util.Map;
import java.util.function.Predicate;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;


public class NeutralServerResponseAssertion {

    @Getter
    private final Response response;

    public NeutralServerResponseAssertion(Response response) {
        this.response = response;
    }

    @Step("Verify response correspond to special condition: {message}.")
    public NeutralServerResponseAssertion expected(Predicate<Response> condition, String message) {
        Assertions.assertTrue(condition.test(response), message);
        return this;
    }

    @Step("Verify response value by json path {jsonPath} equals to '{expected}'")
    public NeutralServerResponseAssertion expectedEquals(String jsonPath, String expected, String message) {
        Object value = response.getBody().jsonPath().get(jsonPath);
        String actual = value == null ? StringUtils.EMPTY : value.toString();
        Assertions.assertEquals(expected, actual, message);
        return this;
    }

    @Step("Verify response value equals to Container: '{expected.name}'")
    public NeutralServerResponseAssertion expectedEqualsContainer(Container expected, String message) {
        Assertions.assertEquals(expected, DefaultResponses.extractAsContainer(response), message);
        return this;
    }

    @Step("Verify response value in Container list equals to Container: '{expected.name}'")
    public NeutralServerResponseAssertion expectedEqualsContainerInList(Container expected, String message) {
        Assertions.assertEquals(expected,
                DefaultResponses.extractContainerPresentInList(expected.getName(), response),
                message);
        return this;
    }

    @Step("Verify response value equals to DataProvider: '{expected.name}'")
    public NeutralServerResponseAssertion expectedEqualsProvider(DataProvider expected, String message) {
        Assertions.assertEquals(expected, DefaultResponses.extractAsProvider(response), message);
        return this;
    }

    @Step("Verify response value equals to Container resource.")
    public NeutralServerResponseAssertion expectedEqualsContainerData(Map<String, String> expected,
            String message) {
        Assertions
                .assertEquals(expected, DefaultResponses.extractAsContainerData(response), message);
        return this;
    }

    @Step("Verify response value equals to ISO Container resource.")
    public NeutralServerResponseAssertion expectedEqualsISOContainerData(Map<String, String> expected,
            String message) {
        Assertions
                .assertEquals(expected, DefaultResponses.extractAsISOContainerData(response), message);
        return this;
    }

    @Step("Verify response value by json path {jsonPath} contains value '{expected}'")
    public NeutralServerResponseAssertion expectedJsonContains(String jsonPath, String expected,
            String message) {
        Object value = response.getBody().jsonPath().get(jsonPath);
        String actual = value == null ? StringUtils.EMPTY : value.toString();
        Assertions.assertTrue(actual.contains(expected), message + " '" + actual + "'");
        return this;
    }

    @Step("Verify response value by json path {jsonPath} are correct.")
    public NeutralServerResponseAssertion expectedJsonTrue(String jsonPath, Predicate<String> condition,
            String message) {
        Object value = response.getBody().jsonPath().get(jsonPath);
        String actual = value == null ? StringUtils.EMPTY : value.toString();
        Assertions.assertTrue(condition.test(actual), message + " '" + actual + "'");
        return this;
    }

    @Step("Verify response code equals to '{responseCode}'")
    public NeutralServerResponseAssertion expectedCode(int responseCode) {
        Assertions.assertEquals(responseCode, response.getStatusCode(),
                "Response code not as expected!");
        return this;
    }

    @Step("Verify response contains correct header '{header.name} - {header.value}'")
    public NeutralServerResponseAssertion expectedHeader(Header header) {
        Assertions.assertNotNull(response.getHeader(header.getName()),
                "No header " + header.getName() + " detected in response:" + response.headers()
                        .toString());
        Assertions.assertEquals(header.getValue(), response.getHeader(header.getName()),
                "Header " + header.getName() + " value not as expected!");
        return this;
    }

    @Step("Verify response contains header '{headerName}'")
    public NeutralServerResponseAssertion expectedHeaderIsPresent(String headerName) {
        Assertions.assertNotNull(response.getHeader(headerName),
                "No header " + headerName + " detected in response:" + response.headers()
                        .toString());
        return this;
    }

    @Step("Verify response contains NS error {error.status} {error.title}")
    public NeutralServerResponseAssertion expectedError(NSError error) {
        if (response.getStatusCode() == error.getStatus()) {
            try {
                NSError actual = response.getBody().as(NSError.class);
                Assertions.assertEquals(error, actual, "NS Error not as expected!");
                Assertions.assertEquals(actual.getCorrelationId(),
                        response.getHeader("X-Correlation-ID"),
                        "X-Correlation-ID in body not same as in header!");
                Assertions.assertFalse(StringUtils.isEmpty(actual.getCorrelationId()));
            } catch (ClassCastException e) {
                Assertions.fail("No sign of error " + error.getStatus() + " detected!");
            }
        } else {
            Assertions.fail("Expected error code " + error.getStatus() +
                    " not detected, " + response.getStatusCode() + " found!");
        }
        return this;
    }

    @Step("Verify response contains Sentry error {error.status} {error.error}")
    public NeutralServerResponseAssertion expectedSentryError(SentryError error) {
        if (response.getStatusCode() == error.getStatus()) {
            try {
                int code = response.getStatusCode();
                String errorName = response.jsonPath().getString("error");
                String errorDescr = response.jsonPath().getString("error_description");
                SentryError actual = new SentryError(code, errorName, errorDescr);
                Assertions.assertEquals(error, actual, "Sentry Error not as expected!");
            } catch (ClassCastException e) {
                Assertions.fail("No sign of error " + error.getStatus() + " detected!");
            }
        } else {
            Assertions.fail("Expected error code " + error.getStatus() +
                    " not detected, " + response.getStatusCode() + " found!");
        }
        return this;
    }

    @Step("Verify response value body equals to expected {expected}")
    public NeutralServerResponseAssertion expectedBody(String expected,
            String message) {
        Assertions.assertEquals(expected, response.getBody().print(), message);
        return this;
    }

    public NeutralServerResponseAssertion expectedSentryError(SentryErrorsList error) {
        return expectedSentryError(error.getError());
    }

}
